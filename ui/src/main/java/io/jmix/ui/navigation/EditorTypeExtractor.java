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

package io.jmix.ui.navigation;

import io.jmix.core.JmixEntity;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.StandardEditor;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public final class EditorTypeExtractor {

    private EditorTypeExtractor() {
    }

    @Nullable
    public static Class<? extends JmixEntity> extractEntityClass(WindowInfo windowInfo) {
        Class controllerClass = windowInfo.getControllerClass();

        Class<? extends JmixEntity> entityClass = extractEntityTypeByInterface(controllerClass);
        if (entityClass == null) {
            entityClass = extractEntityTypeByClass(controllerClass);
        }

        return entityClass;
    }

    @Nullable
    protected static Class<? extends JmixEntity> extractEntityTypeByInterface(Class controllerClass) {
        while (controllerClass != null
                && !Arrays.asList(controllerClass.getInterfaces()).contains(EditorScreen.class)) {
            controllerClass = controllerClass.getSuperclass();
        }

        if (controllerClass == null) {
            return null;
        }

        Class<? extends JmixEntity> entityClass = null;

        for (Type genericInterface : controllerClass.getGenericInterfaces()) {
            if (!(genericInterface instanceof ParameterizedType)) {
                continue;
            }

            ParameterizedType paramType = (ParameterizedType) genericInterface;
            String typeName = paramType.getRawType().getTypeName();

            if (!EditorScreen.class.getName().equals(typeName)) {
                continue;
            }

            if (paramType.getActualTypeArguments().length > 0) {
                Type typeArg = paramType.getActualTypeArguments()[0];

                if (typeArg instanceof Class
                        && JmixEntity.class.isAssignableFrom((Class<?>) typeArg)) {
                    //noinspection unchecked
                    entityClass = (Class<? extends JmixEntity>) typeArg;

                    break;
                }
            }
        }

        return entityClass;
    }

    @Nullable
    protected static Class<? extends JmixEntity> extractEntityTypeByClass(Class controllerClass) {
        while (controllerClass != null
                && !isAbstractEditor(controllerClass.getSuperclass())
                && !isStandardEditor(controllerClass.getSuperclass())) {
            controllerClass = controllerClass.getSuperclass();
        }

        if (controllerClass == null
                || (!isAbstractEditor(controllerClass.getSuperclass())
                && !isStandardEditor(controllerClass.getSuperclass()))) {
            return null;
        }

        if (!(controllerClass.getGenericSuperclass() instanceof ParameterizedType)) {
            return null;
        }

        Class<? extends JmixEntity> entityClass = null;

        ParameterizedType paramType = (ParameterizedType) controllerClass.getGenericSuperclass();
        Type typeArg = paramType.getActualTypeArguments()[0];

        if (typeArg instanceof Class
                && JmixEntity.class.isAssignableFrom((Class<?>) typeArg)) {
            //noinspection unchecked
            entityClass = (Class<? extends JmixEntity>) typeArg;
        }

        return entityClass;
    }

    protected static boolean isAbstractEditor(Class controllerClass) {
        return false;
        // TODO: legacy-ui
        // return AbstractEditor.class == controllerClass;
    }

    protected static boolean isStandardEditor(Class controllerClass) {
        return StandardEditor.class == controllerClass;
    }
}
