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

import com.haulmont.cuba.gui.components.SuggestionField;
import io.jmix.ui.component.impl.SuggestionFieldImpl;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Deprecated
public class WebSuggestionField<V> extends SuggestionFieldImpl<V> implements SuggestionField<V> {

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

    @Override
    public void addValidator(Consumer<? super V> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<V> validator) {
        removeValidator(validator::accept);
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
