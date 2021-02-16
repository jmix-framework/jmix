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

import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.compatibility.LookupFieldNewOptionHandlerAdapter;
import io.jmix.ui.component.impl.ComboBoxImpl;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

@Deprecated
public class WebLookupField<V> extends ComboBoxImpl<V> implements LookupField<V> {

    protected V nullOption;

    @Override
    public V getNullOption() {
        return nullOption;
    }

    @Override
    public void setNullOption(V nullOption) {
        this.nullOption = nullOption;
        setNullSelectionCaption(generateItemCaption(nullOption));
    }

    @Override
    public boolean isNewOptionAllowed() {
        return component.getNewItemHandler() != null;
    }

    @Override
    public void setNewOptionAllowed(boolean newItemAllowed) {
        if (newItemAllowed
                && component.getNewItemHandler() == null) {
            component.setNewItemHandler(this::onEnterPressed);
        }

        if (!newItemAllowed
                && component.getNewItemHandler() != null) {
            component.setNewItemHandler(null);
        }
    }

    @Nullable
    @Override
    public Consumer<String> getNewOptionHandler() {
        Consumer<EnterPressEvent> enterPressHandler = getEnterPressHandler();
        if (enterPressHandler instanceof LookupFieldNewOptionHandlerAdapter) {
            return ((LookupFieldNewOptionHandlerAdapter) enterPressHandler).getNewOptionHandler();
        }

        return null;
    }

    @Override
    public void setNewOptionHandler(@Nullable Consumer<String> newOptionHandler) {
        setEnterPressHandler(new LookupFieldNewOptionHandlerAdapter(newOptionHandler));
    }

    @Override
    public void setOptionIconProvider(Class<V> optionClass, Function<? super V, String> optionIconProvider) {
        setOptionIconProvider(optionIconProvider);
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
    public Collection<V> getLookupSelectedItems() {
        return Collections.singleton(getValue());
    }

    @Override
    public void setLookupSelectHandler(Consumer<Collection<V>> selectHandler) {
        // do nothing
    }
}
