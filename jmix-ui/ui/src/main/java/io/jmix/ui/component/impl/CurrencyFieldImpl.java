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

import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.annotation.CurrencyValue;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.CurrencyField;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.ValueConversionException;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.widget.JmixCurrencyField;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

public class CurrencyFieldImpl<V extends Number> extends AbstractField<JmixCurrencyField, String, V>
        implements CurrencyField<V> {

    protected Locale locale;
    protected Datatype<V> datatype;
    protected Datatype<V> defaultDatatype;
    protected String conversionErrorMessage;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public CurrencyFieldImpl() {
        component = createComponent();
        initComponent(component);

        attachValueChangeListener(component);
    }

    protected JmixCurrencyField createComponent() {
        return new JmixCurrencyField();
    }

    protected void initComponent(JmixCurrencyField component) {
        component.setCurrencyLabelPosition(toWidgetLabelPosition(CurrencyLabelPosition.RIGHT));
    }

    @Autowired
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        //noinspection unchecked
        this.defaultDatatype = (Datatype<V>) datatypeRegistry.get(BigDecimal.class);
    }

    @Override
    protected void attachValueChangeListener(JmixCurrencyField component) {
        component.getInternalComponent()
                .addValueChangeListener(event ->
                        componentValueChanged(event.getOldValue(), event.getValue(), event.isUserOriginated()));
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.locale = currentAuthentication.getLocale();
    }

    @Override
    protected String convertToPresentation(@Nullable V modelValue) throws ConversionException {
        Datatype<V> datatype = getDatatypeInternal();
        // Vaadin TextField does not permit `null` value
        if (datatype != null) {
            return nullToEmpty(datatype.format(modelValue, locale));
        }

        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            Datatype<V> propertyDataType = entityValueSource.getMetaPropertyPath().getRange().asDatatype();
            return nullToEmpty(propertyDataType.format(modelValue, locale));
        }

        return nullToEmpty(super.convertToPresentation(modelValue));
    }

    @Nullable
    @Override
    protected V convertToModel(@Nullable String componentRawValue) throws ConversionException {
        String value = StringUtils.trimToNull(emptyToNull(componentRawValue));

        Datatype<V> datatype = getDatatypeInternal();
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
                return propertyDataType.parse(componentRawValue, locale);
            } catch (ValueConversionException e) {
                throw new ConversionException(e.getLocalizedMessage(), e);
            } catch (ParseException e) {
                throw new ConversionException(getConversionErrorMessageInternal(), e);
            }
        }

        return super.convertToModel(componentRawValue);
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
    public void setCurrency(@Nullable String currency) {
        component.setCurrency(currency);
    }

    @Nullable
    @Override
    public String getCurrency() {
        return component.getCurrency();
    }

    @Override
    public void setShowCurrencyLabel(boolean showCurrencyLabel) {
        component.setShowCurrencyLabel(showCurrencyLabel);
    }

    @Override
    public boolean getShowCurrencyLabel() {
        return component.getShowCurrencyLabel();
    }

    @Override
    public void setCurrencyLabelPosition(CurrencyLabelPosition currencyLabelPosition) {
        Preconditions.checkNotNullArgument(currencyLabelPosition);

        component.setCurrencyLabelPosition(toWidgetLabelPosition(currencyLabelPosition));
    }

    @Override
    public CurrencyLabelPosition getCurrencyLabelPosition() {
        return fromWidgetLabelPosition(component.getCurrencyLabelPosition());
    }

    @Override
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            MetaPropertyPath metaPropertyPath = ((EntityValueSource) valueSource).getMetaPropertyPath();
            if (metaPropertyPath.getRange().isDatatype()) {
                Datatype datatype = metaPropertyPath.getRange().asDatatype();
                if (!Number.class.isAssignableFrom(datatype.getJavaClass())) {
                    throw new IllegalArgumentException("CurrencyField doesn't support Datatype with class: " + datatype.getJavaClass());
                }
            } else {
                throw new IllegalArgumentException("CurrencyField doesn't support properties with association");
            }

            MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

            Object annotation = metaProperty.getAnnotations()
                    .get(CurrencyValue.class.getName());
            if (annotation == null) {
                return;
            }

            //noinspection unchecked
            Map<String, Object> annotationProperties = (Map<String, Object>) annotation;

            String currencyName = (String) annotationProperties.get("currency");
            component.setCurrency(currencyName);

            String labelPosition = ((io.jmix.core.entity.annotation.CurrencyLabelPosition) annotationProperties.get("labelPosition")).name();
            setCurrencyLabelPosition(CurrencyLabelPosition.valueOf(labelPosition));
        }
    }

    @Override
    public void setDatatype(Datatype<V> datatype) {
        Preconditions.checkNotNullArgument(datatype);
        dataAwareComponentsTools.checkValueSourceDatatypeMismatch(datatype, getValueSource());
        if (!Number.class.isAssignableFrom(datatype.getJavaClass())) {
            throw new IllegalArgumentException("CurrencyField doesn't support Datatype with class: " + datatype.getJavaClass());
        }

        this.datatype = datatype;
    }

    @Nullable
    @Override
    public Datatype<V> getDatatype() {
        return datatype;
    }

    @Nullable
    protected Datatype<V> getDatatypeInternal() {
        if (datatype != null) {
            return datatype;
        }
        return valueBinding == null ? defaultDatatype : null;
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

    protected io.jmix.ui.widget.CurrencyLabelPosition toWidgetLabelPosition(CurrencyLabelPosition labelPosition) {
        return io.jmix.ui.widget.CurrencyLabelPosition.valueOf(labelPosition.name());
    }

    protected CurrencyLabelPosition fromWidgetLabelPosition(io.jmix.ui.widget.CurrencyLabelPosition wLabelPosition) {
        return CurrencyLabelPosition.valueOf(wLabelPosition.name());
    }
}
