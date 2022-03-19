/*
 * Copyright 2020 Haulmont.
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
import javassist.NotFoundException;

import java.io.IOException;

import static io.jmix.gradle.MetaModelUtil.ENTITY_TYPE;
import static io.jmix.gradle.MetaModelUtil.subtypeOfEntityInterface;

public class JmixEntityEnhancingStep extends BaseEnhancingStep {
    @Override
    protected boolean isAlreadyEnhanced(CtClass ctClass) throws NotFoundException {
        return subtypeOfEntityInterface(ctClass, classPool);
    }

    @Override
    protected String getEnhancingType() {
        return "Jmix Entity Enhancer";
    }

    @Override
    protected void executeInternal(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        ctClass.addInterface(classPool.get(ENTITY_TYPE));
    }
}
