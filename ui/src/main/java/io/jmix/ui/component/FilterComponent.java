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

package io.jmix.ui.component;

import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.Condition;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.DataLoader;

/**
 * Component that can be used for filtering entities returned by the {@link DataLoader}.
 * The filter component is related to {@link Condition} which will be used together with
 * query when loading entities into the {@link DataLoader}.
 */
public interface FilterComponent extends Component {

    /**
     * @return a {@link DataLoader} related to the current filter component
     */
    DataLoader getDataLoader();

    /**
     * Sets a {@link DataLoader} related to the current filter component.
     *
     * @param dataLoader a {@link DataLoader} to set
     */
    @StudioProperty(type = PropertyType.DATALOADER_REF, required = true)
    void setDataLoader(DataLoader dataLoader);

    /**
     * @return {@code true} if the filter component should be automatically applied to
     * the {@link DataLoader} when the value component value is changed
     */
    boolean isAutoApply();

    /**
     * Sets whether the filter component should be automatically applied to the
     * {@link DataLoader} when the value component value is changed.
     *
     * @param autoApply {@code true} if the filter component should be automatically
     *                  applied to the {@link DataLoader} when the value component
     *                  value is changed
     */
    @StudioProperty(defaultValue = "true")
    void setAutoApply(boolean autoApply);

    /**
     * @return {@code true} if the filter component is located inside the {@link Filter}
     * or {@link LogicalFilterComponent} and the modification of {@link DataLoader}
     * condition is delegated to the owner {@link FilterComponent} or {@link Filter},
     * {@code false} otherwise
     */
    @Internal
    boolean isConditionModificationDelegated();

    /**
     * Sets whether the modification of {@link DataLoader} condition is delegated to the owner
     * {@link FilterComponent} or {@link Filter}.
     *
     * @param conditionModificationDelegated {@code true} if the filter component is located inside
     *                                       the {@link Filter} or {@link LogicalFilterComponent}
     *                                       and the modification of {@link DataLoader} condition
     *                                       is delegated to the owner {@link FilterComponent} or {@link Filter},
     *                                       {@code false} otherwise
     */
    @Internal
    void setConditionModificationDelegated(boolean conditionModificationDelegated);

    /**
     * @return a {@link Condition} related to the current filter component
     */
    Condition getQueryCondition();

    /**
     * Applies the current filter component condition.
     */
    void apply();
}
