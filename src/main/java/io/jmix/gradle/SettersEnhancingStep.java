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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static io.jmix.gradle.MetaModelUtil.*;

/**
 * Enhances entity classes: setters fire propertyChange events
 */
public class SettersEnhancingStep extends BaseEnhancingStep {

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

        ctClass.addInterface(classPool.get(SETTERS_ENHANCED_TYPE));
    }

    protected void enhanceSetters(CtClass ctClass) throws NotFoundException, CannotCompileException {
        boolean jmixPropertiesAnnotatedOnly = isJmixPropertiesAnnotatedOnly(ctClass);
        boolean isPersistentEntity = isJpaEntity(ctClass) || isJpaEmbeddable(ctClass) || isJpaMappedSuperclass(ctClass);
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            if (!isSetterMethod(ctMethod)) {
                continue;
            }

            CtField field = findDeclaredFieldByAccessor(ctClass, ctMethod.getName());

            if (field == null) {
                //kotlin-generated accessors case: "'isProp' -> isProp(), setProp()"
                field = findDeclaredKotlinFieldByAccessor(ctClass, ctMethod.getName());
            }

            if (field == null || isPersistentEntity && isTransientField(ctClass, field.getName()) && !isJmixProperty(ctClass, field.getName())) {
                continue;
            }

            if (isPersistentField(ctClass, field.getName()) || isJmixProperty(ctClass, field.getName()) || !jmixPropertiesAnnotatedOnly) {
                CtClass paramType = ctMethod.getParameterTypes()[0];

                if (paramType.isPrimitive()) {
                    throw new IllegalStateException(
                            String.format("Unable to enhance field %s.%s with primitive type %s. Use type %s.",
                                    ctClass.getName(), field.getName(),
                                    paramType.getSimpleName(), StringUtils.capitalize(paramType.getSimpleName())));
                }

                ctMethod.addLocalVariable("__prev", paramType);
                ctMethod.addLocalVariable("__new", paramType);

                String getterName = "get" + ctMethod.getName().substring(3);

                CtMethod getter = findDeclaredMethod(ctClass, getterName);
                if (getter == null) {//'isProp()' may be used instead of 'getProp()'
                    getterName = "is" + ctMethod.getName().substring(3);
                }

                ctMethod.insertBefore(
                        "__prev = this." + getterName + "();"
                );

                ctMethod.insertAfter(
                        "__new = this." + getterName + "();" +
                                "io.jmix.core.impl.EntityInternals.fireListeners(this, \"" + field.getName() + "\", __prev, __new);"
                );
            }
        }
    }

    protected void enhanceProtectedForPersistentMethods(CtClass ctClass) {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (isPersistentMethod(method)) {
                method.setModifiers(Modifier.setProtected(method.getModifiers()));
            }
        }
    }
}
