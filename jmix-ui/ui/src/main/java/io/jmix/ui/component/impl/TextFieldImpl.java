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
package io.jmix.ui.component.impl;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ValueChangeMode;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.ValueConversionException;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.widget.JmixTextField;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

public class TextFieldImpl<V> extends AbstractField<JmixTextField, String, V>
        implements TextField<V>, InitializingBean {

    protected Datatype<V> datatype;
    protected Formatter<? super V> formatter;
    protected Locale locale;

    protected boolean trimming = true;

    protected Registration enterShortcutRegistration;
    protected String conversionErrorMessage;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public TextFieldImpl() {
        this.component = createComponent();

        attachValueChangeListener(this.component);
    }

    @Autowired
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.locale = currentAuthentication.getLocale();
    }

    protected JmixTextField createComponent() {
        return new JmixTextField();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(JmixTextField component) {
        component.setValueChangeMode(ValueChangeMode.BLUR);
    }

    @Override
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            DataAwareComponentsTools dataAwareComponentsTools = applicationContext.getBean(DataAwareComponentsTools.class);
            EntityValueSource entityValueSource = (EntityValueSource) valueSource;

            dataAwareComponentsTools.setupCaseConversion(this, entityValueSource);
            dataAwareComponentsTools.setupMaxLength(this, entityValueSource);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String convertToPresentation(@Nullable V modelValue) throws ConversionException {
        // Vaadin TextField does not permit `null` value

        if (formatter != null) {
            return nullToEmpty(formatter.apply(modelValue));
        }

        if (datatype != null) {
            return nullToEmpty(datatype.format(modelValue, locale));
        }

        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            Range range = entityValueSource.getMetaPropertyPath().getRange();
            if (range.isDatatype()) {
                Datatype<V> propertyDataType = range.asDatatype();
                return nullToEmpty(propertyDataType.format(modelValue, locale));
            } else {
                setEditable(false);
                if (modelValue == null)
                    return "";

                if (range.isClass()) {
                    MetadataTools metadataTools = applicationContext.getBean(MetadataTools.class);
                    if (range.getCardinality().isMany()) {
                        return ((Collection<Object>) modelValue).stream()
                                .map(metadataTools::getInstanceName)
                                .collect(Collectors.joining(", "));
                    } else {
                        return metadataTools.getInstanceName(modelValue);
                    }
                } else if (range.isEnum()) {
                    Messages messages = applicationContext.getBean(Messages.class);
                    return messages.getMessage((Enum) modelValue);
                }
            }
        }

        return nullToEmpty(super.convertToPresentation(modelValue));
    }

    @Nullable
    @Override
    protected V convertToModel(@Nullable String componentRawValue) throws ConversionException {
        String value = emptyToNull(componentRawValue);

        if (isTrimming()) {
            value = StringUtils.trimToNull(value);
        }

        if (datatype != null) {
            try {
                return datatype.parse(value, locale);
            } catch (ValueConversionException e) {
                throw new ConversionException(e.getLocalizedMessage(), e);
            } catch (ParseException e) {
                throw new ConversionException(getConversionErrorMessageInternal(), e);
            }
        }

        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            Datatype<V> propertyDataType = entityValueSource.getMetaPropertyPath().getRange().asDatatype();
            try {
                return propertyDataType.parse(value, locale);
            } catch (ValueConversionException e) {
                throw new ConversionException(e.getLocalizedMessage(), e);
            } catch (ParseException e) {
                throw new ConversionException(getConversionErrorMessageInternal(), e);
            }
        }

        return super.convertToModel(value);
    }

    @Override
    public void setConversionErrorMessage(@Nullable String conversionErrorMessage) {
        this.conversionErrorMessage = conversionErrorMessage;
    }

    @Nullable
    @Override
    public String getConversionErrorMessage() {
        return conversionErrorMessage;
    }

    protected String getConversionErrorMessageInternal() {
        String customErrorMessage = getConversionErrorMessage();
        if (StringUtils.isNotEmpty(customErrorMessage)) {
            return customErrorMessage;
        }

        Datatype<V> datatype = this.datatype;

        if (datatype == null
                && valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {

            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            datatype = entityValueSource.getMetaPropertyPath().getRange().asDatatype();
        }

        if (datatype != null) {
            String msg = getDatatypeConversionErrorMsg(datatype);
            if (StringUtils.isNotEmpty(msg)) {
                return msg;
            }
        }

        return applicationContext.getBean(Messages.class)
                .getMessage("databinding.conversion.error");
    }

    @Override
    protected void componentValueChanged(String prevComponentValue, String newComponentValue, boolean isUserOriginated) {
        if (isUserOriginated) {
            fireTextChangeEvent(newComponentValue);
        }

        super.componentValueChanged(prevComponentValue, newComponentValue, isUserOriginated);
    }

    @Override
    public boolean isEmpty() {
        V value = getValue();
        return value instanceof String
                ? Strings.isNullOrEmpty((String) value)
                : TextField.super.isEmpty();
    }

    @Nullable
    @Override
    public Datatype<V> getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(Datatype<V> datatype) {
        dataAwareComponentsTools.checkValueSourceDatatypeMismatch(datatype, getValueSource());

        this.datatype = datatype;
    }

    @Override
    public CaseConversion getCaseConversion() {
        return CaseConversion.valueOf(component.getCaseConversion().name());
    }

    @Override
    public void setCaseConversion(CaseConversion caseConversion) {
        io.jmix.ui.widget.CaseConversion widgetCaseConversion =
                io.jmix.ui.widget.CaseConversion.valueOf(caseConversion.name());
        component.setCaseConversion(widgetCaseConversion);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Formatter<V> getFormatter() {
        return (Formatter<V>) formatter;
    }

    @Override
    public void setFormatter(@Nullable Formatter<? super V> formatter) {
        this.formatter = formatter;
    }

    @Override
    public int getMaxLength() {
        return component.getMaxLength();
    }

    @Override
    public void setMaxLength(int maxLength) {
        component.setMaxLength(maxLength);
    }

    @Override
    public boolean isTrimming() {
        return trimming;
    }

    @Override
    public void setTrimming(boolean trimming) {
        this.trimming = trimming;
    }

    @Nullable
    @Override
    public String getInputPrompt() {
        return component.getPlaceholder();
    }

    @Override
    public void setInputPrompt(@Nullable String inputPrompt) {
        component.setPlaceholder(inputPrompt);
    }

    @Override
    public void setCursorPosition(int position) {
        component.setCursorPosition(position);
    }

    @Override
    public String getRawValue() {
        return component.getValue();
    }

    @Override
    public void selectAll() {
        component.selectAll();
    }

    @Override
    public void setSelectionRange(int pos, int length) {
        component.setSelection(pos, length);
    }

    protected void fireTextChangeEvent(String newComponentValue) {
        // call it before value change due to compatibility with the previous versions
        TextChangeEvent event = new TextChangeEvent(this, newComponentValue, component.getCursorPosition());
        publish(TextChangeEvent.class, event);
    }

    @Override
    public void setTextChangeTimeout(int timeout) {
        component.setValueChangeTimeout(timeout);
    }

    @Override
    public Subscription addTextChangeListener(Consumer<TextChangeEvent> listener) {
        return getEventHub().subscribe(TextChangeEvent.class, listener);
    }

    @Override
    public int getTextChangeTimeout() {
        return component.getValueChangeTimeout();
    }

    @Override
    public TextChangeEventMode getTextChangeEventMode() {
        return WrapperUtils.toTextChangeEventMode(component.getValueChangeMode());
    }

    @Override
    public void setTextChangeEventMode(TextChangeEventMode mode) {
        component.setValueChangeMode(WrapperUtils.toVaadinValueChangeEventMode(mode));
    }

    @Override
    public Subscription addEnterPressListener(Consumer<EnterPressEvent> listener) {
        if (enterShortcutRegistration == null) {
            ShortcutListener enterShortcutListener = new ShortcutListenerDelegate("", KeyCode.ENTER, null)
                    .withHandler((sender, target) -> {
                        EnterPressEvent event = new EnterPressEvent(TextFieldImpl.this);
                        publish(EnterPressEvent.class, event);
                    });
            enterShortcutRegistration = component.addShortcutListener(enterShortcutListener);
        }

        getEventHub().subscribe(EnterPressEvent.class, listener);

        return () -> internalRemoveEnterPressListener(listener);
    }

    protected void internalRemoveEnterPressListener(Consumer<EnterPressEvent> listener) {
        unsubscribe(EnterPressEvent.class, listener);

        if (!hasSubscriptions(EnterPressEvent.class)) {
            enterShortcutRegistration.remove();
            enterShortcutRegistration = null;
        }
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public void commit() {
        super.commit();
    }

    @Override
    public void discard() {
        super.discard();
    }

    @Override
    public boolean isBuffered() {
        return super.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        super.setBuffered(buffered);
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }

    @Override
    public void setHtmlName(@Nullable String htmlName) {
        component.setHtmlName(htmlName);
    }

    @Nullable
    @Override
    public String getHtmlName() {
        return component.getHtmlName();
    }
}
