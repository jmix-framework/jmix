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

package io.jmix.reports.app;

import io.jmix.core.FetchPlan;
import io.jmix.core.Sort;
import io.jmix.core.querycondition.Condition;

import java.io.Serializable;
import java.util.Map;

/**
 * Report parameter for lazy data loading in core module
 */
public class ParameterPrototype implements Serializable {

    protected static final long serialVersionUID = 2654220919728705511L;

    protected String paramName;

    protected String queryString;

    protected String fetchPlanName;

    protected String metaClassName;

    protected Integer firstResult;

    protected Integer maxResults;

    protected Map<String, Object> queryParams;

    protected Condition condition;

    protected Sort sort;

    protected FetchPlan fetchPlan;

    public ParameterPrototype(String paramName) {
        this.paramName = paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getFetchPlanName() {
        return fetchPlanName;
    }

    public void setFetchPlanName(String fetchPlanName) {
        this.fetchPlanName = fetchPlanName;
    }

    public String getMetaClassName() {
        return metaClassName;
    }

    public void setMetaClassName(String metaClassName) {
        this.metaClassName = metaClassName;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    public void setFetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
    }
}