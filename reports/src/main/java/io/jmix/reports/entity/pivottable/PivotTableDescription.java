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

package io.jmix.reports.entity.pivottable;

import com.google.gson.*;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.reports.converter.MetadataFieldsIgnoringGson;

import javax.persistence.Id;
import javax.persistence.Lob;
import java.lang.reflect.Type;
import java.util.*;

@JmixEntity(name = "report_PivotTableDescription")
@SystemLevel
public class PivotTableDescription {

    protected final static Gson gson;

    static {
        gson = MetadataFieldsIgnoringGson.create()
                .addIgnoringStrategy()
                .registerTypeAdapter(RendererType.class, new RendererTypeAdapter())
                .registerTypeAdapter(AggregationMode.class, new AggregationTypeAdapter())
                .build();
    }

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty(mandatory = true)
    protected String bandName;

    @JmixProperty(mandatory = true)
    protected RendererType defaultRenderer;

    @JmixProperty(mandatory = true)
    protected Set<RendererType> renderers = new HashSet<>();

    @JmixProperty
    protected PivotTableAggregation defaultAggregation;

    @JmixProperty
    protected Set<PivotTableAggregation> aggregations = new HashSet<>();

    @JmixProperty(mandatory = true)
    protected Set<PivotTableProperty> properties = new HashSet<>();

    @JmixProperty
    protected Boolean editable = false;

    @Lob
    @JmixProperty
    protected String filterFunction;

    @Lob
    @JmixProperty
    protected String sortersFunction;

    @JmixProperty
    protected String colorScaleGeneratorFunction;

    @JmixProperty
    protected Double c3Width;

    @JmixProperty
    protected Double c3Height;

    @JmixProperty
    protected List<String> rowsProperties = new ArrayList<>();

    @JmixProperty
    protected List<String> columnsProperties = new ArrayList<>();

    @JmixProperty
    protected List<String> aggregationProperties = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBandName() {
        return bandName;
    }

    public Set<PivotTableProperty> getProperties() {
        return properties;
    }

    public PivotTableAggregation getDefaultAggregation() {
        return defaultAggregation;
    }

    public Set<PivotTableAggregation> getAggregations() {
        return aggregations;
    }

    public Set<RendererType> getRenderers() {
        return renderers;
    }

    public RendererType getDefaultRenderer() {
        return defaultRenderer;
    }

    public Boolean getEditable() {
        return editable;
    }

    public Boolean isEditable() {
        return editable;
    }

    public String getFilterFunction() {
        return filterFunction;
    }

    public String getSortersFunction() {
        return sortersFunction;
    }

    public String getColorScaleGeneratorFunction() {
        return colorScaleGeneratorFunction;
    }

    public Double getC3Width() {
        return c3Width;
    }

    public Double getC3Height() {
        return c3Height;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }

    public void setRenderers(Set<RendererType> renderers) {
        this.renderers = renderers;
    }

    public void setColorScaleGeneratorFunction(String colorScaleGeneratorFunction) {
        this.colorScaleGeneratorFunction = colorScaleGeneratorFunction;
    }

    public void setDefaultRenderer(RendererType defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    public void setProperties(Set<PivotTableProperty> properties) {
        this.properties = properties;
    }

    public void setDefaultAggregation(PivotTableAggregation defaultAggregation) {
        this.defaultAggregation = defaultAggregation;
    }

    public void setAggregations(Set<PivotTableAggregation> aggregations) {
        this.aggregations = aggregations;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public void setFilterFunction(String filterFunction) {
        this.filterFunction = filterFunction;
    }

    public void setSortersFunction(String sortersFunction) {
        this.sortersFunction = sortersFunction;
    }

    public void setC3Width(Double c3Width) {
        this.c3Width = c3Width;
    }

    public void setC3Height(Double c3Height) {
        this.c3Height = c3Height;
    }

    public List<String> getRowsProperties() {
        return rowsProperties;
    }

    public void setRowsProperties(List<String> rowsProperties) {
        this.rowsProperties = rowsProperties;
    }

    public List<String> getColumnsProperties() {
        return columnsProperties;
    }

    public void setColumnsProperties(List<String> columnsProperties) {
        this.columnsProperties = columnsProperties;
    }

    public List<String> getAggregationProperties() {
        return aggregationProperties;
    }

    public void setAggregationProperties(List<String> aggregationProperties) {
        this.aggregationProperties = aggregationProperties;
    }

    public static String toJsonString(PivotTableDescription description) {
        return gson.toJson(description);
    }

    public static PivotTableDescription fromJsonString(String json) {
        try {
            return gson.fromJson(json, PivotTableDescription.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    protected static class RendererTypeAdapter implements JsonSerializer<RendererType>, JsonDeserializer<RendererType> {
        @Override
        public JsonElement serialize(RendererType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getId());
        }

        @Override
        public RendererType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return RendererType.fromId(json.getAsString());
        }
    }

    protected static class AggregationTypeAdapter implements JsonSerializer<AggregationMode>, JsonDeserializer<AggregationMode> {
        @Override
        public JsonElement serialize(AggregationMode src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getId());
        }

        @Override
        public AggregationMode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return AggregationMode.fromId(json.getAsString());
        }
    }
}
