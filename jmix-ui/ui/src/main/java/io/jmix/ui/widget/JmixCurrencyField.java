/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget;

import com.vaadin.server.*;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.TextField;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class JmixCurrencyField extends CustomField<String> {

    protected static final String CURRENCYFIELD_STYLENAME = "jmix-currencyfield";
    protected static final String CURRENCYFIELD_LAYOUT_STYLENAME = "jmix-currencyfield-layout";
    protected static final String CURRENCY_STYLENAME = "jmix-currencyfield-currency";
    protected static final String CURRENCYFIELD_TEXT_STYLENAME = "jmix-currencyfield-text";
    protected static final String CURRENCYFIELD_LABEL_LEFT_POSITION_STYLENAME =
            CURRENCYFIELD_STYLENAME + "-label-left-position";

    protected static final String CURRENCY_VISIBLE = "currency-visible";
    protected static final String IE9_INPUT_WRAP_STYLENAME = "ie9-input-wrap";

    protected CssLayout container;
    protected CssLayout ie9InputWrapper;
    protected JmixTextField textField;
    protected JmixLabel currencyLabel;

    protected String currency;
    protected boolean showCurrencyLabel = true;
    protected CurrencyLabelPosition currencyLabelPosition = CurrencyLabelPosition.RIGHT;

    public JmixCurrencyField() {
        this.textField = new JmixTextField();

        init();

        initTextField();
        initCurrencyLabel();
        initLayout();

        updateCurrencyLabelVisibility();
    }

    protected void init() {
        setPrimaryStyleName(CURRENCYFIELD_STYLENAME);
        setSizeUndefined();
    }

    protected void initTextField() {
        textField.addStyleName(CURRENCYFIELD_TEXT_STYLENAME);
        textField.setWidth("100%");
        textField.setValueChangeMode(ValueChangeMode.BLUR);

        textField.addValueChangeListener(event -> markAsDirty());
    }

    protected void initCurrencyLabel() {
        currencyLabel = new JmixLabel();
        // enables to set to the table-cell element width by content
        currencyLabel.setWidth("1px");
        currencyLabel.setHeight("100%");
        currencyLabel.addStyleName(CURRENCY_STYLENAME);
    }

    protected void initLayout() {
        container = new CssLayout();
        container.setSizeFull();
        container.setPrimaryStyleName(CURRENCYFIELD_LAYOUT_STYLENAME);

        container.addComponent(currencyLabel);

        if (useWrapper()) {
            ie9InputWrapper = new CssLayout(textField);
            ie9InputWrapper.setSizeFull();
            ie9InputWrapper.setPrimaryStyleName(IE9_INPUT_WRAP_STYLENAME);

            container.addComponent(ie9InputWrapper);
        } else {
            container.addComponent(textField);
        }

        setFocusDelegate(textField);
    }

    protected boolean useWrapper() {
        Page current = Page.getCurrent();
        if (current != null) {
            WebBrowser browser = current.getWebBrowser();
            return browser != null &&
                    (browser.isIE() && browser.getBrowserMajorVersion() <= 10 || browser.isSafari());
        } else {
            return false;
        }
    }

    public boolean getShowCurrencyLabel() {
        return showCurrencyLabel;
    }

    public void setShowCurrencyLabel(boolean showCurrency) {
        this.showCurrencyLabel = showCurrency;

        updateCurrencyLabelVisibility();
    }

    @Nullable
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(@Nullable String currency) {
        this.currency = currency;
        currencyLabel.setValue(currency);

        updateCurrencyLabelVisibility();
    }

    protected void updateCurrencyLabelVisibility() {
        boolean currencyVisible = StringUtils.isNotEmpty(currency) && showCurrencyLabel;

        currencyLabel.setVisible(currencyVisible);

        if (currencyVisible) {
            container.addStyleName(CURRENCY_VISIBLE);
        } else {
            container.removeStyleName(CURRENCY_VISIBLE);
        }
    }

    public CurrencyLabelPosition getCurrencyLabelPosition() {
        return currencyLabelPosition;
    }

    public void setCurrencyLabelPosition(CurrencyLabelPosition currencyLabelPosition) {
        container.removeStyleName(this.currencyLabelPosition.name().toLowerCase());
        this.currencyLabelPosition = currencyLabelPosition;
        container.addStyleName(currencyLabelPosition.name().toLowerCase());

        container.removeComponent(currencyLabel);
        if (CurrencyLabelPosition.LEFT == currencyLabelPosition) {
            container.addComponent(currencyLabel, 0);
        } else {
            container.addComponent(currencyLabel, 1);
        }

        if (currencyLabelPosition == CurrencyLabelPosition.LEFT) {
            addStyleName(CURRENCYFIELD_LABEL_LEFT_POSITION_STYLENAME);
        } else {
            removeStyleName(CURRENCYFIELD_LABEL_LEFT_POSITION_STYLENAME);
        }
    }

    @Override
    protected Component initContent() {
        return container;
    }

    @Override
    public void focus() {
        textField.focus();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        textField.setTabIndex(tabIndex);
    }

    @Override
    public int getTabIndex() {
        return textField.getTabIndex();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        textField.setReadOnly(readOnly);
    }

    @Override
    protected void doSetValue(String value) {
        textField.setValue(value);
    }

    @Override
    public String getValue() {
        return textField.getValue();
    }

    @Override
    public void setRequiredError(String requiredMessage) {
        textField.setRequiredError(requiredMessage);

        markAsDirty();
    }

    @Override
    public String getRequiredError() {
        return textField.getRequiredError();
    }

    @Override
    public ErrorMessage getErrorMessage() {
        ErrorMessage superError = super.getErrorMessage();
        if (!textField.isReadOnly() && textField.isRequiredIndicatorVisible() && textField.isEmpty()) {
            ErrorMessage error = AbstractErrorMessage.getErrorMessageForException(
                    new com.vaadin.v7.data.Validator.EmptyValueException(getRequiredError()));
            if (error != null) {
                return new CompositeErrorMessage(superError, error);
            }
        }
        return superError;
    }

    @Override
    public void setComponentError(ErrorMessage componentError) {
        textField.setComponentError(componentError);
    }

    @Override
    public ErrorMessage getComponentError() {
        return textField.getComponentError();
    }

    public TextField getInternalComponent() {
        return textField;
    }

    @Override
    public void setComponentErrorProvider(Supplier<ErrorMessage> componentErrorProvider) {
        textField.setComponentErrorProvider(componentErrorProvider);
    }

    @Override
    public Supplier<ErrorMessage> getComponentErrorProvider() {
        return textField.getComponentErrorProvider();
    }

    @Override
    public void setStyleName(String style) {
        super.setStyleName(getCurrencyLabelPosition() == CurrencyLabelPosition.LEFT
                ? style + CURRENCYFIELD_LABEL_LEFT_POSITION_STYLENAME
                : style);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName()
                .replace(CURRENCYFIELD_LABEL_LEFT_POSITION_STYLENAME, ""));
    }
}
