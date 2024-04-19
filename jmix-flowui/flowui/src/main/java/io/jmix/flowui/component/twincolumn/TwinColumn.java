/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.component.twincolumn;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Set;

public class TwinColumn extends Composite<Div>
        implements HasEnabled, HasSize, SupportsValueSource<String>, HasHelper, HasAriaLabel, HasLabel, HasRequired, Focusable<Select<String>>, HasValueChangeMode, HasTheme,
        SupportsTypedValue<Select<String>, AbstractField.ComponentValueChangeEvent<Select<String>, Set<String>>, Collection<String>, Set<String>>,
        HasValueAndElement< AbstractField.ComponentValueChangeEvent<Select<String>, Set<String>>, Set<String> >, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();
    }

    @Override
    protected Div initContent() {
        Div root = super.initContent();
        //root.addClassName(USER_INDICATOR_CLASS_NAME);
        return root;
    }

    protected void initLayout() {
        /*contentWrapper = createContentWrapper();
        initContentWrapper(contentWrapper);
        getContent().setContent(contentWrapper);

        controlsLayout = createControlsLayout();
        initControlsLayout(controlsLayout);
        contentWrapper.add(controlsLayout);*/
    }

    protected VerticalLayout createContentWrapper() {
        return uiComponents.create(VerticalLayout.class);
    }

    protected void initContentWrapper(VerticalLayout contentWrapper) {
        /*contentWrapper.setPadding(false);
        contentWrapper.setClassName(FILTER_CONTENT_WRAPPER_CLASS_NAME);*/
    }

    protected HorizontalLayout createControlsLayout() {
        return uiComponents.create(HorizontalLayout.class);
    }

    protected void initControlsLayout(HorizontalLayout controlsLayout) {
        /*controlsLayout.setWidthFull();
        controlsLayout.setClassName(FILTER_CONTROLS_LAYOUT_CLASS_NAME);

        applyButton = createApplyButton();
        initApplyButton(applyButton);
        controlsLayout.add(applyButton);

        settingsButton = createSettingsButton();
        initSettingsButton(settingsButton);
        controlsLayout.add(settingsButton);

        addConditionButton = createAddConditionButton();
        initAddConditionButton(addConditionButton);
        controlsLayout.add(addConditionButton);*/
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
    }

    protected void initComponent() {
        initLayout();
    }

    public void setAllBtnEnabled(Boolean setAllBtnEnabled) {

    }

    public void setLeftColumnCaption(String leftColumnCaption) {

    }

    public void setRightColumnCaption(String rightColumnCaption) {

    }

    public void setReorderable(Boolean reorderable) {

    }

    public void setRows(Integer rows) {

    }

    @Override
    public ValueSource<String> getValueSource() {
        return null;
    }

    @Override
    public void setValueSource(ValueSource<String> valueSource) {

    }

    @Override
    public String getRequiredMessage() {
        return null;
    }

    @Override
    public void setRequiredMessage(String requiredMessage) {

    }

    @Override
    public ValueChangeMode getValueChangeMode() {
        return null;
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {

    }



    @Override
    public void setValue(Set<String> value) {

    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener listener) {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }

    @Override
    public Set<String> getTypedValue() {
        return null;
    }

    public void setTypedValue(Set<String> value) {

    }

    @Override
    public Registration addTypedValueChangeListener(ComponentEventListener listener) {
        return null;
    }

    @Override
    public Set<String> getValue() {
        return null;
    }

    @Override
    public void setTypedValue(Collection<String> value) {

    }
}
