/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.data.aggregation;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.flowui.data.aggregation.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component("flowui_Aggregations")
public class Aggregations {

    protected DatatypeRegistry datatypeRegistry;

    @SuppressWarnings("rawtypes")
    protected Map<Class, Aggregation> datatypeToAggregation;

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Nullable
    public <T> Aggregation<T> get(Class<T> clazz) {
        if (datatypeToAggregation == null) {
            registerDatatypes();
        }

        //noinspection unchecked
        return datatypeToAggregation.get(clazz);
    }

    protected void registerDatatypes() {
        datatypeToAggregation = new HashMap<>();
        register(datatypeRegistry.get(BigDecimal.class), new BigDecimalAggregation());
        register(datatypeRegistry.get(BigInteger.class), new BigIntegerAggregation());
        register(datatypeRegistry.get(Long.class), new LongAggregation());
        register(datatypeRegistry.get(Integer.class), new LongAggregation());
        register(datatypeRegistry.get(Short.class), new LongAggregation());
        register(datatypeRegistry.get(Double.class), new DoubleAggregation());
        register(datatypeRegistry.get(Float.class), new DoubleAggregation());
        register(datatypeRegistry.get(Date.class), new DateAggregation());
        register(datatypeRegistry.get(Boolean.class), new BasicAggregation<>(Boolean.class));
        register(datatypeRegistry.get(byte[].class), new BasicAggregation<>(byte[].class));
        register(datatypeRegistry.get(String.class), new BasicAggregation<>(String.class));
        register(datatypeRegistry.get(UUID.class), new BasicAggregation<>(UUID.class));
    }

    protected <T> void register(Datatype<?> datatype, Aggregation<T> aggregation) {
        datatypeToAggregation.put(datatype.getJavaClass(), aggregation);
    }
}
