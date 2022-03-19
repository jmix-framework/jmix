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

package io.jmix.rest.impl.service.filter;

import com.google.common.base.Strings;
import io.jmix.core.metamodel.model.MetaClass;

/**
 */
public class RestFilterPropertyCondition implements RestFilterCondition {
    private String propertyName;
    private MetaClass metaClass;
    private String queryParamName;
    private Object value;
    private RestFilterOp operator;

    @Override
    public String toJpql() {
        //queryParamName will be null for operators that don't require value, e.g. "notEmpty"
        return "{E}." + propertyName + " " + operator.forJpql() +
                (Strings.isNullOrEmpty(queryParamName) ? "" : (" :" + queryParamName));
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    public String getQueryParamName() {
        return queryParamName;
    }

    public void setQueryParamName(String queryParamName) {
        this.queryParamName = queryParamName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public RestFilterOp getOperator() {
        return operator;
    }

    public void setOperator(RestFilterOp operator) {
        this.operator = operator;
    }
}
