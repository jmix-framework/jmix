/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.action.genericfilter;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.filer.FilterComponent;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@ActionType(GenericFilterClearValuesAction.ID)
public class GenericFilterClearValuesAction extends GenericFilterAction<GenericFilterClearValuesAction> {

    public static final String ID = "genericFilter_clearValues";

    public GenericFilterClearValuesAction() {
        this(ID);
    }

    public GenericFilterClearValuesAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.ERASER);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.genericFilter.ClearValues");
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() &&
                !target.getCurrentConfiguration().getRootLogicalFilterComponent()
                        .getFilterComponents().isEmpty();
    }

    @Override
    public void execute() {
        checkTarget();

        List<FilterComponent> ownFilterComponents = target.getCurrentConfiguration()
                .getRootLogicalFilterComponent()
                .getOwnFilterComponents();

        if (ownFilterComponents.isEmpty()) {
            return;
        }

        ownFilterComponents.stream()
                .filter(filterComponent -> filterComponent instanceof HasValue)
                .forEach(filterComponent -> {
                    filterComponent.setAutoApply(false);
                    ((HasValue<?, ?>) filterComponent).clear();
                    filterComponent.setAutoApply(true);
                });

        target.apply();
    }
}
