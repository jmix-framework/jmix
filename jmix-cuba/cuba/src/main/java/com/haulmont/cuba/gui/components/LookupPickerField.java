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

package com.haulmont.cuba.gui.components;

import com.google.common.reflect.TypeToken;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Entity;
import io.jmix.ui.component.EntityComboBox;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> entity
 * @deprecated Use {@link EntityComboBox} instead
 */
@Deprecated
public interface LookupPickerField<V extends Entity> extends EntityComboBox<V>, LookupField<V>, PickerField<V> {

    String NAME = "lookupPickerField";

    static <T extends Entity> TypeToken<LookupPickerField<T>> of(Class<T> valueClass) {
        return new TypeToken<LookupPickerField<T>>() {};
    }

    /**
     * Use this method to enable items refreshing in component after closing lookup window.
     *
     * @deprecated Override LookupAction if needed instead of using this option.
     */
    @Deprecated
    void setRefreshOptionsOnLookupClose(boolean refresh);
    boolean isRefreshOptionsOnLookupClose();
}
