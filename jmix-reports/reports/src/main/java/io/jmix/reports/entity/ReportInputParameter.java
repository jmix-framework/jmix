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

import io.jmix.reports.delegate.ParameterTransformer;
import io.jmix.reports.delegate.ParameterValidator;
import io.jmix.reports.yarg.structure.DefaultValueProvider;
import io.jmix.reports.yarg.structure.ReportParameterWithDefaultValue;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.reports.util.MsgBundleTools;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Id;
import java.util.UUID;

@JmixEntity(name = "report_ReportInputParameter", annotatedPropertiesOnly = true)
@SystemLevel
@SuppressWarnings("unused")
public class ReportInputParameter implements ReportParameterWithDefaultValue {

    private static final long serialVersionUID = 6231014880104406246L;

    @Id
    @JmixProperty
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty
    protected Report report;

    @JmixProperty
    protected Integer type = ParameterType.TEXT.getId();

    @JmixProperty
    protected String name;

    @JmixProperty
    protected String localeNames;

    @JmixProperty
    protected String alias;

    @JmixProperty
    protected Integer position;

    @JmixProperty
    protected String entityMetaClass;

    @JmixProperty
    protected Boolean lookup = false;

    @JmixProperty
    protected String lookupJoin;

    @JmixProperty
    protected String lookupWhere;

    @JmixProperty
    protected String enumerationClass;

    @JmixProperty
    protected String screen;

    @JmixProperty
    protected Boolean required = false;

    @JmixProperty
    protected String defaultValue;

    @JmixProperty
    protected String parameterClassName;

    @JmixProperty
    protected String transformationScript;

    @JmixProperty
    protected String validationScript;

    @JmixProperty
    protected Boolean validationOn = false;

    @JmixProperty
    protected Integer predefinedTransformation;

    @JmixProperty
    protected Boolean hidden;

    @JmixProperty
    protected Boolean defaultDateIsCurrent = false;

    protected ParameterValidator<?> validationDelegate; // todo exclude from json

    protected ParameterTransformer<?> transformationDelegate; // todo exclude from json

    protected DefaultValueProvider<?> defaultValueProvider; // todo exclude from json

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public ParameterType getType() {
        return type != null ? ParameterType.fromId(type) : null;
    }

    public void setType(ParameterType type) {
        this.type = type != null ? type.getId() : null;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEntityMetaClass() {
        return entityMetaClass;
    }

    public Boolean getLookup() {
        return lookup;
    }

    public void setLookup(Boolean lookup) {
        this.lookup = lookup;
    }

    public String getLookupJoin() {
        return lookupJoin;
    }

    public void setLookupJoin(String lookupJoin) {
        this.lookupJoin = lookupJoin;
    }

    public String getLookupWhere() {
        return lookupWhere;
    }

    public void setLookupWhere(String lookupWhere) {
        this.lookupWhere = lookupWhere;
    }

    public void setEntityMetaClass(String entityMetaClass) {
        this.entityMetaClass = entityMetaClass;
    }

    public String getEnumerationClass() {
        return enumerationClass;
    }

    public void setEnumerationClass(String enumerationClass) {
        this.enumerationClass = enumerationClass;
    }

    @Override
    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getLocaleNames() {
        return localeNames;
    }

    public void setLocaleNames(String localeNames) {
        this.localeNames = localeNames;
    }

    @InstanceName
    @DependsOnProperties({"localeNames", "name"})
    public String getInstanceName(MsgBundleTools msgBundleTools) {
        return msgBundleTools.getLocalizedValue(localeNames, name);
    }

    @Override
    public Class getParameterClass() {
        try {
            if (StringUtils.isNotBlank(parameterClassName)) {
                return Class.forName(parameterClassName);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("An error occurred while resolving class name " + parameterClassName, e);
        }

        return null;
    }

    public void setParameterClass(Class parameterClass) {
        if (parameterClass != null) {
            parameterClassName = parameterClass.getName();
        }
    }

    public String getParameterClassName() {
        return parameterClassName;
    }

    public void setParameterClassName(String parameterClassName) {
        this.parameterClassName = parameterClassName;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTransformationScript() {
        return transformationScript;
    }

    public void setTransformationScript(String transformationScript) {
        this.transformationScript = transformationScript;
    }

    public PredefinedTransformation getPredefinedTransformation() {
        return predefinedTransformation != null ? PredefinedTransformation.fromId(predefinedTransformation) : null;
    }

    public void setPredefinedTransformation(PredefinedTransformation predefinedTransformation) {
        this.predefinedTransformation = predefinedTransformation != null ? predefinedTransformation.getId() : null;
    }

    public String getValidationScript() {
        return validationScript;
    }

    public void setValidationScript(String validationScript) {
        this.validationScript = validationScript;
    }

    public Boolean getValidationOn() {
        return validationOn;
    }

    public void setValidationOn(Boolean validationOn) {
        this.validationOn = validationOn;
    }

    public ParameterValidator<?> getValidationDelegate() {
        return validationDelegate;
    }

    public void setValidationDelegate(ParameterValidator<?> validationDelegate) {
        this.validationDelegate = validationDelegate;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getDefaultDateIsCurrent() {
        return defaultDateIsCurrent;
    }

    public void setDefaultDateIsCurrent(Boolean defaultDateIsCurrent) {
        this.defaultDateIsCurrent = defaultDateIsCurrent;
    }

    public ParameterTransformer<?> getTransformationDelegate() {
        return transformationDelegate;
    }

    public void setTransformationDelegate(ParameterTransformer<?> transformationDelegate) {
        this.transformationDelegate = transformationDelegate;
    }

    @Override
    public DefaultValueProvider<?> getDefaultValueProvider() {
        return defaultValueProvider;
    }

    public void setDefaultValueProvider(DefaultValueProvider<?> defaultValueProvider) {
        this.defaultValueProvider = defaultValueProvider;
    }
}