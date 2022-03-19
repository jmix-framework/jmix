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

import com.vaadin.shared.ui.ContentMode;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.widget.JmixLabel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

public class LabelImpl<V> extends AbstractViewComponent<com.vaadin.ui.Label, String, V> implements Label<V> {

    protected MetadataTools metadataTools;

    protected Formatter<? super V> formatter;

    public LabelImpl() {
        component = createComponent();
        initComponent(component);
    }

    protected com.vaadin.ui.Label createComponent() {
        return new JmixLabel();
    }

    protected void initComponent(com.vaadin.ui.Label component) {
        component.setSizeUndefined();
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    protected void setValueToPresentation(@Nullable String value) {
        if (hasValidationError()) {
            setValidationError(null);
        }

        component.setValue(value);
    }

    @Nullable
    protected String convertToPresentation(@Nullable V modelValue) {
        String presentationValue;
        if (formatter != null) {
            presentationValue = formatter.apply(modelValue);
        } else if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            MetaProperty metaProperty = entityValueSource.getMetaPropertyPath().getMetaProperty();
            presentationValue = metadataTools.format(modelValue, metaProperty);
        } else {
            presentationValue = metadataTools.format(modelValue);
        }

        return isHtmlEnabled()
                ? sanitize(presentationValue)
                : presentationValue;
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
    public boolean isHtmlEnabled() {
        return component.getContentMode() == ContentMode.HTML;
    }

    @Override
    public void setHtmlEnabled(boolean htmlEnabled) {
        component.setContentMode(htmlEnabled ? ContentMode.HTML : ContentMode.TEXT);
    }

    @Override
    public String getRawValue() {
        return component.getValue();
    }
}
