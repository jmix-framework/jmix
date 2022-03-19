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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.web.theme.HaloTheme;
import com.vaadin.server.Resource;
import io.jmix.core.Entity;
import io.jmix.ui.component.impl.EntityPickerImpl;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public class WebPickerField<V extends Entity> extends EntityPickerImpl<V> implements PickerField<V> {

    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> iconProvider;

    @Deprecated
    @Override
    public PickerField.LookupAction addLookupAction() {
        PickerField.LookupAction action = PickerField.LookupAction.create(this);
        addAction(action);
        return action;
    }

    @Override
    @Deprecated
    public PickerField.ClearAction addClearAction() {
        PickerField.ClearAction action = PickerField.ClearAction.create(this);
        addAction(action);
        return action;
    }

    @Deprecated
    @Override
    public PickerField.OpenAction addOpenAction() {
        PickerField.OpenAction action = PickerField.OpenAction.create(this);
        addAction(action);
        return action;
    }

    @Override
    public void addValidator(Consumer<? super V> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<V> validator) {
        removeValidator(validator::accept);
    }

    @Override
    protected String formatValue(@Nullable V value) {
        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(value);
        }

        return super.formatValue(value);
    }

    @Override
    public void setOptionCaptionProvider(Function<? super V, String> optionCaptionProvider) {
        this.optionCaptionProvider = optionCaptionProvider;
    }

    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    @Override
    public void setOptionIconProvider(Function<? super V, String> optionIconProvider) {
        if (this.iconProvider != optionIconProvider) {
            this.iconProvider = optionIconProvider;

            component.setStyleName("jmix-has-field-icon", optionIconProvider != null);
            component.getField().setStyleName(HaloTheme.TEXTFIELD_INLINE_ICON, optionIconProvider != null);

            component.setIconGenerator(this::generateOptionIcon);
        }
    }

    protected Resource generateOptionIcon(V item) {
        if (iconProvider == null) {
            return null;
        }

        String resourceId;
        try {
            resourceId = iconProvider.apply(item);
        } catch (Exception e) {
            LoggerFactory.getLogger(WebPickerField.class)
                    .warn("Error invoking optionIconProvider apply method", e);
            return null;
        }

        return getIconResource(resourceId);
    }

    @Override
    public Function<? super V, String> getOptionIconProvider() {
        return iconProvider;
    }

    @Override
    public void setFieldIconProvider(@Nullable Function<? super V, String> iconProvider) {
        setOptionIconProvider(iconProvider);
    }

    @Nullable
    @Override
    public Function<? super V, String> getFieldIconProvider() {
        return getOptionIconProvider();
    }
}
