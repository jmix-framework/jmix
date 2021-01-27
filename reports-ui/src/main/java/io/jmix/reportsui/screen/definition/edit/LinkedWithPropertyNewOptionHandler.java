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

package io.jmix.reportsui.screen.definition.edit;

import io.jmix.core.entity.EntityValues;
import io.jmix.ui.model.InstanceContainer;

import java.util.function.Consumer;

/**
 *
 */
class LinkedWithPropertyNewOptionHandler implements Consumer<String> {
    protected InstanceContainer datasource;
    protected String fieldName;

    public static LinkedWithPropertyNewOptionHandler handler(InstanceContainer datasource, String fieldName) {
        return new LinkedWithPropertyNewOptionHandler(datasource, fieldName);
    }

    public LinkedWithPropertyNewOptionHandler(InstanceContainer datasource, String fieldName) {
        this.datasource = datasource;
        this.fieldName = fieldName;
    }

    @Override
    public void accept(String caption) {
        EntityValues.setValue(datasource.getItem(), fieldName, caption);
    }
}
