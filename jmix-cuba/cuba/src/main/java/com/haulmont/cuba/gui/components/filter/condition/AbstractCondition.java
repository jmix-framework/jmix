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
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.gui.components.filter.ConditionParamBuilder;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.descriptor.AbstractConditionDescriptor;
import com.haulmont.cuba.gui.components.filter.operationedit.AbstractOperationEditor;
import io.jmix.core.MessageTools;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import com.haulmont.cuba.core.global.filter.Op;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Class that encapsulates common filter condition behaviour.
 */
@JmixEntity(name = "sec$AbstractCondition", annotatedPropertiesOnly = true)
@SystemLevel
public abstract class AbstractCondition extends BaseUuidEntity {

    private static final long serialVersionUID = -8405022004399309798L;

    public interface Listener {

        void captionChanged();

        void paramChanged(Param oldParam, Param newParam);
    }

    protected String name;
    protected String paramName;
    protected String caption;
    protected String messagesPack;
    @JmixProperty
    protected String locCaption;
    protected String filterComponentName;
    protected String text;
    protected io.jmix.core.metamodel.model.MetaClass metaClass;
    protected Boolean group = false;
    protected Boolean unary = false;
    protected Boolean inExpr = false;
    protected Class javaClass;
    protected Class paramClass;
    protected Param param;
    protected String entityAlias;
    protected Boolean hidden = false;
    protected Boolean required = false;
    protected String entityParamWhere;
    protected String entityParamView;
    protected Boolean useUserTimeZone;
    protected Integer width = 1;
    protected Op operator;

    protected List<Listener> listeners = new ArrayList<>();
    protected AbstractOperationEditor operationEditor;

    protected AbstractCondition() {}

    protected AbstractCondition(AbstractCondition other) {
        this.name = other.name;
        this.caption = other.caption;
        this.messagesPack = other.messagesPack;
        this.locCaption = other.locCaption;
        this.filterComponentName = other.filterComponentName;
        this.group = other.group;
        this.unary = other.unary;
        this.inExpr = other.inExpr;
        this.javaClass = other.javaClass;
        this.paramClass = other.paramClass;
        this.paramName = other.paramName;
        this.entityAlias = other.entityAlias;
        this.hidden = other.hidden;
        this.required = other.required;
        this.entityParamWhere = other.entityParamWhere;
        this.entityParamView = other.entityParamView;
        this.metaClass = other.metaClass;
        this.width = other.width;
        this.param = other.param;
        this.text = other.text;
        this.operator = other.operator;
        this.useUserTimeZone = other.useUserTimeZone;
    }

    protected AbstractCondition(Element element, String messagesPack, String filterComponentName, io.jmix.core.metamodel.model.MetaClass metaClass) {
        this.messagesPack = messagesPack;
        this.filterComponentName = filterComponentName;
        name = element.attributeValue("name");
        text = StringEscapeUtils.unescapeXml(element.getTextTrim());
        this.metaClass = metaClass;
        if (text == null)
            text = "";

        caption = element.attributeValue("caption");
        MessageTools messageTools = AppBeans.get(MessageTools.class);
        locCaption = messageTools.loadString(messagesPack, caption);

        unary = Boolean.valueOf(element.attributeValue("unary"));
        inExpr = Boolean.valueOf(element.attributeValue("inExpr"));
        hidden = Boolean.valueOf(element.attributeValue("hidden"));
        required = Boolean.valueOf(element.attributeValue("required"));
        useUserTimeZone = Boolean.valueOf(element.attributeValue("useUserTimeZone"));
        entityParamWhere = element.attributeValue("paramWhere");
        entityParamView = element.attributeValue("paramView");
        this.entityAlias = element.attributeValue("entityAlias");
        width = Strings.isNullOrEmpty(element.attributeValue("width")) ? 1 : Integer.parseInt(element.attributeValue("width"));

        resolveParam(element);
    }

    protected AbstractCondition(AbstractConditionDescriptor descriptor) {
        name = descriptor.getName();
        caption = descriptor.getCaption();
        locCaption = descriptor.getLocCaption();
        filterComponentName = descriptor.getFilterComponentName();
        javaClass = descriptor.getJavaClass();
        unary = javaClass == null;
        entityParamWhere = descriptor.getEntityParamWhere();
        entityParamView = descriptor.getEntityParamView();
        metaClass = descriptor.getDatasourceMetaClass();
        messagesPack = descriptor.getMessagesPack();
        ConditionParamBuilder paramBuilder = AppBeans.get(ConditionParamBuilder.class);
        paramName = paramBuilder.createParamName(this);
        param = paramBuilder.createParam(this);
        String operatorType = descriptor.getOperatorType();
        if (operatorType != null) {
            operator = Op.valueOf(operatorType);
        }
    }

    protected void resolveParam(Element element) {
        Scripting scripting = AppBeans.get(Scripting.NAME);
        String aclass = element.attributeValue("class");
        if (!isBlank(aclass)) {
            javaClass = scripting.loadClass(aclass);
        }

        String operatorName = element.attributeValue("operatorType", null);
        if (operatorName != null) {
            operator = Op.valueOf(operatorName);
        }

        List<Element> paramElements = element.elements("param");
        if (!paramElements.isEmpty()) {
            Element paramElem = paramElements.iterator().next();

            if (BooleanUtils.toBoolean(paramElem.attributeValue("hidden", "false"), "true", "false")) {
                paramElem = paramElements.iterator().next();
            }
            paramName = paramElem.attributeValue("name");

            if (!isBlank(paramElem.attributeValue("javaClass"))) {
                paramClass = scripting.loadClass(paramElem.attributeValue("javaClass"));
            }

            ConditionParamBuilder paramBuilder = AppBeans.get(ConditionParamBuilder.class);
            if (Strings.isNullOrEmpty(paramName)) {
                paramName = paramBuilder.createParamName(this);
            }

            param = paramBuilder.createParam(this);
            param.setDateInterval(BooleanUtils.toBoolean(paramElem.attributeValue("isDateInterval", "false"), "true", "false"));

            // read additional attribute for filter with folder entities set, because if all entities in set are
            // removed (in db), value from param returns 'NULL' and shows all entities
            String isFoldersFilterEntitiesSet = paramElem.attributeValue("isFoldersFilterEntitiesSet");
            if (!Strings.isNullOrEmpty(isFoldersFilterEntitiesSet)) {
                boolean isEntitiesSet = Boolean.parseBoolean(isFoldersFilterEntitiesSet);
                param.setFoldersFilterEntitiesSet(isEntitiesSet);
            }

            param.parseValue(paramElem.getText());
            param.setDefaultValue(param.getValue());
        }

        if ("EMPTY".equals(operatorName)) {
            //for backward compatibility with old filters that still use EMPTY operator
            operatorName = "NOT_EMPTY";
            if (BooleanUtils.isTrue((Boolean) param.getValue()))
                param.setValue(false);
            param.setDefaultValue(false);
            operator = Op.valueOf(operatorName);
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public String getName() {
        return name;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLocCaption() {
        return locCaption;
    }

    public void setLocCaption(String locCaption) {
        if (Objects.equals(this.locCaption, locCaption))
            return;

        this.locCaption = locCaption;
        for (Listener listener : listeners) {
            listener.captionChanged();
        }
    }

    public String getText() {
        updateText();
        return text;
    }

    protected void updateText() {
    }

    public Param getParam() {
        return param;
    }

    public void setParam(Param param) {
        Param oldParam = this.param;
        this.param = param;

        for (AbstractCondition.Listener listener : listeners) {
            listener.paramChanged(oldParam, param);
        }
    }

    public String getParamName() {
        return paramName;
    }

    public Class getParamClass() {
        return paramClass;
    }

    public String getEntityAlias() {
        return entityAlias;
    }

    public String getFilterComponentName() {
        return filterComponentName;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getUseUserTimeZone() {
        return useUserTimeZone;
    }

    public void setUseUserTimeZone(Boolean useUserTimeZone) {
        this.useUserTimeZone = useUserTimeZone;
    }

    public void toXml(Element element, Param.ValueProperty valueProperty) {
        String text = getText();
        if (StringUtils.isNotBlank(text))
            element.addCDATA(text);

        element.addAttribute("name", name);

        if (javaClass != null)
            element.addAttribute("class", javaClass.getName());

        if (caption != null)
            element.addAttribute("caption", caption);

        if (unary)
            element.addAttribute("unary", "true");

        if (inExpr)
            element.addAttribute("inExpr", "true");

        if (hidden)
            element.addAttribute("hidden", "true");

        if (required)
            element.addAttribute("required", "true");

        if (Boolean.TRUE.equals(useUserTimeZone))
            element.addAttribute("useUserTimeZone", "true");

        if (operator != null) {
            element.addAttribute("operatorType", operator.name());
        }

        if (param != null) {
            param.toXml(element, valueProperty);
            if (entityParamWhere != null)
                element.addAttribute("paramWhere", entityParamWhere);
            if (entityParamView != null)
                element.addAttribute("paramView", entityParamView);
        }

        if (width != null) {
            element.addAttribute("width", width.toString());
        }
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
    }

    public Boolean isGroup() {
        return group;
    }

    public Boolean getUnary() {
        return unary;
    }

    public void setUnary(Boolean unary) {
        this.unary = unary;
    }

    public Boolean getInExpr() {
        return inExpr;
    }

    public void setInExpr(Boolean inExpr) {
        this.inExpr = inExpr;
    }

    public String getOperationCaption() {
        return "";
    }

    public Op getOperator() {
        return operator;
    }

    public void setOperator(Op operator) {
        this.operator = operator;
    }

    public String getEntityParamView() {
        return entityParamView;
    }

    public void setEntityParamView(String entityParamView) {
        this.entityParamView = entityParamView;
    }

    public String getEntityParamWhere() {
        return entityParamWhere;
    }

    public io.jmix.core.metamodel.model.MetaClass getEntityMetaClass() {
        return metaClass;
    }

    public void setEntityParamWhere(String entityParamWhere) {
        this.entityParamWhere = entityParamWhere;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public AbstractOperationEditor createOperationEditor() {
        return null;
    }

    public AbstractOperationEditor getOperationEditor() {
        return operationEditor;
    }

    public abstract AbstractCondition createCopy();

    public boolean canBeRequired() {
        return true;
    }

    public boolean canHasWidth() {
        return true;
    }

    public boolean canHasDefaultValue() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                '}';
    }
}
