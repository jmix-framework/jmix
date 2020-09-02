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

package io.jmix.reports.entity.pivottable;

import io.jmix.core.UuidProvider;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Lob;

@ModelObject(name = "report$PivotTableAggregation")
public class PivotTableAggregation extends BaseUuidEntity {

    @ModelProperty
    protected AggregationMode mode;

    @ModelProperty
    protected String caption;

    @Lob
    @ModelProperty
    protected String function;

    public PivotTableAggregation() {
        id = UuidProvider.createUuid();
    }

    public PivotTableAggregation(AggregationMode mode) {
        id = UuidProvider.createUuid();
        setMode(mode);
    }

    public AggregationMode getMode() {
        return mode;
    }

    public void setMode(AggregationMode mode) {
        this.mode = mode;
        if (StringUtils.isEmpty(caption)) {
            setCaption(AppBeans.get(Messages.class).getMessage(getClass(), "AggregationMode." + mode.toString()));
        }
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
