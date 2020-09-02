/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.gui.definition.edit;

import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.entity.EntityValues;

import java.util.function.Consumer;

/**
 *
 */
class LinkedWithPropertyNewOptionHandler implements Consumer<String> {
    protected Datasource datasource;
    protected String fieldName;

    public static LinkedWithPropertyNewOptionHandler handler(Datasource datasource, String fieldName) {
        return new LinkedWithPropertyNewOptionHandler(datasource, fieldName);
    }

    public LinkedWithPropertyNewOptionHandler(Datasource datasource, String fieldName) {
        this.datasource = datasource;
        this.fieldName = fieldName;
    }

    @Override
    public void accept(String caption) {
        EntityValues.setValue(datasource.getItem(), fieldName, caption);
    }
}
