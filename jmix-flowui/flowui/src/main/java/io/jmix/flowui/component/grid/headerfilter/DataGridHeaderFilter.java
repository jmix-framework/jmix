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

package io.jmix.flowui.component.grid.headerfilter;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.datagrid.HeaderPropertyFilterLayout;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

/**
 * A UI component used for displaying the filter in the column header. Modifies the standard header by
 * adding a button to open a pop-up overlay with a {@link PropertyFilter}.
 */
public class DataGridHeaderFilter extends Composite<HorizontalLayout>
        implements ApplicationContextAware, InitializingBean {

    public static final String ATTRIBUTE_JMIX_ROLE_NAME = "jmix-role";
    public static final String COLUMN_FILTER_BUTTON_ROLE = "column-filter-button";
    public static final String COLUMN_FILTER_POPUP_CLASSNAME = "column-filter-popup";
    public static final String COLUMN_FILTER_DIALOG_CLASSNAME = "column-filter-dialog";
    public static final String COLUMN_FILTER_FOOTER_SMALL_CLASSNAME = "column-filter-footer-small";
    public static final String COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME = "activated";

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;
    protected Messages messages;
    protected PropertyFilterSupport propertyFilterSupport;

    protected HeaderFilterContext context;

    protected JmixButton filterButton;
    protected Dialog overlay;
    @SuppressWarnings("rawtypes")
    protected PropertyFilter propertyFilter;
    protected Component headerComponent;

    protected Object appliedValue;
    protected PropertyFilter.Operation appliedOperation;

    public DataGridHeaderFilter(HeaderFilterContext context) {
        this.context = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        messages = applicationContext.getBean(Messages.class);
        propertyFilterSupport = applicationContext.getBean(PropertyFilterSupport.class);
    }

    protected void initComponent() {
        initPropertyFilter();
        initOverlay();
        initFunnelButton();

        setHeader(context.getHeaderComponent());
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout root = super.initContent();

        root.setPadding(false);
        root.setSpacing(false);
        root.setClassName(LumoUtility.Gap.XSMALL);
        // Padding for filterButton's focus-ring
        root.getStyle().set("padding-inline-end", "2px");
        root.getStyle().set("padding-block", "2px");

        return root;
    }

    /**
     * Sets a text to be displayed next to the filter button.
     *
     * @param labelText the text to be shown
     */
    public void setHeader(String labelText) {
        setHeader(new Span(labelText));
    }

    /**
     * Sets a component to be displayed next to the filter button.
     *
     * @param headerComponent the component to be shown
     */
    public void setHeader(Component headerComponent) {
        if (this.headerComponent == null) {
            getContent().addComponentAsFirst(headerComponent);
        } else {
            getContent().replace(this.headerComponent, headerComponent);
        }

        this.headerComponent = headerComponent;
    }

    /**
     * @return the column header component except the filter button
     */
    @Nullable
    public Component getHeader() {
        return this.headerComponent;
    }

    /**
     * @return {@link PropertyFilter} witch is displayed in the overlay
     */
    public PropertyFilter<?> getPropertyFilter() {
        return propertyFilter;
    }


    /**
     * @return {@code true} if the {@link PropertyFilter} component is applied
     * to the {@link DataLoader}, {@code false} otherwise
     */
    public boolean isFilterApplied() {
        return !overlay.isOpened()
                && propertyFilter.getValue() != null;
    }

    protected void initFunnelButton() {
        filterButton = uiComponents.create(JmixButton.class);
        filterButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ICON);
        filterButton.setIcon(VaadinIcon.FILTER.create());
        filterButton.setClassName(LumoUtility.TextColor.TERTIARY);
        filterButton.getElement().setAttribute(ATTRIBUTE_JMIX_ROLE_NAME, COLUMN_FILTER_BUTTON_ROLE);
        filterButton.addClickListener(__ -> {
            overlay.open();

            if (!isSmallDevice()) {
                overlay.getElement().executeJs(getOverlayPositionExpression(), overlay, filterButton);
            }
        });

        getContent().add(filterButton);
    }

    protected void initOverlay() {
        JmixButton clearButton = createClearFilterButton();

        HeaderPropertyFilterLayout headerPropertyFilterLayout = uiComponents.create(HeaderPropertyFilterLayout.class);
        headerPropertyFilterLayout.getContent().add(propertyFilter, clearButton);

        overlay = new Dialog(headerPropertyFilterLayout);
        overlay.addClassName(COLUMN_FILTER_DIALOG_CLASSNAME);

        if (!isSmallDevice()) {
            overlay.addClassName(COLUMN_FILTER_POPUP_CLASSNAME);
        } else {
            overlay.addClassName(COLUMN_FILTER_FOOTER_SMALL_CLASSNAME);
        }

        overlay.getFooter().add(createApplyButton(), createCancelButton());
        overlay.addOpenedChangeListener(this::onOpenOverlay);
        overlay.addDialogCloseActionListener(__ -> doCancel());
    }

    protected void initPropertyFilter() {
        propertyFilter = uiComponents.create(PropertyFilter.class);

        propertyFilter.setDataLoader(context.getDataLoader());
        propertyFilter.setProperty(context.getProperty());
        propertyFilter.setAutoApply(false);

        appliedOperation = propertyFilterSupport.getDefaultOperation(
                context.getMetaClass(), context.getProperty());
        propertyFilter.setOperation(appliedOperation);
        propertyFilter.setOperationEditable(true);
        propertyFilter.setParameterName(PropertyConditionUtils.generateParameterName(context.getProperty()));
        propertyFilter.setWidthFull();
    }

    protected void onOpenOverlay(Dialog.OpenedChangeEvent event) {
        if (event.isOpened()) {
            propertyFilter.focus();
        }
    }

    protected JmixButton createApplyButton() {
        JmixButton applyButton = uiComponents.create(JmixButton.class);
        applyButton.setIcon(VaadinIcon.CHECK.create());
        applyButton.setText(messages.getMessage("columnFilter.apply.text"));

        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyButton.addClickListener(__ -> {
            doApply();
            overlay.close();

            fireEvent(new ApplyEvent(this, propertyFilter, false));
        });

        if (isSmallDevice()) {
            applyButton.getStyle().set("flex-grow", "1");
        }

        return applyButton;
    }

    protected JmixButton createCancelButton() {
        JmixButton cancelButton = uiComponents.create(JmixButton.class);
        cancelButton.setIcon(VaadinIcon.BAN.create());
        cancelButton.setText(messages.getMessage("columnFilter.cancel.text"));

        cancelButton.addClickListener(__ -> doCancel());

        if (isSmallDevice()) {
            cancelButton.getStyle().set("flex-grow", "1");
        }

        return cancelButton;
    }

    /**
     * Applies the current value of the {@link PropertyFilter} to the loader
     */
    public void doApply() {
        propertyFilter.getDataLoader().load();
        appliedValue = propertyFilter.getValue();
        appliedOperation = propertyFilter.getOperation();

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME,
                propertyFilter.getValue() != null);
    }

    @SuppressWarnings({"unchecked"})
    protected void doCancel() {
        propertyFilter.setValue(appliedValue);
        propertyFilter.setOperation(appliedOperation);

        overlay.close();
    }

    protected JmixButton createClearFilterButton() {
        JmixButton clearButton = uiComponents.create(JmixButton.class);

        clearButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        clearButton.setIcon(VaadinIcon.ERASER.create());
        clearButton.addClickListener(__ -> propertyFilter.clear());

        return clearButton;
    }

    protected String getOverlayPositionExpression() {
        return "$0.$.overlay.$.overlay.style['top'] = $1.getBoundingClientRect().top + 'px';" +
                "const sum = $1.getBoundingClientRect().left + $1.getBoundingClientRect().width " +
                "+ $0.$.overlay.$.overlay.getBoundingClientRect().width;" +
                "if (sum < window.innerWidth) { " +
                "$0.$.overlay.$.overlay.style['left'] = $1.getBoundingClientRect().left + 'px'; " +
                "} else { " +
                "$0.$.overlay.$.overlay.style['right'] = window.innerWidth - $1.getBoundingClientRect().left " +
                "- $1.getBoundingClientRect().width + 'px';" +
                "}";
    }

    protected boolean isSmallDevice() {
        // magic number from vaadin-app-layout.js
        // '--vaadin-app-layout-touch-optimized' style property
        return UI.getCurrent().getInternals().getExtendedClientDetails().getScreenWidth() < 801;
    }

    public Registration addApplyListener(ComponentEventListener<ApplyEvent> eventListener) {
        return getEventBus().addListener(ApplyEvent.class, eventListener);
    }

    /**
     * An event fires when the current value of the {@link PropertyFilter} is applied to the loader.
     */
    public static class ApplyEvent extends ComponentEvent<DataGridHeaderFilter> {

        protected final PropertyFilter<?> appliedFilter;

        public ApplyEvent(DataGridHeaderFilter source, PropertyFilter<?> appliedFilter, boolean fromClient) {
            super(source, fromClient);

            this.appliedFilter = appliedFilter;
        }

        public PropertyFilter<?> getAppliedFilter() {
            return appliedFilter;
        }
    }

    public static class HeaderFilterContext {

        protected DataLoader dataLoader;
        protected MetaClass metaClass;
        protected String property;

        protected Component headerComponent;

        @SuppressWarnings({"rawtypes", "unchecked"})
        public HeaderFilterContext(Grid grid, DataGridColumn column) {
            if (grid.getDataProvider() instanceof ContainerDataGridItems<?> containerItems
                    && containerItems.getContainer() instanceof HasLoader hasLoaderContainer) {
                dataLoader = hasLoaderContainer.getLoader();
            } else {
                String message = String.format("%s is required for %s",
                        DataLoader.class.getSimpleName(),
                        DataGridHeaderFilter.class.getSimpleName());
                throw new IllegalArgumentException(message);
            }

            if (grid instanceof EnhancedDataGrid<?> enhancedDataGrid) {
                MetaPropertyPath propertyPath = enhancedDataGrid.getColumnMetaPropertyPath(column);
                Preconditions.checkNotNullArgument(propertyPath);

                metaClass = propertyPath.getMetaClass();
                property = propertyPath.getFirstPropertyName();
            }

            if (column.getHeaderText() != null) {
                headerComponent = new Span(column.getHeaderText());
            } else if (column.getHeaderComponent() != null) {
                headerComponent = column.getHeaderComponent();
            }
        }

        public DataLoader getDataLoader() {
            return dataLoader;
        }

        public MetaClass getMetaClass() {
            return metaClass;
        }

        public String getProperty() {
            return property;
        }

        public Component getHeaderComponent() {
            return headerComponent;
        }
    }
}
