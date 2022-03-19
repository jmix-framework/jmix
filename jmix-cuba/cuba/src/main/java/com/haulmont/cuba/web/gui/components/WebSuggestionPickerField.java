/*
 * Copyright 2020 Haulmont.
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
import com.haulmont.cuba.gui.components.SuggestionPickerField;
import com.haulmont.cuba.web.theme.HaloTheme;
import com.vaadin.server.Resource;
import io.jmix.core.Entity;
import io.jmix.ui.component.impl.EntitySuggestionFieldImpl;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

@Deprecated
public class WebSuggestionPickerField<V extends Entity> extends EntitySuggestionFieldImpl<V>
        implements SuggestionPickerField<V> {

    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> iconProvider;

    protected EnterActionHandler enterActionHandler;
    protected ArrowDownActionHandler arrowDownActionHandler;

    @Nullable
    @Override
    public V getValue() {
        V value = super.getValue();

        //noinspection unchecked
        return value instanceof OptionWrapper
                ? ((OptionWrapper<V>) value).getValue()
                : value;
    }

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

    @Override
    public EnterActionHandler getEnterActionHandler() {
        return enterActionHandler;
    }

    @Override
    public void setEnterActionHandler(EnterActionHandler enterActionHandler) {
        this.enterActionHandler = enterActionHandler;

        if (enterActionHandler != null) {
            if (getEnterPressHandler() == null) {
                setEnterPressHandler(this::onEnterPressHandler);
            }
        } else {
            setEnterPressHandler(null);
        }
    }

    @Override
    public ArrowDownActionHandler getArrowDownActionHandler() {
        return arrowDownActionHandler;
    }

    @Override
    public void setArrowDownActionHandler(ArrowDownActionHandler arrowDownActionHandler) {
        this.arrowDownActionHandler = arrowDownActionHandler;

        if (arrowDownActionHandler != null) {
            if (getArrowDownHandler() == null) {
                setArrowDownHandler(this::onArrowDownHandler);
            }
        } else {
            setArrowDownHandler(null);
        }
    }

    protected void onEnterPressHandler(EnterPressEvent event) {
        if (enterActionHandler != null) {
            enterActionHandler.onEnterKeyPressed(event.getText());
        }
    }

    protected void onArrowDownHandler(ArrowDownEvent event) {
        if (arrowDownActionHandler != null) {
            arrowDownActionHandler.onArrowDownKeyPressed(event.getText());
        }
    }
}
