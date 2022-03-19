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
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.ValueConversionException;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Locale;
import java.util.function.Consumer;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

public abstract class AbstractTextArea<T extends com.vaadin.ui.TextArea, V>
        extends AbstractField<T, String, V> implements TextArea<V> {

    protected Datatype<V> datatype;
    protected Locale locale;

    protected boolean trimming = true;

    protected String conversionErrorMessage;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    @Autowired
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    protected String convertToPresentation(@Nullable V modelValue) throws ConversionException {
        // Vaadin TextField does not permit `null` value

        if (datatype != null) {
            return nullToEmpty(datatype.format(modelValue, locale));
        }

        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            Datatype<V> propertyDataType = entityValueSource.getMetaPropertyPath().getRange().asDatatype();
            return nullToEmpty(propertyDataType.format(modelValue));
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
    public boolean isEmpty() {
        V value = getValue();
        return value instanceof String
                ? Strings.isNullOrEmpty((String) value)
                : TextArea.super.isEmpty();
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.locale = currentAuthentication.getLocale();
    }

    @Override
    protected void componentValueChanged(String prevComponentValue, String newComponentValue, boolean isUserOriginated) {
        if (isUserOriginated) {
            fireTextChangeEvent(newComponentValue);
        }

        super.componentValueChanged(prevComponentValue, newComponentValue, isUserOriginated);
    }

    protected void fireTextChangeEvent(String newComponentValue) {
        // call it before value change due to compatibility with the previous versions
        TextChangeEvent event = new TextChangeEvent(this, newComponentValue, component.getCursorPosition());
        publish(TextChangeEvent.class, event);
    }

    @Override
    public Subscription addTextChangeListener(Consumer<TextChangeEvent> listener) {
        return getEventHub().subscribe(TextChangeEvent.class, listener);
    }

    @Override
    public int getMaxLength() {
        return component.getMaxLength();
    }

    @Override
    public void setMaxLength(int value) {
        component.setMaxLength(value);
    }

    @Override
    public int getRows() {
        return component.getRows();
    }

    @Override
    public void setRows(int rows) {
        component.setRows(rows);
    }

    @Override
    public boolean isWordWrap() {
        return component.isWordWrap();
    }

    @Override
    public void setWordWrap(boolean wordWrap) {
        component.setWordWrap(wordWrap);
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
    public String getRawValue() {
        return component.getValue();
    }

    @Override
    public boolean isTrimming() {
        return trimming;
    }

    @Override
    public void setTrimming(boolean trimming) {
        this.trimming = trimming;
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
    public void selectAll() {
        component.selectAll();
    }

    @Override
    public void setSelectionRange(int pos, int length) {
        component.setSelection(pos, length);
    }

    @Override
    public void setTextChangeTimeout(int timeout) {
        component.setValueChangeTimeout(timeout);
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
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            DataAwareComponentsTools dataAwareComponentsTools = applicationContext.getBean(DataAwareComponentsTools.class);
            EntityValueSource entityValueSource = (EntityValueSource) valueSource;

            dataAwareComponentsTools.setupCaseConversion(this, entityValueSource);
            dataAwareComponentsTools.setupMaxLength(this, entityValueSource);
        }
    }
}
