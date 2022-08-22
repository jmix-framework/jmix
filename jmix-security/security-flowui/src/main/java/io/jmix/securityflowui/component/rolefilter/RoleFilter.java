package io.jmix.securityflowui.component.rolefilter;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.data.items.EnumDataProvider;
import io.jmix.securityflowui.model.BaseRoleModel;
import io.jmix.securityflowui.model.RoleSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleFilter extends Composite<Details> implements InitializingBean {

    private FormLayout layout;
    private TextField nameFilter;
    private TextField codeFilter;
    private JmixSelect<RoleSource> sourceFilter;

    protected Messages messages;
    protected UiComponents uiComponents;

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initNameFilter();
        initCodeFilter();
        initSourceFilter();
        initLayout();
        initRootComponent();
    }

    protected void initNameFilter() {
        nameFilter = uiComponents.create(TextField.class);
        nameFilter.setLabel(messages.getMessage(BaseRoleModel.class, "BaseRoleModel.name"));
        nameFilter.addValueChangeListener(this::onFilterFieldValueChange);
    }

    protected void initCodeFilter() {
        codeFilter = uiComponents.create(TextField.class);
        codeFilter.setLabel(messages.getMessage(BaseRoleModel.class, "BaseRoleModel.code"));
        codeFilter.addValueChangeListener(this::onFilterFieldValueChange);
    }

    @SuppressWarnings("unchecked")
    protected void initSourceFilter() {
        sourceFilter = uiComponents.create(JmixSelect.class);
        sourceFilter.setLabel(messages.getMessage(BaseRoleModel.class, "BaseRoleModel.source"));
        sourceFilter.setEmptySelectionAllowed(true);
        sourceFilter.setItems(new EnumDataProvider<>(RoleSource.class));
        sourceFilter.addValueChangeListener(this::onFilterFieldValueChange);
    }

    protected void initLayout() {
        layout = uiComponents.create(FormLayout.class);
        layout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("50em", 3)
        );

        layout.add(nameFilter, codeFilter, sourceFilter);
    }

    protected void initRootComponent() {
        getContent().setSummaryText(messages.getMessage(RoleFilter.class, "summaryText"));
        getContent().setWidthFull();
        getContent().addContent(layout);
    }

    public boolean isNameFilterVisible() {
        return nameFilter.isVisible();
    }

    public void setNameFilterVisible(boolean visible) {
        nameFilter.setVisible(visible);
    }

    public boolean isCodeFilterVisible() {
        return nameFilter.isVisible();
    }

    public void setCodeFilterVisible(boolean visible) {
        codeFilter.setVisible(visible);
    }

    public boolean isSourceFilterVisible() {
        return sourceFilter.isVisible();
    }

    public void setSourceFilterVisible(boolean visible) {
        sourceFilter.setVisible(visible);
    }

    protected void onFilterFieldValueChange(AbstractField.ComponentValueChangeEvent<?, ?> event) {
        RoleFilterChangeEvent filterChangeEvent = new RoleFilterChangeEvent(this,
                nameFilter.getValue(), codeFilter.getValue(), sourceFilter.getValue());
        getEventBus().fireEvent(filterChangeEvent);
    }

    public Registration addRoleFilterChangeListener(ComponentEventListener<RoleFilterChangeEvent> listener) {
        return getEventBus().addListener(RoleFilterChangeEvent.class, listener);
    }
}
