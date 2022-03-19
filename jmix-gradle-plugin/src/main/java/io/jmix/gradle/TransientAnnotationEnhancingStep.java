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

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

import java.io.IOException;

import static io.jmix.gradle.MetaModelUtil.*;

public class TransientAnnotationEnhancingStep extends BaseEnhancingStep {
    @Override
    protected boolean isAlreadyEnhanced(CtClass ctClass) throws NotFoundException {
        return false;
    }

    @Override
    protected String getEnhancingType() {
        return "Transient Entity Entry Enhancer";
    }

    @Override
    protected void executeInternal(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        CtField ctField = findDeclaredField(ctClass, GEN_ENTITY_ENTRY_VAR_NAME);
        if (ctField != null) {
            AnnotationsAttribute attr = new AnnotationsAttribute(ctClass.getClassFile().getConstPool(), AnnotationsAttribute.visibleTag);
            attr.addAnnotation(new Annotation(TRANSIENT_ANNOTATION_TYPE, ctClass.getClassFile().getConstPool()));

            ctField.getFieldInfo().addAttribute(attr);
        }
    }
}
