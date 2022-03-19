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

package io.jmix.ui.app.filter.condition;

import io.jmix.ui.component.Filter;
import io.jmix.ui.entity.FilterCondition;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.StandardEditor;

public abstract class FilterConditionEdit<E extends FilterCondition> extends StandardEditor<E> {

    protected Filter.Configuration currentConfiguration;

    public abstract InstanceContainer<E> getInstanceContainer();

    public void setCurrentConfiguration(Filter.Configuration currentConfiguration) {
        this.currentConfiguration = currentConfiguration;
    }
}
