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
package io.jmix.ui.component.data.aggregation;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.ui.component.data.aggregation.impl.BasicAggregation;
import io.jmix.ui.component.data.aggregation.impl.BigDecimalAggregation;
import io.jmix.ui.component.data.aggregation.impl.DateAggregation;
import io.jmix.ui.component.data.aggregation.impl.DoubleAggregation;
import io.jmix.ui.component.data.aggregation.impl.LongAggregation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component("ui_Aggregations")
public class Aggregations {

    protected DatatypeRegistry datatypeRegistry;

    protected Map<Class, Aggregation> aggregationByDatatype;

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> Aggregation<T> get(Class<T> clazz) {
        if (aggregationByDatatype == null) {
            registerDatatypes();
        }
        return aggregationByDatatype.get(clazz);
    }

    protected void registerDatatypes() {
        aggregationByDatatype = new HashMap<>();
        register(datatypeRegistry.get(BigDecimal.class), new BigDecimalAggregation());
        register(datatypeRegistry.get(Integer.class), new LongAggregation());
        register(datatypeRegistry.get(Long.class), new LongAggregation());
        register(datatypeRegistry.get(Double.class), new DoubleAggregation());
        register(datatypeRegistry.get(Date.class), new DateAggregation());
        register(datatypeRegistry.get(Boolean.class), new BasicAggregation<>(Boolean.class));
        register(datatypeRegistry.get(byte[].class), new BasicAggregation<>(byte[].class));
        register(datatypeRegistry.get(String.class), new BasicAggregation<>(String.class));
        register(datatypeRegistry.get(UUID.class), new BasicAggregation<>(UUID.class));
    }

    protected <T> void register(Datatype datatype, Aggregation<T> aggregation) {
        aggregationByDatatype.put(datatype.getJavaClass(), aggregation);
    }
}
