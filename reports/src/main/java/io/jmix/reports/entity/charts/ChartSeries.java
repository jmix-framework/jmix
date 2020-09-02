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

package io.jmix.reports.entity.charts;

import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.entity.annotation.SystemLevel;

@ModelObject(name = "report$ChartSeries")
@SystemLevel
public class ChartSeries extends BaseUuidEntity {

    private static final long serialVersionUID = -3205550424620740535L;

    @ModelProperty
    protected String name;
    @ModelProperty(mandatory = true)
    protected String type;
    @ModelProperty(mandatory = true)
    protected String valueField;
    @ModelProperty
    protected String colorField;
    @ModelProperty
    protected Integer order;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SeriesType getType() {
        return SeriesType.fromId(type);
    }

    public void setType(SeriesType type) {
        this.type = type != null ? type.getId() : null;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getColorField() {
        return colorField;
    }

    public void setColorField(String colorField) {
        this.colorField = colorField;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
