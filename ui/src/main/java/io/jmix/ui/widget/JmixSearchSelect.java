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

package io.jmix.ui.widget;

import io.jmix.ui.widget.client.searchselect.JmixSearchSelectState;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class JmixSearchSelect<V> extends JmixComboBox<V> {

    protected Consumer<String> filterHandler = null;

    public JmixSearchSelect() {
        setStyleName("jmix-searchselect");
    }

    @Override
    protected JmixSearchSelectState getState() {
        return (JmixSearchSelectState) super.getState();
    }

    @Override
    protected JmixSearchSelectState getState(boolean markAsDirty) {
        return (JmixSearchSelectState) super.getState(markAsDirty);
    }

    @Override
    protected void filterChanged(String filter) {
        if (filterHandler != null) {
            filterHandler.accept(filter);
        }
    }

    @Override
    public boolean isTextInputAllowed() {
        return false;
    }

    @Nullable
    @Override
    public NewItemProvider<V> getNewItemProvider() {
        return null;
    }

    @Override
    public void setNewItemProvider(NewItemProvider<V> newItemProvider) {
        if (newItemProvider != null) {
            throw new UnsupportedOperationException();
        }
    }

    public void setFilterHandler(Consumer<String> filterHandler) {
        this.filterHandler = filterHandler;
    }
}