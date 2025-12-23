/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowuidata.dateinterval.model;

import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;

/**
 * DTO class describes a date interval with a custom date range.
 */
public class CustomDateInterval implements BaseDateInterval {

    protected String propertyPath;
    protected String datatype;
    protected final Object start;
    protected final Object end;

    public CustomDateInterval(String propertyPath, String datatype, Object start, Object end) {
        this.propertyPath = propertyPath;
        this.datatype = datatype;
        this.start = start;
        this.end = end;
    }

    @Override
    public Type getType() {
        return Type.CUSTOM;
    }

    /**
     * @return property name that is used to generate the parameter name in the query
     */
    public String getPropertyPath() {
        return propertyPath;
    }

    /**
     * @return datatype ID of the property
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * @return date interval start date
     */
    public Object getStart() {
        return start;
    }

    /**
     * @return date interval end date
     */
    public Object getEnd() {
        return end;
    }

    @Override
    public String apply(String property) {
        return "@dateBetween(%s.%s, :%s, :%s)".formatted("%s", "%s",
                getStartParameterName(), getEndParameterName()
        );
    }

    /**
     * @return parameter name of the start date in a query
     */
    public String getStartParameterName() {
        return propertyPath + "_start";
    }

    /**
     * @return parameter name of the end date in a query
     */
    public String getEndParameterName() {
        return propertyPath + "_end";
    }
}
