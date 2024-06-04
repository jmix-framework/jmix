/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.sys;

import io.jmix.core.DevelopmentException;
import io.jmix.core.Resources;
import io.jmix.core.common.util.Dom4j;
import io.jmix.core.common.util.ParamsMap;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * Provides inheritance of screen XML descriptors.
 */
@Component("flowui_XmlInheritanceProcessor")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class XmlInheritanceProcessor {

    private static final Logger log = LoggerFactory.getLogger(XmlInheritanceProcessor.class);

    private Document document;
    private Namespace extNs;

    private List<ElementTargetLocator> targetLocators;

    protected Resources resources;
    protected ViewXmlParser viewXmlParser;
    protected ApplicationContext applicationContext;

    public XmlInheritanceProcessor(Document document) {
        this.document = document;

        extNs = document.getRootElement().getNamespaceForPrefix("ext");

        this.targetLocators = Arrays.asList(
                new FetchPlanPropertyElementTargetLocator(),
                new FetchPlanElementTargetLocator(),
                new ButtonElementTargetLocator(),
                new DataGridColumnElementTargetLocator(),
                new CommonElementTargetLocator()
        );
    }

    @Autowired
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @Autowired
    public void setViewXmlParser(ViewXmlParser viewXmlParser) {
        this.viewXmlParser = viewXmlParser;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Element getResultRoot() {
        Element result;

        Element root = document.getRootElement();
        String ancestorTemplate = root.attributeValue("extends");

        if (StringUtils.isNotEmpty(ancestorTemplate)) {
            Document ancestorDocument;
            try (InputStream ancestorStream = getAncestorStream(ancestorTemplate)) {
                ancestorDocument = viewXmlParser.parseDescriptor(ancestorStream);
            } catch (IOException e) {
                throw new RuntimeException("Unable to read ancestor XML document", e);
            }

            XmlInheritanceProcessor processor = applicationContext.getBean(XmlInheritanceProcessor.class, ancestorDocument);
            result = processor.getResultRoot();
            process(result, root);

            if (log.isTraceEnabled()) {
                StringWriter writer = new StringWriter();
                Dom4j.writeDocument(result.getDocument(), true, writer);
                log.trace("Resulting template:\n" + writer.toString());
            }
        } else {
            result = root;
        }

        return result;
    }

    protected InputStream getAncestorStream(String ancestorTemplate) {
        InputStream ancestorStream = resources.getResourceAsStream(ancestorTemplate);
        if (ancestorStream == null) {
            ancestorStream = XmlInheritanceProcessor.class.getResourceAsStream(ancestorTemplate);
            if (ancestorStream == null) {
                throw new DevelopmentException("Template is not found", "Ancestor's template path", ancestorTemplate);
            }
        }
        return ancestorStream;
    }

    protected void process(Element resultElem, Element extElem) {
        // set text
        if (!StringUtils.isBlank(extElem.getText()))
            resultElem.setText(extElem.getText());

        // add all attributes from extension
        for (Attribute attribute : extElem.attributes()) {
            if (resultElem == document.getRootElement() && attribute.getName().equals("extends")) {
                // ignore "extends" in root element
                continue;
            }
            resultElem.addAttribute(attribute.getName(), attribute.getValue());
        }

        String idx = extElem.attributeValue(new QName("index", extNs));
        if (resultElem != document.getRootElement() && StringUtils.isNotBlank(idx)) {
            int index = Integer.parseInt(idx);

            Element parent = resultElem.getParent();
            if (index < 0 || index > parent.elements().size()) {
                String message = String.format(
                        "Incorrect extension XML for screen. Could not move existing element %s to position %s",
                        resultElem.getName(), index);

                throw new DevelopmentException(message,
                        ParamsMap.of("element", resultElem.getName(), "index", index));
            }

            parent.remove(resultElem);
            parent.elements().add(index, resultElem);
        }

        // add and process elements
        Set<Element> justAdded = new HashSet<>();
        for (Element element : extElem.elements()) {
            // look for suitable locator
            ElementTargetLocator locator = null;
            for (ElementTargetLocator l : targetLocators) {
                if (l.suitableFor(element)) {
                    locator = l;
                    break;
                }
            }
            if (locator != null) {
                Element target = locator.locate(resultElem, element);
                // process target or a new element if target not found
                if (target != null) {
                    process(target, element);
                } else {
                    addNewElement(resultElem, element, justAdded);
                }
            } else {
                // if no suitable locator found, look for a single element with the same name
                List<Element> list = resultElem.elements(element.getName());
                if (list.size() == 1 && !justAdded.contains(list.get(0))) {
                    process(list.get(0), element);
                } else {
                    addNewElement(resultElem, element, justAdded);
                }
            }
        }
    }

    protected void addNewElement(Element resultElem, Element element, Set<Element> justAdded) {
        String idx = element.attributeValue(new QName("index", extNs));
        Element newElement;
        if (StringUtils.isBlank(idx)) {
            newElement = resultElem.addElement(element.getName());
        } else {
            newElement = DocumentHelper.createElement(element.getName());

            List<Element> elements = resultElem.elements();
            int index = Integer.parseInt(idx);
            if (index < 0 || index > elements.size()) {
                String message = String.format(
                        "Incorrect extension XML for screen. Could not paste new element %s to position %s",
                        newElement.getName(), index);

                throw new DevelopmentException(message,
                        ParamsMap.of("element", newElement.getName(), "index", index));
            }
            elements.add(index, newElement);
        }
        justAdded.add(newElement);
        process(newElement, element);
    }

    protected interface ElementTargetLocator {
        boolean suitableFor(Element extElem);
        @Nullable
        Element locate(Element resultParentElem, Element extElem);
    }

    protected static class CommonElementTargetLocator implements ElementTargetLocator {

        @Override
        public boolean suitableFor(Element extElem) {
            return !StringUtils.isBlank(extElem.attributeValue("id"));
        }

        @Nullable
        @Override
        public Element locate(Element resultParentElem, Element extElem) {
            String id = extElem.attributeValue("id");
            for (Element e : resultParentElem.elements()) {
                if (id.equals(e.attributeValue("id"))) {
                    return e;
                }
            }
            return null;
        }
    }

    protected static class FetchPlanElementTargetLocator implements ElementTargetLocator {

        private static final String FETCH_PLAN = "fetchPlan";

        @Override
        public boolean suitableFor(Element extElem) {
            return FETCH_PLAN.equals(extElem.getName());
        }

        @Nullable
        @Override
        public Element locate(Element resultParentElem, Element extElem) {
            String entity = extElem.attributeValue("entity");
            String clazz = extElem.attributeValue("class");
            String name = extElem.attributeValue("name");
            for (Element e : resultParentElem.elements()) {
                if (name != null) {
                    if (name.equals(e.attributeValue("name"))
                            && ((entity != null && entity.equals(e.attributeValue("entity")))
                            || (clazz != null && clazz.equals(e.attributeValue("class"))))) {
                        return e;
                    }
                }
            }

            Element extParentElem = extElem.getParent();
            if (extParentElem != null && Objects.equals(resultParentElem.attributeValue("id"),
                    extParentElem.attributeValue("id"))) {
                for (Element e : resultParentElem.elements()) {
                    if (FETCH_PLAN.equals(e.getName())) {
                        return e;
                    }
                }
            }

            return null;
        }
    }

    protected static class FetchPlanPropertyElementTargetLocator implements ElementTargetLocator {

        @Override
        public boolean suitableFor(Element extElem) {
            return "property".equals(extElem.getName());
        }

        @Nullable
        @Override
        public Element locate(Element resultParentElem, Element extElem) {
            String name = extElem.attributeValue("name");
            for (Element e : resultParentElem.elements()) {
                if (name.equals(e.attributeValue("name"))) {
                    return e;
                }
            }
            return null;
        }
    }

    protected static class ButtonElementTargetLocator implements ElementTargetLocator {

        @Override
        public boolean suitableFor(Element extElem) {
            return "button".equals(extElem.getName())
                    && extElem.attributeValue("id") == null
                    && extElem.attributeValue("action") != null;
        }

        @Nullable
        @Override
        public Element locate(Element resultParentElem, Element extElem) {
            String action = extElem.attributeValue("action");
            for (Element e : resultParentElem.elements()) {
                if (action.equals(e.attributeValue("action"))) {
                    return e;
                }
            }
            return null;
        }
    }

    protected static class DataGridColumnElementTargetLocator implements ElementTargetLocator {

        @Override
        public boolean suitableFor(Element extElem) {
            return "column".equals(extElem.getName())
                    && extElem.attributeValue("id") == null
                    && (extElem.attributeValue("property") != null || extElem.attributeValue("key") != null);
        }

        @Nullable
        @Override
        public Element locate(Element resultParentElem, Element extElem) {
            String attrName = extElem.attributeValue("property") != null ?
                    "property" : "key";

            String action = extElem.attributeValue(attrName);
            for (Element e : resultParentElem.elements()) {
                if (action.equals(e.attributeValue(attrName))) {
                    return e;
                }
            }
            return null;
        }
    }
}