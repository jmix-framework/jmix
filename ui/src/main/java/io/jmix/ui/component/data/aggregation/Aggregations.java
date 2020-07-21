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

import io.jmix.core.AppBeans;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.ui.component.data.aggregation.impl.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// vaadin8 convert to bean
public class Aggregations {
    private static final Aggregations instance;

    static {
        DatatypeRegistry datatypeRegistry = AppBeans.get(DatatypeRegistry.NAME);
        instance = new Aggregations();
        instance.register(datatypeRegistry.get(BigDecimal.class), new BigDecimalAggregation());
        instance.register(datatypeRegistry.get(Integer.class), new LongAggregation());
        instance.register(datatypeRegistry.get(Long.class), new LongAggregation());
        instance.register(datatypeRegistry.get(Double.class), new DoubleAggregation());
        instance.register(datatypeRegistry.get(Date.class), new DateAggregation());
        instance.register(datatypeRegistry.get(Boolean.class), new BasicAggregation<>(Boolean.class));
        instance.register(datatypeRegistry.get(byte[].class), new BasicAggregation<>(byte[].class));
        instance.register(datatypeRegistry.get(String.class), new BasicAggregation<>(String.class));
        instance.register(datatypeRegistry.get(UUID.class), new BasicAggregation<>(UUID.class));
    }

    public static Aggregations getInstance() {
        return instance;
    }

    private Map<Class, Aggregation> aggregationByDatatype;

    private Aggregations() {
        aggregationByDatatype = new HashMap<>();
    }

    protected <T> void register(Datatype datatype, Aggregation<T> aggregation) {
        aggregationByDatatype.put(datatype.getJavaClass(), aggregation);
    }

    @SuppressWarnings("unchecked")
    public static <T> Aggregation<T> get(Class<T> clazz) {
        return getInstance().aggregationByDatatype.get(clazz);
    }
}
