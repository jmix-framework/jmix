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

package io.jmix.core.querycondition;

import io.jmix.core.common.util.Dom4j;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Loads the tree of {@link Condition}s from XML.
 * <p>
 * Use {@link #addFactory(String, Function)} method to add your own functions creating conditions from XML elements.
 * By default, {@link LogicalCondition} and {@link JpqlCondition} are supported.
 */
@Component("core_ConditionXmlLoader")
public class ConditionXmlLoader {

    private Map<String, Function<Element, Condition>> factories = new LinkedHashMap<>();

    public ConditionXmlLoader() {
        factories.put("and",
                element -> {
                    if (element.getName().equals("and")) {
                        Condition condition = new LogicalCondition(LogicalCondition.Type.AND);
                        for (Element el : element.elements()) {
                            ((LogicalCondition) condition).getConditions().add(fromXml(el));
                        }
                        return condition;
                    } else {
                        return null;
                    }
                });
        factories.put("or",
                element -> {
                    if (element.getName().equals("or")) {
                        Condition condition = new LogicalCondition(LogicalCondition.Type.OR);
                        for (Element el : element.elements()) {
                            ((LogicalCondition) condition).getConditions().add(fromXml(el));
                        }
                        return condition;
                    } else {
                        return null;
                    }
                });
        factories.put("jpql",
                element -> {
                    if (element.getName().equals("jpql")) {
                        JpqlCondition jpqlCondition = UIConditions.jpqlCondition();

                        Element whereElement = element.element("where");
                        if (whereElement != null) {
                            jpqlCondition.setWhere(whereElement.getText());
                        }

                        Element joinElement = element.element("join");
                        if (joinElement != null) {
                            jpqlCondition.setJoin(joinElement.getText());
                        }

                        return jpqlCondition;
                    }
                    return null;
                });
    }

    /**
     * Adds a function creating a condition from XML element.
     *
     * @param name    name that can be used later in {@link #removeFactory(String)} method to remove the function
     * @param factory function creating a condition from XML element
     */
    public void addFactory(String name, Function<Element, Condition> factory) {
        factories.put(name, factory);
    }

    /**
     * Removes a factory by its name.
     */
    public void removeFactory(String name) {
        factories.remove(name);
    }

    /**
     * Creates a conditions tree from XML string.
     */
    public Condition fromXml(String xml) {
        Element element = Dom4j.readDocument(xml).getRootElement();
        return fromXml(element);
    }

    /**
     * Creates a conditions tree from XML element.
     */
    public Condition fromXml(Element element) {
        for (Function<Element, Condition> factory : factories.values()) {
            Condition condition = factory.apply(element);
            if (condition != null)
                return condition;
        }
        throw new RuntimeException("Cannot create condition for element " + element.getName());
    }
}
