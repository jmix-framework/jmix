/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dashboardsui.transformation;

import io.jmix.dashboards.model.parameter.ParameterType;
import io.jmix.dashboards.model.parameter.type.ParameterValue;
import io.jmix.ui.screen.ScreenFragment;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public interface ParameterTransformer {

    @Nullable
    Object transform(ParameterValue parameterValue);

    boolean compareParameterTypes(ParameterType parameterType, Field field);

    @Nullable
    ParameterValue createParameterValue(Field field, ScreenFragment widgetFragment);

    /**
     * Returns ParameterValue object according to passed parameter class
     *
     * @param obj parameter
     * @return wrapped object which implements {@link ParameterValue} interface
     */
    @Nullable
    ParameterValue createParameterValue(Object obj);
}
