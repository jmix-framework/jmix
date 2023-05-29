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

package com.haulmont.cuba.gui.components.filter.descriptor;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.condition.CustomCondition;
import io.jmix.core.MessageTools;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.model.MetaClass;
import org.dom4j.Element;

/**
 * Class for existing (described in filter component xml) custom condition descriptor
 */
@JmixEntity(name = "sec$CustomConditionDescriptor")
@SystemLevel
public class CustomConditionDescriptor extends AbstractConditionDescriptor {
    public CustomConditionDescriptor(Element element,
                                     String messagesPack,
                                     String filterComponentName,
                                     MetaClass metaClass,
                                     String entityAlias) {
        super(element.attributeValue("name"), filterComponentName, metaClass, entityAlias);
        this.element = element;
        this.messagesPack = messagesPack;
        this.caption = element.attributeValue("caption");
        if (this.caption != null) {
            MessageTools messageTools = AppBeans.get(MessageTools.class);
            this.locCaption = messageTools.loadString(messagesPack, this.caption);
        }

        inExpr = Boolean.valueOf(element.attributeValue("inExpr"));
    }

    @Override
    public AbstractCondition createCondition() {
        CustomCondition condition = new CustomCondition(this,
                element.getText(), getJoinValue(), entityAlias, inExpr);
        return condition;
    }

    protected String getJoinValue() {
        Element joinElement = element.element("join");
        String join;
        if (joinElement != null) {
            join = joinElement.getText();
        } else {
            //for backward compatibility
            join = element.attributeValue("join");
        }
        return join;
    }

    @Override
    public Class getJavaClass() {
        String className = element.attributeValue("paramClass");
        if (className == null) {
            className = element.attributeValue("class");
        }

        if (className == null) {
            return null;
        } else {
            Scripting scripting = AppBeans.get(Scripting.NAME);
            return scripting.loadClass(element.attributeValue("paramClass"));
        }
    }

    @Override
    public String getEntityParamWhere() {
        return element.attributeValue("paramWhere");
    }

    @Override
    public String getEntityParamView() {
        return element.attributeValue("paramView");
    }
}
