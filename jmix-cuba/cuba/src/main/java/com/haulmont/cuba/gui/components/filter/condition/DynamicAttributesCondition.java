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

package com.haulmont.cuba.gui.components.filter.condition;

import com.google.common.base.Strings;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.filter.ConditionParamBuilder;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.descriptor.AbstractConditionDescriptor;
import com.haulmont.cuba.gui.components.filter.operationedit.AbstractOperationEditor;
import com.haulmont.cuba.gui.components.filter.operationedit.DynamicAttributesOperationEditor;
import io.jmix.core.MessageTools;
import io.jmix.core.QueryUtils;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.MsgBundleTools;
import com.haulmont.cuba.core.global.filter.ConditionType;
import com.haulmont.cuba.core.global.filter.Op;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.dom4j.Element;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@JmixEntity(name = "sec$DynamicAttributesCondition", annotatedPropertiesOnly = true)
@SystemLevel
public class DynamicAttributesCondition extends AbstractCondition {

    protected String categoryId;
    protected String categoryAttributeId;
    protected boolean isCollection;
    protected String propertyPath;
    protected String join;
    private static Pattern LIKE_PATTERN = Pattern.compile("(like \\S+)\\s+(?!ESCAPE)");

    public DynamicAttributesCondition(DynamicAttributesCondition condition) {
        super(condition);
        this.join = condition.getJoin();
        this.categoryId = condition.getCategoryId();
        this.categoryAttributeId = condition.getCategoryAttributeId();
        this.isCollection = condition.getIsCollection();
    }

    public DynamicAttributesCondition(AbstractConditionDescriptor descriptor, String entityAlias, String propertyPath) {
        super(descriptor);
        this.entityAlias = entityAlias;
        this.name = RandomStringUtils.randomAlphabetic(10);
        Messages messages = AppBeans.get(Messages.class);
        this.locCaption = messages.getMainMessage("newDynamicAttributeCondition");
        this.propertyPath = propertyPath;
    }

    public DynamicAttributesCondition(Element element, String messagesPack, String filterComponentName, io.jmix.core.metamodel.model.MetaClass metaClass) {
        super(element, messagesPack, filterComponentName, metaClass);

        propertyPath = element.attributeValue("propertyPath");

        MessageTools messageTools = AppBeans.get(MessageTools.class);
        locCaption = isBlank(caption)
                ? element.attributeValue("locCaption")
                : messageTools.loadString(messagesPack, caption);

        entityAlias = element.attributeValue("entityAlias");
        text = element.getText();
        join = element.attributeValue("join");
        categoryId = element.attributeValue("category");
        String categoryAttributeValue = element.attributeValue("categoryAttribute");
        if (!Strings.isNullOrEmpty(categoryAttributeValue)) {
            categoryAttributeId = categoryAttributeValue;
        } else {
            //for backward compatibility
            List<Element> paramElements = element.elements("param");
            for (Element paramElement : paramElements) {
                if (BooleanUtils.toBoolean(paramElement.attributeValue("hidden", "false"), "true", "false")) {
                    categoryAttributeId = paramElement.getText();
                    String paramName = paramElement.attributeValue("name");
                    text = text.replace(":" + paramName, "'" + categoryAttributeId + "'");
                }
            }
        }

        isCollection = Boolean.parseBoolean(element.attributeValue("isCollection"));
        resolveParam(element);
    }

    @Override
    public void toXml(Element element, Param.ValueProperty valueProperty) {
        super.toXml(element, valueProperty);
        element.addAttribute("type", ConditionType.RUNTIME_PROPERTY.name());
        if (isBlank(caption)) {
            element.addAttribute("locCaption", locCaption);
        }
        element.addAttribute("category", categoryId.toString());
        element.addAttribute("categoryAttribute", categoryAttributeId.toString());
        element.addAttribute("entityAlias", entityAlias);
        if (!isBlank(propertyPath)) {
            element.addAttribute("propertyPath", propertyPath);
        }
        if (!isBlank(join)) {
            element.addAttribute("join", StringEscapeUtils.escapeXml(join));
        }
        if (isCollection) {
            element.addAttribute("isCollection", "true");
        }
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String id) {
        categoryId = id;
    }

    public String getCategoryAttributeId() {
        return categoryAttributeId;
    }

    public void setCategoryAttributeId(String categoryAttributeId) {
        this.categoryAttributeId = categoryAttributeId;
    }

    public boolean getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(boolean collection) {
        isCollection = collection;
    }

    @Override
    public void setOperator(Op operator) {
        if (!Objects.equals(this.operator, operator)) {
            this.operator = operator;
            String paramName = param.getName();
            ConditionParamBuilder paramBuilder = AppBeans.get(ConditionParamBuilder.class);
            if (operator.isUnary()) {
                unary = true;
                inExpr = false;
                Param param = Param.Builder.getInstance()
                        .setName(paramName)
                        .setJavaClass(Boolean.class)
                        .setInExpr(false)
                        .setRequired(required).build();
                setParam(param);
            } else {
                unary = false;
                inExpr = operator.equals(Op.IN) || operator.equals(Op.NOT_IN);
                Param param = paramBuilder.createParam(this);
                setParam(param);
            }
        }
    }

    @Override
    public String getOperationCaption() {
        io.jmix.core.Messages messages = AppBeans.get(io.jmix.core.Messages.class);
        return messages.getMessage(Op.class, "Op." + operator.name());
    }

    @Override
    public AbstractOperationEditor createOperationEditor() {
        operationEditor = new DynamicAttributesOperationEditor(this);
        return operationEditor;
    }

    @Override
    protected void updateText() {
        if (operator == Op.NOT_EMPTY) {
            if (BooleanUtils.isTrue((Boolean) param.getValue())) {
                text = text.replace("not exists", "exists");
            } else if (BooleanUtils.isFalse((Boolean) param.getValue()) && !text.contains("not exists")) {
                text = text.replace("exists ", "not exists ");
            }
        }

        if (!isCollection) {
            if (operator == Op.ENDS_WITH || operator == Op.STARTS_WITH || operator == Op.CONTAINS || operator == Op.DOES_NOT_CONTAIN) {
                Matcher matcher = LIKE_PATTERN.matcher(text);
                if (matcher.find()) {
                    String escapeCharacter = ("\\".equals(QueryUtils.ESCAPE_CHARACTER) || "$".equals(QueryUtils.ESCAPE_CHARACTER))
                            ? QueryUtils.ESCAPE_CHARACTER + QueryUtils.ESCAPE_CHARACTER
                            : QueryUtils.ESCAPE_CHARACTER;
                    text = matcher.replaceAll("$1 ESCAPE '" + escapeCharacter + "' ");
                }
            }
        } else {
            if (operator == Op.CONTAINS) {
                text = text.replace("not exists", "exists");
            } else if (operator == Op.DOES_NOT_CONTAIN && !text.contains("not exists")) {
                text = text.replace("exists ", "not exists ");
            }
        }
    }

    public String getJoin() {
        return join;
    }

    public void setJoin(String join) {
        this.join = join;
    }

    public String getWhere() {
        updateText();
        return text;
    }

    public void setWhere(String where) {
        this.text = where;
    }

    public String getPropertyPath() {
        return propertyPath;
    }

    @Override
    public AbstractCondition createCopy() {
        return new DynamicAttributesCondition(this);
    }

    @Override
    public String getLocCaption() {
        if (isBlank(caption) && !isBlank(propertyPath)) {
            MessageTools messageTools = AppBeans.get(MessageTools.class);
            String propertyCaption = messageTools.getPropertyCaption(metaClass, propertyPath);
            if (!isBlank(propertyCaption)) {
                return propertyCaption + "." + locCaption;
            }
        } else if (isNotBlank(caption)) {
            MessageTools messageTools = AppBeans.get(MessageTools.class);
            return messageTools.loadString(messagesPack, caption);
        }

        DynAttrMetadata dynamicModelMetadata = AppBeans.get(DynAttrMetadata.class);
        AttributeDefinition attribute = dynamicModelMetadata.getAttributes(metaClass).stream()
                .filter(attr -> Objects.equals(EntityValues.getId(attr.getSource()), getCategoryAttributeId()))
                .findFirst()
                .orElse(null);
        if (attribute != null) {
            MsgBundleTools msgBundleTools = AppBeans.get(MsgBundleTools.class);
            return msgBundleTools.getLocalizedValue(attribute.getNameMsgBundle(), attribute.getName());
        }

        return super.getLocCaption();
    }
}
