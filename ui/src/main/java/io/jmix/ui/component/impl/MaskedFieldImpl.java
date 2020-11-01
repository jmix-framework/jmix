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

import com.google.common.collect.ImmutableList;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ValueChangeMode;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.MaskedField;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.ValueConversionException;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.widget.JmixMaskedTextField;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

public class MaskedFieldImpl<V> extends AbstractField<JmixMaskedTextField, String, V>
        implements MaskedField<V>, InitializingBean {

    protected static final char PLACE_HOLDER = '_';
    protected static final List<Character> MASK_SYMBOLS = ImmutableList.of('#', 'U', 'L', '?', 'A', '*', 'H', 'h', '~');

    protected Registration enterShortcutRegistration;
    protected String nullRepresentation;

    protected Datatype<V> datatype;
    protected Locale locale;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public MaskedFieldImpl() {
        this.component = createComponent();

        attachValueChangeListener(component);
    }

    @Autowired
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.locale = currentAuthentication.getLocale();
    }

    @Override
    public void setMask(String mask) {
        component.setMask(mask);
        updateNullRepresentation();
    }

    protected void updateNullRepresentation() {
        StringBuilder valueBuilder = new StringBuilder();
        String mask = getMask();

        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);

            if (c == '\'') {
                valueBuilder.append(mask.charAt(++i));
            } else if (MASK_SYMBOLS.contains(c)) {
                valueBuilder.append(PLACE_HOLDER);
            } else {
                valueBuilder.append(c);
            }
        }
        nullRepresentation = valueBuilder.toString();
    }

    protected String getNullRepresentation() {
        return nullRepresentation;
    }

    @Override
    public boolean isEmpty() {
        return MaskedField.super.isEmpty()
                || Objects.equals(getValue(), getNullRepresentation());
    }

    @Override
    public String getMask() {
        return component.getMask();
    }

    @Override
    public void setValueMode(ValueMode mode) {
        component.setMaskedMode(mode == ValueMode.MASKED);
    }

    @Override
    public ValueMode getValueMode() {
        return component.isMaskedMode() ? ValueMode.MASKED : ValueMode.CLEAR;
    }

    @Override
    public boolean isSendNullRepresentation() {
        return component.isSendNullRepresentation();
    }

    @Override
    public void setSendNullRepresentation(boolean sendNullRepresentation) {
        component.setSendNullRepresentation(sendNullRepresentation);
    }

    @Override
    public String getRawValue() {
        return component.getValue();
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
    protected String convertToPresentation(@Nullable V modelValue) throws ConversionException {
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
            }
        }

        return nullToEmpty(super.convertToPresentation(modelValue));
    }

    @Nullable
    @Override
    protected V convertToModel(@Nullable String componentRawValue) throws ConversionException {
        String value = emptyToNull(componentRawValue);

        if (datatype != null) {
            try {
                return datatype.parse(value, locale);
            } catch (ValueConversionException e) {
                throw new ConversionException(e.getLocalizedMessage(), e);
            } catch (ParseException e) {
                throw new ConversionException(getConversionErrorMessage(), e);
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
                throw new ConversionException(getConversionErrorMessage(), e);
            }
        }

        return super.convertToModel(value);
    }

    protected String getConversionErrorMessage() {
        Messages messages = applicationContext.getBean(Messages.class);
        return messages.getMessage("databinding.conversion.error");
    }

    protected JmixMaskedTextField createComponent() {
        return new JmixMaskedTextField();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(JmixMaskedTextField component) {
        component.setValueChangeMode(ValueChangeMode.BLUR);
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
    public Subscription addEnterPressListener(Consumer<EnterPressEvent> listener) {
        if (enterShortcutRegistration == null) {
            ShortcutListener enterShortcutListener = new ShortcutListenerDelegate("enter", KeyCode.ENTER, null)
                    .withHandler((sender, target) -> {
                        EnterPressEvent event = new EnterPressEvent(MaskedFieldImpl.this);
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
}
