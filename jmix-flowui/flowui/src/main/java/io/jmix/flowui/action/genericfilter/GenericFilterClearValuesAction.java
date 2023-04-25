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
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.filer.FilterComponent;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.Nullable;
import java.util.List;

@ActionType(GenericFilterClearValuesAction.ID)
public class GenericFilterClearValuesAction extends GenericFilterAction<GenericFilterClearValuesAction> {

    public static final String ID = "filter_clearValues";

    protected Registration configurationChangeRegistration;
    protected Registration filterComponentsChangeRegistration;

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
    protected void setTargetInternal(@Nullable GenericFilter target) {
        super.setTargetInternal(target);

        if (target != null) {
            unbindListeners();
            bindListeners(target);
        }
    }

    protected void bindListeners(GenericFilter target) {
        configurationChangeRegistration = target.addConfigurationChangeListener(this::onConfigurationChanged);
        bindFilterComponentsChangeListener(target);
    }

    protected void bindFilterComponentsChangeListener(GenericFilter target) {
        LogicalFilterComponent<?> rootLogicalFilterComponent = target.getCurrentConfiguration()
                .getRootLogicalFilterComponent();
        filterComponentsChangeRegistration = rootLogicalFilterComponent
                .addFilterComponentsChangeListener(this::onFilterComponentsChanged);
    }

    protected void unbindListeners() {
        if (configurationChangeRegistration != null) {
            configurationChangeRegistration.remove();
            configurationChangeRegistration = null;
        }

        unbindFilterComponentsChange();
    }

    protected void unbindFilterComponentsChange() {
        if (filterComponentsChangeRegistration != null) {
            filterComponentsChangeRegistration.remove();
            filterComponentsChangeRegistration = null;
        }
    }

    protected void onConfigurationChanged(GenericFilter.ConfigurationChangeEvent event) {
        unbindFilterComponentsChange();
        bindFilterComponentsChangeListener(event.getSource());
        refreshState();
    }

    protected void onFilterComponentsChanged(LogicalFilterComponent.FilterComponentsChangeEvent<?> event) {
        refreshState();
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
