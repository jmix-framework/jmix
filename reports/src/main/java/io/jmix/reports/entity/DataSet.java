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
package io.jmix.reports.entity;

import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.View;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.HashMap;
import java.util.Map;

@ModelObject(name = "report$DataSet")
@SystemLevel
public class DataSet extends BaseUuidEntity implements ReportQuery {

    public static final String ENTITY_PARAM_NAME = "entityParamName";
    public static final String LIST_ENTITIES_PARAM_NAME = "listEntitiesParamName";
    public static final String DATA_STORE_PARAM_NAME = "dataStore";
    public static final String JSON_SOURCE_TYPE = "jsonSourceType";
    public static final String JSON_SOURCE_TEXT = "jsonSourceText";
    public static final String JSON_PATH_QUERY = "jsonPathQuery";
    public static final String JSON_INPUT_PARAMETER = "jsonSourceInputParameter";

    private static final long serialVersionUID = -3706206933129963303L;

    protected View view;
    @ModelProperty
    protected String name;
    @ModelProperty
    protected Boolean useExistingView = false;
    @ModelProperty
    protected String viewName;
    @ModelProperty
    protected String text;
    @ModelProperty
    protected Integer type;
    @ModelProperty
    protected Integer jsonSourceType = JsonSourceType.GROOVY_SCRIPT.getId();
    @ModelProperty
    protected String jsonSourceText;
    @ModelProperty
    protected String jsonPathQuery;
    @ModelProperty
    protected ReportInputParameter jsonSourceInputParameter;
    @ModelProperty
    protected String entityParamName;
    @ModelProperty
    protected String listEntitiesParamName;
    @ModelProperty
    protected BandDefinition bandDefinition;
    @ModelProperty
    protected String linkParameterName;
    @ModelProperty
    protected String dataStore;
    @ModelProperty
    protected Boolean processTemplate;

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public Boolean getUseExistingView() {
        return useExistingView;
    }

    public void setUseExistingView(Boolean useExistingView) {
        this.useExistingView = useExistingView;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
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
}