/*
 * Copyright 2021 Haulmont.
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

package io.jmix.charts.model.dataset;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

@StudioElement(
        caption = "FieldMapping",
        xmlElement = "fieldMapping",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class FieldMapping extends AbstractChartObject {

    private static final long serialVersionUID = 2656776286364228750L;
    
    private String fromField;
    
    private String toField;

    public String getFromField() {
        return fromField;
    }

    @StudioProperty
    public FieldMapping setFromField(String fromField) {
        this.fromField = fromField;
        return this;
    }

    public String getToField() {
        return toField;
    }

    @StudioProperty
    public FieldMapping setToField(String toField) {
        this.toField = toField;
        return this;
    }
}