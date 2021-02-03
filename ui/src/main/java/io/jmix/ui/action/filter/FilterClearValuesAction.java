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

package io.jmix.ui.action.filter;

import io.jmix.core.Messages;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import org.springframework.beans.factory.annotation.Autowired;

@StudioAction(target = "io.jmix.ui.component.Filter", description = "Clears the filter condition values")
@ActionType(FilterClearValuesAction.ID)
public class FilterClearValuesAction extends FilterAction {

    public static final String ID = "filter_clearValues";

    public FilterClearValuesAction() {
        this(ID);
    }

    public FilterClearValuesAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Filter.ClearValues");
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.ERASER);
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && !filter.getCurrentConfiguration().getRootLogicalFilterComponent().getFilterComponents().isEmpty();
    }

    @Override
    public void execute() {
        filter.getCurrentConfiguration()
                .getRootLogicalFilterComponent()
                .getOwnFilterComponents()
                .stream()
                .filter(filterComponent -> filterComponent instanceof HasValue)
                .forEach(filterComponent -> ((HasValue<?>) filterComponent).setValue(null));
        filter.apply();
    }
}
