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

import com.vaadin.server.Resource;
import io.jmix.core.JmixEntity;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.theme.HaloTheme;
import io.jmix.ui.widget.JmixPickerField;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Strings.nullToEmpty;

public class WebEntityPicker<V extends JmixEntity> extends WebValuePicker<V> implements EntityPicker<V> {

    protected Metadata metadata;

    protected MetaClass metaClass;

    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> iconProvider;

    public WebEntityPicker() {
    }

    protected JmixPickerField<V> createComponent() {
        return new JmixPickerField<>();
    }

    @Override
    public void setValue(@Nullable V value) {
        checkValueType(value);
        super.setValue(value);
    }

    @Override
    public void setValueFromUser(@Nullable V value) {
        checkValueType(value);
        super.setValueFromUser(value);
    }

    protected void checkValueType(@Nullable V value) {
        if (value != null) {
            MetaClass metaClass = getMetaClass();
            if (metaClass == null) {
                throw new IllegalStateException("Neither metaClass nor valueSource is set for PickerField");
            }

            Class<?> fieldClass = metaClass.getJavaClass();
            Class<?> valueClass = value.getClass();
            if (!fieldClass.isAssignableFrom(valueClass)) {
                throw new IllegalArgumentException(
                        String.format("Could not set value with class %s to field with class %s",
                                fieldClass.getCanonicalName(),
                                valueClass.getCanonicalName())
                );
            }
        }
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    protected String formatValue(@Nullable V value) {
        if (optionCaptionProvider != null) {
            return nullToEmpty(optionCaptionProvider.apply(value));
        }

        return super.formatValue(value);
    }

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource) valueSource).getMetaPropertyPath().getMetaProperty();
            return metaProperty.getRange().asClass();
        } else {
            return metaClass;
        }
    }

    @Override
    public void setMetaClass(@Nullable MetaClass metaClass) {
        ValueSource<V> valueSource = getValueSource();
        if (valueSource != null) {
            throw new IllegalStateException("ValueSource is not null");
        }
        this.metaClass = metaClass;
    }

    @Override
    public void setOptionCaptionProvider(@Nullable Function<? super V, String> optionCaptionProvider) {
        this.optionCaptionProvider = optionCaptionProvider;
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    @Override
    public void setOptionIconProvider(@Nullable Function<? super V, String> optionIconProvider) {
        if (this.iconProvider != optionIconProvider) {
            this.iconProvider = optionIconProvider;

            component.setStyleName("c-has-field-icon", optionIconProvider != null);
            component.getField().setStyleName(HaloTheme.TEXTFIELD_INLINE_ICON, optionIconProvider != null);

            component.setIconGenerator(this::generateOptionIcon);
        }
    }

    @Nullable
    protected Resource generateOptionIcon(@Nullable V item) {
        if (iconProvider == null) {
            return null;
        }

        String resourceId;
        try {
            resourceId = iconProvider.apply(item);
        } catch (Exception e) {
            LoggerFactory.getLogger(WebEntityPicker.class)
                    .warn("Error invoking optionIconProvider apply method", e);
            return null;
        }

        return getIconResource(resourceId);
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionIconProvider() {
        return iconProvider;
    }

    @Override
    public void setLookupSelectHandler(Consumer selectHandler) {
        // do nothing
    }

    @Override
    public Collection getLookupSelectedItems() {
        V value = getValue();
        if (value == null) {
            return Collections.emptyList();
        }

        return Collections.singleton(value);
    }
}
