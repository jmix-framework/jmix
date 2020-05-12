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

package com.haulmont.cuba.gui.components.filter.edit;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.filter.ConditionParamBuilder;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.condition.DynamicAttributesCondition;
import io.jmix.core.AppBeans;
import io.jmix.core.Entity;
import io.jmix.core.ReferenceToEntitySupport;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.CategoryDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.dynattrui.MsgBundleTools;
import io.jmix.ui.components.Frame;
import io.jmix.ui.components.Label;
import io.jmix.ui.components.LookupField;
import io.jmix.ui.components.TextField;
import io.jmix.ui.filter.Op;
import io.jmix.ui.filter.OpManager;
import org.apache.commons.lang3.RandomStringUtils;

import javax.inject.Inject;
import java.util.*;

import static io.jmix.ui.filter.Op.*;

public class DynamicAttributesConditionFrame extends ConditionFrame<DynamicAttributesCondition> {

    @Inject
    protected LookupField<CategoryDefinition> categoryLookup;
    @Inject
    protected LookupField<AttributeDefinition> attributeLookup;
    @Inject
    protected LookupField<Op> operationLookup;
    @Inject
    protected Label<String> categoryLabel;
    @Inject
    protected TextField<String> caption;

    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Inject
    protected MsgBundleTools msgBundleTools;
    @Inject
    protected Metadata metadata;
    @Inject
    protected DynAttrMetadata dynAttrMetadata;
    @Inject
    protected OpManager opManager;

    @Override
    protected void initComponents() {
        super.initComponents();

        categoryLookup.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                fillAttributeSelect(e.getValue());
            }
        });

        attributeLookup.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                fillOperationSelect(e.getValue());
            }
        });
    }

    @Override
    public void setCondition(DynamicAttributesCondition condition) {
        super.setCondition(condition);
        fillCategorySelect();
        caption.setValue(condition.getCaption());
    }

    protected String checkCondition() {
        if (categoryLookup.getValue() == null) {
            return "filter.dynamicAttributesConditionFrame.selectCategory";
        }
        if (attributeLookup.getValue() == null) {
            return "filter.dynamicAttributesConditionFrame.selectAttribute";
        }
        if (operationLookup.getValue() == null) {
            return "filter.dynamicAttributesConditionFrame.selectOperator";
        }
        return null;
    }

    @Override
    public boolean commit() {
        if (!super.commit())
            return false;

        String error = checkCondition();
        if (error != null) {
            showNotification(messages.getMessage(error), Frame.NotificationType.TRAY);
            return false;
        }

        AttributeDefinition attribute = attributeLookup.getValue();

        String cavAlias = "cav" + RandomStringUtils.randomNumeric(5);

        String paramName;
        String operation = operationLookup.getValue().forJpql();
        Op op = operationLookup.getValue();

        Class javaClass = DynAttrUtils.getMetaProperty(attribute).getJavaType();
        String propertyPath = Strings.isNullOrEmpty(condition.getPropertyPath()) ? "" : "." + condition.getPropertyPath();
        ConditionParamBuilder paramBuilder = AppBeans.get(ConditionParamBuilder.class);
        paramName = paramBuilder.createParamName(condition);

        String cavEntityId = referenceToEntitySupport.getReferenceIdPropertyName(condition.getEntityMetaClass());

        String where;
        if (op == Op.NOT_EMPTY) {
            where = "(exists (select " + cavAlias + " from sys$CategoryAttributeValue " + cavAlias +
                    " where " + cavAlias + ".entity." + cavEntityId + "=" +
                    "{E}" +
                    propertyPath +
                    ".id and " + cavAlias + ".categoryAttribute.id='" +
                    attributeLookup.getValue().getId() + "'))";
        } else {
            String valueFieldName = "stringValue";
            if (Entity.class.isAssignableFrom(javaClass))
                valueFieldName = "entityValue." + referenceToEntitySupport.getReferenceIdPropertyName(metadata.getClassNN(javaClass));
            else if (String.class.isAssignableFrom(javaClass))
                valueFieldName = "stringValue";
            else if (Integer.class.isAssignableFrom(javaClass))
                valueFieldName = "intValue";
            else if (Double.class.isAssignableFrom(javaClass))
                valueFieldName = "doubleValue";
            else if (Boolean.class.isAssignableFrom(javaClass))
                valueFieldName = "booleanValue";
            else if (Date.class.isAssignableFrom(javaClass))
                valueFieldName = "dateValue";

            if (attribute.isCollection()) {
                condition.setJoin(", sys$CategoryAttributeValue " + cavAlias + " ");

                String paramStr = " ? ";
                where = cavAlias + ".entity." + cavEntityId + "=" +
                        "{E}" +
                        propertyPath +
                        ".id and " + cavAlias + "." +
                        valueFieldName +
                        " " +
                        operation +
                        (op.isUnary() ? " " : paramStr) + "and " + cavAlias + ".categoryAttribute.id='" +
                        attributeLookup.getValue().getId() + "'";
                where = where.replace("?", ":" + paramName);
            } else {
                where = "(exists (select " + cavAlias + " from sys$CategoryAttributeValue " + cavAlias +
                        " where " + cavAlias + ".entity." + cavEntityId + "=" + "{E}" + propertyPath + ".id and "
                        + cavAlias + "." + valueFieldName + " = :" + paramName + " and " +
                        cavAlias + ".categoryAttribute.id='" + attributeLookup.getValue().getId() + "'))";
            }
        }

        condition.setWhere(where);
        condition.setUnary(op.isUnary());
        condition.setEntityParamView(null);
        condition.setEntityParamWhere(null);
        condition.setInExpr(Op.IN.equals(op) || Op.NOT_IN.equals(op));
        condition.setOperator(operationLookup.getValue());
        Class paramJavaClass = op.isUnary() ? Boolean.class : javaClass;
        condition.setJavaClass(javaClass);

        Param param = Param.Builder.getInstance()
                .setName(paramName)
                .setJavaClass(paramJavaClass)
                .setMetaClass(condition.getEntityMetaClass())
                .setProperty(DynAttrUtils.getMetaProperty(attribute))
                .setInExpr(condition.getInExpr())
                .setRequired(condition.getRequired())
                .setCategoryAttrId(attribute.getId())
                .build();

        Object defaultValue = condition.getParam().getDefaultValue();
        param.setDefaultValue(defaultValue);

        condition.setParam(param);
        if (categoryLookup.getValue() != null) {
            condition.setCategoryId(categoryLookup.getValue().getId());
        }
        condition.setCategoryAttributeId(attribute.getId());
        condition.setIsCollection(attribute.isCollection());
        condition.setLocCaption(msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName()));
        condition.setCaption(caption.getValue());

        return true;
    }

    protected void fillCategorySelect() {
        MetaClass metaClass = condition.getEntityMetaClass();
        if (!Strings.isNullOrEmpty(condition.getPropertyPath())) {
            MetaPropertyPath propertyPath = metaClass.getPropertyPath(condition.getPropertyPath());
            if (propertyPath == null) {
                throw new RuntimeException("Property path " + condition.getPropertyPath() + " doesn't exist");
            }
            metaClass = propertyPath.getRange().asClass();
        }

        Collection<CategoryDefinition> categories = dynAttrMetadata.getCategories(metaClass);
        CategoryDefinition firstCategory = Iterables.getFirst(categories, null);
        String currentId = condition.getCategoryId();

        if (categories.size() == 1 && firstCategory != null && (currentId == null || currentId.equals(firstCategory.getId()))) {

            categoryLookup.setVisible(false);
            categoryLabel.setVisible(false);

            attributeLookup.focus();

            Map<String, CategoryDefinition> options = new TreeMap<>();
            options.put(firstCategory.getName(), firstCategory);
            categoryLookup.setOptionsMap(options);

            categoryLookup.setValue(firstCategory);
            fillAttributeSelect(firstCategory);
        } else {
            categoryLookup.setVisible(true);
            categoryLabel.setVisible(true);

            CategoryDefinition selectedCategory = null;
            Map<String, CategoryDefinition> options = new TreeMap<>();
            for (CategoryDefinition item : categories) {
                options.put(item.getName(), item);
                if (Objects.equals(item.getId(), currentId)) {
                    selectedCategory = item;
                }
            }
            categoryLookup.setOptionsMap(options);
            categoryLookup.setValue(selectedCategory);
        }
    }

    protected void fillAttributeSelect(CategoryDefinition category) {
        String currentId = condition.getCategoryAttributeId();
        AttributeDefinition selectedAttribute = null;
        Map<String, AttributeDefinition> options = new TreeMap<>();
        for (AttributeDefinition attribute : category.getAttributeDefinitions()) {
            options.put(msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName()), attribute);
            if (Objects.equals(attribute.getId(), currentId)) {
                selectedAttribute = attribute;
            }
        }
        attributeLookup.setOptionsMap(options);
        attributeLookup.setValue(selectedAttribute);
    }

    protected void fillOperationSelect(AttributeDefinition attribute) {
        Class clazz = DynAttrUtils.getMetaProperty(attribute).getJavaType();
        EnumSet<Op> availableOps = attribute.isCollection() ?
                EnumSet.of(CONTAINS, DOES_NOT_CONTAIN, NOT_EMPTY) : opManager.availableOps(clazz);
        List<Op> ops = new LinkedList<>(availableOps);
        operationLookup.setOptionsList(ops);
        Op operator = condition.getOperator();
        if (operator != null) {
            operationLookup.setValue(operator);
        }
    }
}
