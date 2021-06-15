/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.validators.DateValidator;
import com.haulmont.cuba.gui.components.validators.DoubleValidator;
import com.haulmont.cuba.gui.components.validators.IntegerValidator;
import com.haulmont.cuba.gui.components.validators.LongValidator;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.RuntimePropsDatasource;
import com.haulmont.cuba.gui.dynamicattributes.DynamicAttributesGuiTools;
import io.jmix.core.DevelopmentException;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.dynattr.model.Categorized;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.SizeUnit;
import io.jmix.ui.component.SizeWithUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

/**
 * Universal frame for editing dynamic attributes of any {@link Categorized} implementations.
 */
public class RuntimePropertiesFrame extends AbstractFrame {

    public static final String NAME = "runtimeProperties";
    public static final String DEFAULT_FIELD_WIDTH = "100%";

    protected RuntimePropsDatasource rds;

    protected CollectionDatasource categoriesDs;

    protected boolean requiredControlEnabled = true;

    @Autowired
    protected BoxLayout categoryFieldBox;

    @Autowired
    protected LookupField categoryField;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected DynamicAttributesGuiTools dynamicAttributesGuiTools;

    @Autowired
    protected DynAttrMetadata dynAttrMetadata;

    @Autowired
    protected MsgBundleTools msgBundleTools;

    @Autowired
    protected Security security;

    @WindowParam
    protected String rows;

    @WindowParam
    protected String cols;

    @WindowParam
    protected String fieldWidth;

    @WindowParam
    protected Boolean borderVisible;

    @WindowParam
    protected String fieldCaptionWidth;

    @WindowParam
    protected String[] fieldCaptionWidths;

    @Override
    public void init(Map<String, Object> params) {
        initDatasources(params);

        if (StringUtils.isEmpty(fieldWidth)) {
            fieldWidth = DEFAULT_FIELD_WIDTH;
        }

        initCategoryField();
        loadComponent(rds);
    }

    protected void initDatasources(Map<String, Object> params) {
        String dsId = (String) params.get("runtimeDs");
        if (dsId == null) {
            throw new DevelopmentException("runtimeProperties initialization error: runtimeDs is not provided");
        }
        rds = (RuntimePropsDatasource) getDsContext().get(dsId);
        if (rds == null) {
            throw new DevelopmentException(
                    String.format("runtimeProperties initialization error: runtimeDs '%s' does not exist", dsId));
        }

        String categoriesDsId = (String) params.get("categoriesDs");
        if (categoriesDsId == null) {
            throw new DevelopmentException("runtimeProperties initialization error: categoriesDs is not provided");
        }

        categoriesDs = (CollectionDatasource) getDsContext().get(categoriesDsId);
        if (categoriesDs == null) {
            throw new DevelopmentException(
                    String.format("runtimeProperties initialization error: categoriesDs '%s' does not exist",
                            categoriesDsId)
            );
        }
    }

    protected void initCategoryField() {
        categoryField.setDatasource(rds.getMainDs(), "category");
        categoryField.setOptionsDatasource(categoriesDs);
    }

    @SuppressWarnings("unchecked")
    protected void loadComponent(Datasource ds) {
        ds.addStateChangeListener(e -> {
            if (!Datasource.State.VALID.equals(e.getState())) {
                return;
            }
            createRuntimeFieldGroup(ds);
        });
    }

    protected FieldGroup createRuntimeFieldGroup(Datasource ds) {
        Component runtime = getComponent("runtime");
        if (runtime != null) {
            remove(runtime);
        }

        FieldGroup newRuntimeFieldGroup = uiComponents.create(FieldGroup.class);
        newRuntimeFieldGroup.setBorderVisible(Boolean.TRUE.equals(borderVisible));

        newRuntimeFieldGroup.setWidth("100%");
        newRuntimeFieldGroup.setId("runtime");

        newRuntimeFieldGroup.setFrame(getFrame());
        add(newRuntimeFieldGroup);

        for (FieldGroup.FieldConfig field : newRuntimeFieldGroup.getFields()) {
            newRuntimeFieldGroup.removeField(field);
        }

        List<FieldGroup.FieldConfig> fields = createFieldsForAttributes(newRuntimeFieldGroup);
        addFieldsToFieldGroup(newRuntimeFieldGroup, fields);

        if (!newRuntimeFieldGroup.getFields().isEmpty()) {
            newRuntimeFieldGroup.setDatasource(ds);
            newRuntimeFieldGroup.bind();
        }

        for (FieldGroup.FieldConfig fieldConfig : newRuntimeFieldGroup.getFields()) {
            loadValidators(newRuntimeFieldGroup, fieldConfig);
            loadRequired(newRuntimeFieldGroup, fieldConfig);
            loadEditable(newRuntimeFieldGroup, fieldConfig);
        }

        initFieldCaptionWidth(newRuntimeFieldGroup);

        return newRuntimeFieldGroup;
    }

    protected void initFieldCaptionWidth(FieldGroup newRuntimeFieldGroup) {
        if (fieldCaptionWidth != null) {
            if (fieldCaptionWidth.contains("%")) {
                throw new IllegalStateException("RuntimePropertiesFrame fieldCaptionWidth with '%' unit is unsupported");
            }

            int captionWidth = Integer.parseInt(fieldCaptionWidth.replace("px", ""));

            newRuntimeFieldGroup.setFieldCaptionWidth(captionWidth);
        }
        if (fieldCaptionWidths != null) {
            for (int i = 0; i < fieldCaptionWidths.length; i++) {
                if (fieldCaptionWidths[i].contains("%")) {
                    throw new IllegalStateException("RuntimePropertiesFrame fieldCaptionWidth with '%' unit is unsupported");
                }

                int captionWidth = Integer.parseInt(fieldCaptionWidths[i].replace("px", ""));

                newRuntimeFieldGroup.setFieldCaptionWidth(i, captionWidth);
            }
        }
    }

    protected List<FieldGroup.FieldConfig> createFieldsForAttributes(FieldGroup newRuntimeFieldGroup) {
        @SuppressWarnings("unchecked")
        Collection<AttributeDefinition> attributes = rds.getAttributesByCategory();
        List<FieldGroup.FieldConfig> fields = new ArrayList<>(attributes.size());

        for (AttributeDefinition attribute : attributes) {
            String propertyName = DynAttrUtils.getPropertyFromAttributeCode(attribute.getCode());
            FieldGroup.FieldConfig field = newRuntimeFieldGroup.createField(propertyName);
            field.setProperty(propertyName);
            field.setCaption(msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName()));
            field.setDescription(msgBundleTools.getLocalizedValue(attribute.getDescriptionsMsgBundle(), attribute.getDescription()));

            if (StringUtils.isNotBlank(attribute.getConfiguration().getFormWidth())) {
                field.setWidth(new SizeWithUnit(attribute.getConfiguration().getColumnWidth(), SizeUnit.PIXELS).stringValue());
            } else {
                field.setWidth(fieldWidth);
            }
            fields.add(field);
        }
        return fields;
    }

    protected void addFieldsToFieldGroup(FieldGroup newRuntimeFieldGroup, List<FieldGroup.FieldConfig> fields) {
        int rowsPerColumn;
        int propertiesCount = rds.getAttributesByCategory().size();
        if (StringUtils.isNotBlank(cols)) {
            if (propertiesCount % Integer.parseInt(cols) == 0) {
                rowsPerColumn = propertiesCount / Integer.parseInt(cols);
            } else {
                rowsPerColumn = propertiesCount / Integer.parseInt(cols) + 1;
            }
        } else if (StringUtils.isNotBlank(rows)) {
            rowsPerColumn = Integer.parseInt(rows);
        } else {
            rowsPerColumn = propertiesCount;
        }

        int columnNo = 0;
        int fieldsCount = 0;
        for (FieldGroup.FieldConfig field : fields) {
            fieldsCount++;
            newRuntimeFieldGroup.addField(field, columnNo);
            if (fieldsCount % rowsPerColumn == 0) {
                columnNo++;
                newRuntimeFieldGroup.setColumns(columnNo + 1);
            }
        }
    }

    protected void loadValidators(FieldGroup fieldGroup, FieldGroup.FieldConfig field) {
        getAttributeByPropertyName(rds.resolveCategorizedEntityClass(), field.getId())
                .ifPresent(attribute -> {
                    Collection<Consumer<?>> validators = getValidator(attribute);
                    if (validators != null && !validators.isEmpty()) {
                        for (Consumer<?> validator : validators) {
                            field.addValidator(validator);
                        }
                    }
                });
    }

    protected void loadRequired(FieldGroup fieldGroup, FieldGroup.FieldConfig field) {
        getAttributeByPropertyName(rds.resolveCategorizedEntityClass(), field.getId())
                .ifPresent(attribute -> {
                    String requiredMessage = messages.formatMessage("",
                            "validation.required.defaultMsg", attribute.getName());
                    field.setRequired(attribute.isRequired() && requiredControlEnabled);
                    field.setRequiredMessage(requiredMessage);
                });
    }

    protected void loadEditable(FieldGroup fieldGroup, FieldGroup.FieldConfig field) {
        if (fieldGroup.isEditable()) {
            MetaClass metaClass = rds.resolveCategorizedEntityClass();
            getAttributeByPropertyName(metaClass, field.getProperty())
                    .ifPresent(attribute -> {
                        MetaProperty metaProperty = attribute.getMetaProperty();
                        MetaPropertyPath propertyPath = new MetaPropertyPath(metaClass, metaProperty);

                        boolean editableFromPermissions = security.isEntityAttrUpdatePermitted(metaClass, propertyPath.toString());
                        if (!editableFromPermissions) {
                            field.setEditable(false);
                        }
                        boolean visibleFromPermissions = security.isEntityAttrReadPermitted(metaClass, propertyPath.toString());
                        if (!visibleFromPermissions) {
                            field.setVisible(false);
                        }

                    });

        }
    }

    public void setCategoryFieldVisible(boolean visible) {
        categoryFieldBox.setVisible(visible);
    }

    public boolean isRequiredControlEnabled() {
        return requiredControlEnabled;
    }

    public void setRequiredControlEnabled(boolean requiredControlEnabled) {
        this.requiredControlEnabled = requiredControlEnabled;
        FieldGroup newRuntime = (FieldGroup) getComponent("runtime");
        if (newRuntime != null) {
            for (final FieldGroup.FieldConfig field : newRuntime.getFields()) {
                loadRequired(newRuntime, field);
            }
        }
    }

    public void setCategoryFieldEditable(boolean editable) {
        categoryField.setEditable(editable);
        FieldGroup newRuntime = (FieldGroup) getComponent("runtime");
        if (newRuntime != null) {
            newRuntime.setEditable(editable);
        }
    }

    @Nullable
    protected Collection<Consumer<?>> getValidator(AttributeDefinition attribute) {
        Collection<Consumer<?>> validators = null;
        if (!attribute.isCollection()) {

            validators = dynamicAttributesGuiTools.createValidators(attribute);

            Consumer validator = null;
            Class type = attribute.getJavaType();
            if (type.equals(Integer.class)) {
                validator = new IntegerValidator(messages.getMessage("validation.invalidNumber"));

            } else if (type.equals(Long.class)) {
                validator = new LongValidator(messages.getMessage("validation.invalidNumber"));

            } else if (type.equals(Double.class) || type.equals(BigDecimal.class)) {
                validator = new DoubleValidator(messages.getMessage("validation.invalidNumber"));

            } else if (type.equals(java.sql.Date.class)) {
                validator = new DateValidator(messages.getMessage("validation.invalidDate"));
            }
            if (validator != null) {
                validators.add(validator);
            }
        }
        return validators;
    }

    protected Optional<AttributeDefinition> getAttributeByPropertyName(MetaClass metaClass, String propertyName) {
        String attributeCode = DynAttrUtils.getAttributeCodeFromProperty(propertyName);
        return dynAttrMetadata.getAttributeByCode(metaClass, attributeCode);
    }
}