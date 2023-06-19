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
package io.jmix.reports.entity;

import io.jmix.reports.yarg.structure.ReportQuery;
import io.jmix.core.CopyingSystemState;
import io.jmix.core.FetchPlan;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import jakarta.persistence.Id;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JmixEntity(name = "report_DataSet", annotatedPropertiesOnly = true)
@SystemLevel
public class DataSet implements ReportQuery, CopyingSystemState<DataSet> {

    public static final String ENTITY_PARAM_NAME = "entityParamName";
    public static final String LIST_ENTITIES_PARAM_NAME = "listEntitiesParamName";
    public static final String DATA_STORE_PARAM_NAME = "dataStore";
    public static final String JSON_SOURCE_TYPE = "jsonSourceType";
    public static final String JSON_SOURCE_TEXT = "jsonSourceText";
    public static final String JSON_PATH_QUERY = "jsonPathQuery";
    public static final String JSON_INPUT_PARAMETER = "jsonSourceInputParameter";

    private static final long serialVersionUID = -3706206933129963303L;

    protected FetchPlan fetchPlan;
    @Id
    @JmixProperty
    @JmixGeneratedValue
    protected UUID id;
    @JmixProperty
    protected String name;
    @JmixProperty
    protected Boolean useExistingFetchPLan = false;
    @JmixProperty
    protected String fetchPlanName;
    @JmixProperty
    protected String text;
    @JmixProperty
    protected Integer type;
    @JmixProperty
    protected Integer jsonSourceType = JsonSourceType.GROOVY_SCRIPT.getId();
    @JmixProperty
    protected String jsonSourceText;
    @JmixProperty
    protected String jsonPathQuery;
    @JmixProperty
    protected ReportInputParameter jsonSourceInputParameter;
    @JmixProperty
    protected String entityParamName;
    @JmixProperty
    protected String listEntitiesParamName;
    @JmixProperty
    protected BandDefinition bandDefinition;
    @JmixProperty
    protected String linkParameterName;
    @JmixProperty
    protected String dataStore;
    @JmixProperty
    protected Boolean processTemplate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    public void setFetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
    }

    public Boolean getUseExistingFetchPLan() {
        return useExistingFetchPLan;
    }

    public void setUseExistingFetchPLan(Boolean useExistingFetchPLan) {
        this.useExistingFetchPLan = useExistingFetchPLan;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFetchPlanName() {
        return fetchPlanName;
    }

    public void setFetchPlanName(String fetchPlanName) {
        this.fetchPlanName = fetchPlanName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DataSetType getType() {
        return DataSetType.fromId(type);
    }

    public void setType(DataSetType type) {
        this.type = type != null ? type.getId() : null;
    }

    public String getEntityParamName() {
        return entityParamName;
    }

    public void setEntityParamName(String entityParamName) {
        this.entityParamName = entityParamName;
    }

    public String getListEntitiesParamName() {
        return listEntitiesParamName;
    }

    public void setListEntitiesParamName(String listEntitiesParamName) {
        this.listEntitiesParamName = listEntitiesParamName;
    }

    public BandDefinition getBandDefinition() {
        return bandDefinition;
    }

    public void setBandDefinition(BandDefinition bandDefinition) {
        this.bandDefinition = bandDefinition;
    }

    public String getDataStore() {
        return dataStore;
    }

    public void setDataStore(String dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String getLinkParameterName() {
        return linkParameterName;
    }

    public void setLinkParameterName(String linkParameterName) {
        this.linkParameterName = linkParameterName;
    }

    @Override
    public Boolean getProcessTemplate() {
        return processTemplate;
    }

    public void setProcessTemplate(Boolean processTemplate) {
        this.processTemplate = processTemplate;
    }

    @Override
    public String getScript() {
        return text;
    }

    @Override
    public String getLoaderType() {
        return getType().getCode();
    }

    public JsonSourceType getJsonSourceType() {
        return jsonSourceType != null ? JsonSourceType.fromId(jsonSourceType) : null;
    }

    public void setJsonSourceType(JsonSourceType jsonSourceType) {
        this.jsonSourceType = jsonSourceType != null ? jsonSourceType.getId() : null;
    }

    public String getJsonSourceText() {
        return jsonSourceText;
    }

    public void setJsonSourceText(String jsonSourceText) {
        this.jsonSourceText = jsonSourceText;
    }

    public ReportInputParameter getJsonSourceInputParameter() {
        return jsonSourceInputParameter;
    }

    public void setJsonSourceInputParameter(ReportInputParameter jsonSourceInputParameter) {
        this.jsonSourceInputParameter = jsonSourceInputParameter;
    }

    public String getJsonPathQuery() {
        return jsonPathQuery;
    }

    public void setJsonPathQuery(String jsonPathQuery) {
        this.jsonPathQuery = jsonPathQuery;
    }

    @Override
    public Map<String, Object> getAdditionalParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(ENTITY_PARAM_NAME, entityParamName);
        params.put(LIST_ENTITIES_PARAM_NAME, listEntitiesParamName);
        params.put(DATA_STORE_PARAM_NAME, dataStore);
        params.put(JSON_SOURCE_TYPE, getJsonSourceType());
        params.put(JSON_SOURCE_TEXT, jsonSourceText);
        params.put(JSON_PATH_QUERY, jsonPathQuery);
        params.put(JSON_INPUT_PARAMETER, jsonSourceInputParameter);

        return params;
    }

    @Override
    public void copyFrom(DataSet source) {
        this.fetchPlan = source.fetchPlan;
    }
}