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
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.gradle.api.logging.Logger;

import java.io.IOException;

import static io.jmix.gradle.MetaModelUtil.*;

public abstract class BaseEnhancingStep implements EnhancingStep {
    protected ClassPool classPool;
    protected String outputDir;
    protected Logger logger;

    @Override
    public void execute(String className) {
        try {
            CtClass ctClass = classPool.get(className);

            if (isAlreadyEnhanced(ctClass)) {
                logger.info(String.format("[%s] %s has already been enhanced", getEnhancingType(), className));
                return;
            }

            if (isEnhancingDisabled(ctClass)) {
                logger.info(String.format("[%s] Enhancing disabled for %s entity", getEnhancingType(), className));
                return;
            }

            if (isJpaConverter(ctClass)) {
                return;
            }

            if (!isEnhancingSupported(ctClass)) {
                logger.info(String.format("[%s] %s is not an Entity and should not be enhanced", getEnhancingType(), className));
                return;
            }

            logger.info(String.format("[%s] Enhance class %s...", getEnhancingType(), className));

            executeInternal(ctClass);

            ctClass.writeFile(outputDir);
        } catch (NotFoundException | IOException | CannotCompileException e) {
            throw new EnhancingException(String.format("Error while enhancing class %s: %s", className, e.getMessage()), e);
        }
    }

    public boolean isEnhancingSupported(CtClass ctClass) {
        return isJpaEntity(ctClass) || isJpaEmbeddable(ctClass) || isJpaMappedSuperclass(ctClass)
                || isJmixEntity(ctClass);
    }

    @Override
    public void setClassPool(ClassPool classPool) {
        this.classPool = classPool;
    }

    @Override
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected abstract boolean isAlreadyEnhanced(CtClass ctClass) throws NotFoundException;

    protected abstract String getEnhancingType();

    protected abstract void executeInternal(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException;
}
