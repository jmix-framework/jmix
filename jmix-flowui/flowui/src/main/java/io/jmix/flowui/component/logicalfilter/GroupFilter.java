/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.component.logicalfilter;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsResponsiveSteps;
import io.jmix.flowui.component.WrapperUtils;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.DataLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.lang.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * This component can contain {@link FilterComponent}s and can be used for filtering entities
 * returned by the {@link DataLoader}.
 */
public class GroupFilter extends Composite<VerticalLayout>
        implements LogicalFilterComponent<GroupFilter>, SupportsResponsiveSteps,
        ApplicationContextAware, InitializingBean {

    protected static final String GROUP_FILTER_CLASS_NAME = "jmix-group-filter";

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;
    protected LogicalFilterSupport logicalFilterSupport;

    protected DataLoader dataLoader;
    protected Condition initialDataLoaderCondition;
    protected boolean autoApply;

    @Internal
    protected boolean conditionModificationDelegated = false;

    protected List<ResponsiveStep> responsiveSteps;
    protected Label summaryComponent;
    protected String summaryText;
    protected boolean operationTextVisible = true;

    protected Operation operation = Operation.AND;
    protected LogicalCondition queryCondition = LogicalCondition.and();

    protected List<FilterComponent> ownFilterComponentsOrder;

    protected FormLayout conditionsLayout;
    protected Map<FilterComponent, FormLayout.FormItem> filterComponentFormItemMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        logicalFilterSupport = applicationContext.getBean(LogicalFilterSupport.class);
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout root = super.initContent();

        root.setClassName(GROUP_FILTER_CLASS_NAME);
        root.setWidthFull();

        return root;
    }

    protected void initComponent() {
        this.autoApply = applicationContext.getBean(UiComponentProperties.class).isFilterAutoApply();

        initDefaultResponsiveSteps();
        initLayout();
    }

    protected void initDefaultResponsiveSteps() {
        responsiveSteps = List.of(
                new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
                new ResponsiveStep("40em", 1),
                new ResponsiveStep("80em", 2),
                new ResponsiveStep("120em", 3)
        );
    }

    protected void initLayout() {
        // hook
    }

    protected FormLayout createConditionsLayout() {
        return uiComponents.create(FormLayout.class);
    }

    protected void initConditionsLayout(FormLayout layout) {
        layout.setResponsiveSteps(WrapperUtils.convertToFormLayoutResponsiveStep(responsiveSteps));
    }

    protected boolean isAnyFilterComponentVisible() {
        return getOwnFilterComponents().stream()
                .anyMatch(ComponentUtils::isVisible);
    }

    protected void addFilterComponentToConditionsLayout(FormLayout conditionsLayout,
                                                        FilterComponent filterComponent) {
        if (!((Component) filterComponent).isVisible()) {
            return;
        }

        if (filterComponent instanceof SingleFilterComponentBase) {
            SingleFilterComponentBase<?> singleFilterComponent = ((SingleFilterComponentBase<?>) filterComponent);

            Label label = new Label(singleFilterComponent.getLabel());
            singleFilterComponent.setWidthFull();
            FormLayout.FormItem formItem = conditionsLayout.addFormItem(singleFilterComponent, label);
            formItem.getElement().getThemeList().addAll(List.of("label-align-end", "jmix-group-filter-form-item"));

            registerFilterComponentFormItem(filterComponent, formItem);

            singleFilterComponent.setLabelDelegate(label::setText);
        } else if (filterComponent instanceof GroupFilter) {
            GroupFilter groupFilter = (GroupFilter) filterComponent;
            conditionsLayout.add(groupFilter, 3);
        }
    }

    private void registerFilterComponentFormItem(FilterComponent filterComponent, FormLayout.FormItem formItem) {
        if (filterComponentFormItemMap == null) {
            filterComponentFormItemMap = new HashMap<>();
        }
        filterComponentFormItemMap.put(filterComponent, formItem);
    }

    @Override
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        checkState(this.dataLoader == null, "DataLoader has already been initialized");
        checkNotNull(dataLoader);

        this.dataLoader = dataLoader;
        this.initialDataLoaderCondition = dataLoader.getCondition();

        if (!isConditionModificationDelegated()) {
            updateDataLoaderCondition();
        }

        updateSummaryText();
    }

    protected void updateDataLoaderCondition() {
        if (dataLoader == null) {
            return;
        }

        LogicalCondition resultCondition;
        if (initialDataLoaderCondition instanceof LogicalCondition) {
            resultCondition = (LogicalCondition) initialDataLoaderCondition.copy();
            resultCondition.add(getQueryCondition());
        } else if (initialDataLoaderCondition != null) {
            resultCondition = LogicalCondition.and()
                    .add(initialDataLoaderCondition)
                    .add(getQueryCondition());
        } else {
            resultCondition = getQueryCondition();
        }

        dataLoader.setCondition(resultCondition);
    }

    @Override
    public boolean isAutoApply() {
        return autoApply;
    }

    @Override
    public void setAutoApply(boolean autoApply) {
        if (this.autoApply != autoApply) {
            this.autoApply = autoApply;

            getOwnFilterComponents().forEach(filterComponent ->
                    filterComponent.setAutoApply(autoApply));
        }
    }

    @Override
    public void apply() {
        if (dataLoader != null && autoApply) {
            dataLoader.load();
        }
    }

    @Override
    public LogicalCondition getQueryCondition() {
        updateQueryCondition();
        return queryCondition;
    }

    protected void updateQueryCondition() {
        queryCondition = new LogicalCondition(WrapperUtils.convertToLogicalConditionType(operation));

        for (FilterComponent ownComponent : getOwnFilterComponents()) {
            queryCondition.add(ownComponent.getQueryCondition());
        }
    }

    @Override
    public void add(FilterComponent filterComponent) {
        if (dataLoader != filterComponent.getDataLoader()) {
            throw new IllegalArgumentException("The data loader of child component must be the same as the owner " +
                    "GroupFilter component");
        }

        filterComponent.setConditionModificationDelegated(true);
        filterComponent.setAutoApply(isAutoApply());
        getQueryCondition().add(filterComponent.getQueryCondition());

        if (ownFilterComponentsOrder == null) {
            ownFilterComponentsOrder = new ArrayList<>();
        }
        ownFilterComponentsOrder.add(filterComponent);

        updateConditionsLayout();
        addFilterComponentToConditionsLayout(conditionsLayout, filterComponent);

        if (filterComponent instanceof PropertyFilter) {
            ((PropertyFilter<?>) filterComponent).addOperationChangeListener(operationChangeEvent -> apply());
        }

        if (!isConditionModificationDelegated()) {
            updateDataLoaderCondition();
        }

        fireFilterComponentsChanged();
    }

    protected void updateConditionsLayout() {
        if (conditionsLayout == null && isAnyFilterComponentVisible()) {
            conditionsLayout = createConditionsLayout();
            initConditionsLayout(conditionsLayout);
            getContent().add(conditionsLayout);
        } else if (conditionsLayout != null && !isAnyFilterComponentVisible()) {
            conditionsLayout.removeAll();
            getContent().remove(conditionsLayout);
            conditionsLayout = null;
        }
    }

    @Override
    public void remove(FilterComponent filterComponent) {
        if (getOwnFilterComponents().contains(filterComponent)) {
            ownFilterComponentsOrder.remove(filterComponent);

            if (ownFilterComponentsOrder.isEmpty()) {
                ownFilterComponentsOrder = null;
            }

            FormLayout.FormItem formItem = null;
            if (filterComponent instanceof SingleFilterComponent) {
                getDataLoader().removeParameter(((SingleFilterComponent<?>) filterComponent).getParameterName());
                formItem = filterComponentFormItemMap.remove(filterComponent);
            }

            if (formItem != null) {
                conditionsLayout.remove(formItem);
            } else {
                conditionsLayout.remove((Component) filterComponent);
            }

            updateConditionsLayout();

            if (!isConditionModificationDelegated()) {
                updateDataLoaderCondition();
            }
        } else {
            getOwnFilterComponents().stream()
                    .filter(ownComponent -> ownComponent instanceof LogicalFilterComponent)
                    .map(ownComponent -> (LogicalFilterComponent<?>) ownComponent)
                    .forEach(childLogicalFilterComponent -> childLogicalFilterComponent.remove(filterComponent));
        }

        fireFilterComponentsChanged();
    }

    @Override
    public void removeAll() {
        ownFilterComponentsOrder = null;
        updateConditionsLayout();

        if (!isConditionModificationDelegated()) {
            updateDataLoaderCondition();
        }

        fireFilterComponentsChanged();
    }

    @Nullable
    protected Label getSummaryComponent() {
        return summaryComponent;
    }

    @Nullable
    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(@Nullable String summaryText) {
        if (!Objects.equals(this.summaryText, summaryText)) {
            this.summaryText = summaryText;
            updateSummaryText();
        }
    }

    protected void updateSummaryText() {
        String summaryText = Strings.isNullOrEmpty(this.summaryText)
                ? logicalFilterSupport.getOperationText(operation, operationTextVisible)
                : this.summaryText;

        if (Strings.isNullOrEmpty(summaryText)) {
            if (summaryComponent != null) {
                getContent().remove(summaryComponent);
                summaryComponent = null;
            }
        } else {
            if (summaryComponent == null) {
                summaryComponent = createSummaryComponent();
                initSummaryComponent(summaryComponent);
                getContent().addComponentAsFirst(summaryComponent);
            }
            summaryComponent.setText(summaryText);
        }
    }

    protected Label createSummaryComponent() {
        return uiComponents.create(Label.class);
    }

    protected void initSummaryComponent(Label summaryComponent) {
        // hook
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public void setOperation(Operation operation) {
        if (this.operation != operation) {
            this.operation = operation;

            updateSummaryText();

            if (!isConditionModificationDelegated()) {
                updateDataLoaderCondition();
            }
        }
    }

    @Override
    public boolean isOperationTextVisible() {
        return operationTextVisible;
    }

    @Override
    public void setOperationTextVisible(boolean operationTextVisible) {
        if (this.operationTextVisible != operationTextVisible) {
            this.operationTextVisible = operationTextVisible;

            updateSummaryText();
        }
    }

    @Override
    public List<ResponsiveStep> getResponsiveSteps() {
        return Collections.unmodifiableList(responsiveSteps);
    }

    @Override
    public void setResponsiveSteps(List<ResponsiveStep> steps) {
        this.responsiveSteps = steps;

        if (conditionsLayout != null) {
            conditionsLayout.setResponsiveSteps(WrapperUtils.convertToFormLayoutResponsiveStep(responsiveSteps));
        }
    }

    @Override
    public List<FilterComponent> getOwnFilterComponents() {
        return ownFilterComponentsOrder != null
                ? Collections.unmodifiableList(ownFilterComponentsOrder)
                : Collections.emptyList();
    }

    @Override
    public List<FilterComponent> getFilterComponents() {
        List<FilterComponent> components = new ArrayList<>();
        for (FilterComponent ownComponent : getOwnFilterComponents()) {
            components.add(ownComponent);
            if (ownComponent instanceof LogicalFilterComponent) {
                components.addAll(((LogicalFilterComponent<?>) ownComponent).getFilterComponents());
            }
        }

        return components;
    }

    @Internal
    @Override
    public boolean isConditionModificationDelegated() {
        return conditionModificationDelegated;
    }

    @Internal
    @Override
    public void setConditionModificationDelegated(boolean conditionModificationDelegated) {
        this.conditionModificationDelegated = conditionModificationDelegated;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addFilterComponentsChangeListener(
            ComponentEventListener<FilterComponentsChangeEvent<GroupFilter>> listener) {
        return getEventBus().addListener(FilterComponentsChangeEvent.class, ((ComponentEventListener) listener));
    }

    protected void fireFilterComponentsChanged() {
        FilterComponentsChangeEvent<GroupFilter> event = new FilterComponentsChangeEvent<>(this, false);
        getEventBus().fireEvent(event);
    }
}
