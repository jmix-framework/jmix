/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.gradle;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jmix.gradle.MetaModelUtil.*;

/**
 * Enhances entity classes: setters fire propertyChange events
 */
public class SettersEnhancingStep extends BaseEnhancingStep {
    private static final Pattern messagePattern = Pattern.compile("(\\{msg://)([\\p{L}\\w.]*})");

    @Override
    protected boolean isAlreadyEnhanced(CtClass ctClass) throws NotFoundException {
        return isSettersEnhanced(ctClass);
    }

    @Override
    protected String getEnhancingType() {
        return "Setters Enhancer";
    }

    @Override
    protected void executeInternal(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        enhanceSetters(ctClass);

        enhanceProtectedForPersistentMethods(ctClass);

        enhanceBeanValidationMessages(ctClass);

        ctClass.addInterface(classPool.get(SETTERS_ENHANCED_TYPE));
    }

    protected void enhanceSetters(CtClass ctClass) throws NotFoundException, CannotCompileException {
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            if (!isSetterMethod(ctMethod)) {
                continue;
            }
            String fieldName = generateFieldNameByMethod(ctMethod.getName());

            // check if the setter is for a persistent or transient property (field is annotated with @MetaProperty)
            if (!isPersistentField(ctClass, fieldName) && !isMetaPropertyField(ctClass, fieldName)) {
                continue;
            }

            CtClass paramType = ctMethod.getParameterTypes()[0];

            if (paramType.isPrimitive()) {
                throw new IllegalStateException(
                        String.format("Unable to enhance field %s.%s with primitive type %s. Use type %s.",
                                ctClass.getName(), fieldName,
                                paramType.getSimpleName(), StringUtils.capitalize(paramType.getSimpleName())));
            }

            ctMethod.addLocalVariable("__prev", paramType);
            ctMethod.addLocalVariable("__new", paramType);

            ctMethod.insertBefore(
                    "__prev = this.get" + StringUtils.capitalize(fieldName) + "();"
            );

            ctMethod.insertAfter(
                    "__new = this.get" + StringUtils.capitalize(fieldName) + "();" +
                            "io.jmix.core.impl.EntityInternals.fireListeners(this, \"" + fieldName + "\", __prev, __new);"
            );
        }
    }

    protected void enhanceProtectedForPersistentMethods(CtClass ctClass) {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (isPersistentMethod(method)) {
                method.setModifiers(Modifier.setProtected(method.getModifiers()));
            }
        }
    }

    protected void enhanceBeanValidationMessages(CtClass ctClass) {
        for (CtField field : ctClass.getDeclaredFields()) {
            byte[] annotationsValue = field.getAttribute(AnnotationsAttribute.visibleTag);
            if (annotationsValue != null) {
                AnnotationsAttribute attr = new AnnotationsAttribute(ctClass.getClassFile().getConstPool(),
                        AnnotationsAttribute.visibleTag, annotationsValue);
                for (Annotation annotation : attr.getAnnotations()) {
                    if (annotation.getMemberNames() != null) {
                        for (String name : new ArrayList<>(annotation.getMemberNames())) {
                            if ("message".equals(name)) {
                                if (annotation.getMemberValue(name) instanceof StringMemberValue) {
                                    String messageValue = ((StringMemberValue) annotation.getMemberValue(name)).getValue();
                                    String newMessage = convertMessageValue(messageValue, ctClass.getPackageName());

                                    if (!StringUtils.equals(messageValue, newMessage)) {
                                        annotation.addMemberValue("message", new StringMemberValue(newMessage,
                                                ctClass.getClassFile().getConstPool()));
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                field.setAttribute(AnnotationsAttribute.visibleTag, attr.get());
            }
        }
    }

    public String convertMessageValue(String value, String packageName) {
        if (messagePattern.matcher(value).matches()) {
            Matcher matcher = messagePattern.matcher(value);
            if (matcher.find()) {
                return matcher.group(1) + packageName + "/" + matcher.group(2);
            }
        }
        return value;
    }
}
