/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.core.global.filter;

import io.jmix.core.common.util.ReflectionHelper;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterParser implements Serializable {

    protected final Condition root;

    public FilterParser(Element element) {
        if (element.elements().isEmpty())
            throw new IllegalArgumentException("filter element is empty");
        Element rootElem = element.elements().get(0);
        root = createCondition(rootElem);
        parse(rootElem, root.getConditions());
    }

    public FilterParser(Condition root) {
        this.root = root;
    }

    public Condition getRoot() {
        return root;
    }

    protected Condition createCondition(Element conditionElement) {
        Condition condition;
        String conditionName;

        if (conditionElement.attributeValue("locCaption") == null) {
            conditionName = conditionElement.attributeValue("name");
        } else {
            conditionName = conditionElement.attributeValue("locCaption");
        }

        if ("c".equals(conditionElement.getName())) {
            condition = new Clause(conditionName, conditionElement.getText(),
                    getJoinValue(conditionElement),
                    conditionElement.attributeValue("operatorType"),
                    conditionElement.attributeValue("type"));
            Set<ParameterInfo> params = new HashSet<>();
            for (Element paramElem : conditionElement.elements("param")) {
                Set<ParameterInfo> singleParam = ParametersHelper.parseQuery(":" + paramElem.attributeValue("name"));
                Attribute javaClass = paramElem.attribute("javaClass");
                if (javaClass != null) {
                    for (ParameterInfo param : singleParam) {
                        //noinspection CatchMayIgnoreException
                        try {
                            param.setJavaClass(ReflectionHelper.loadClass(javaClass.getValue()));
                            param.setConditionName(conditionName);
                            param.setValue(paramElem.getText());
                        } catch (ClassNotFoundException e) {
                        }
                    }
                }
                params.addAll(singleParam);
            }
            ((Clause)condition).setInputParameters(params);
        } else {
            condition = new LogicalCondition(conditionName, LogicalOp.fromString(conditionElement.getName()));
        }

        return condition;
    }

    protected String getJoinValue(Element conditionElement) {
        Element joinElement = conditionElement.element("join");
        String join;
        if (joinElement != null) {
            join = joinElement.getText();
        } else {
            //for backward compatibility
            join = conditionElement.attributeValue("join");
        }
        return join;
    }

    protected void parse(Element parentElem, List<Condition> conditions) {
        for (Element element : parentElem.elements()) {
            if ("param".equals(element.getName()) || "join".equals(element.getName()))
                continue;

            Condition condition = createCondition(element);
            conditions.add(condition);
            parse(element, condition.getConditions());
        }
    }
}
