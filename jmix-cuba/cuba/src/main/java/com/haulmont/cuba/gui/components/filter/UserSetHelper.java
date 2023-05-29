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

package com.haulmont.cuba.gui.components.filter;

import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.core.Entity;
import io.jmix.core.ReferenceToEntitySupport;
import io.jmix.core.common.util.Dom4j;
import com.haulmont.cuba.core.global.filter.ConditionType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UserSetHelper {
    public static String generateSetFilter(Set ids, String entityClass, String componentId, String entityAlias) {
        Document document = DocumentHelper.createDocument();
        Element root = DocumentHelper.createElement("filter");
        Element or = root.addElement("and");

        Element condition = or.addElement("c");
        condition.addAttribute("name", "set");
        condition.addAttribute("inExpr", "true");
        condition.addAttribute("hidden", "true");
        condition.addAttribute("locCaption", "Set filter");
        condition.addAttribute("entityAlias", entityAlias);
        condition.addAttribute("class", entityClass);
        condition.addAttribute("type", ConditionType.CUSTOM.name());

        String listOfId = createIdsString(ids);
        String randomName = RandomStringUtils.randomAlphabetic(10);
        condition.addText(entityAlias + ".id in :component$" + componentId + "." + randomName);

        Element param = condition.addElement("param");
        param.addAttribute("name", "component$" + componentId + "." + randomName);
        param.addAttribute("isFoldersFilterEntitiesSet", "true");
        param.addText(listOfId);

        document.add(root);
        return Dom4j.writeDocument(document, true);
    }

    public static Set<String> parseSet(String text) {
        Set<String> set = new HashSet<>();
        if ("NULL".equals(StringUtils.trimToEmpty(text)))
            return set;
        String[] ids = text.split(",");
        for (String id : ids) {
            String s = StringUtils.trimToNull(id);
            if (s != null)
                set.add(s);
        }
        return set;
    }

    public static String createIdsString(Set entities) {
        return createIdsString(new HashSet<>(), entities);
    }

    public static String createIdsString(Set<String> current, Collection entities) {
        Set<String> convertedSet = new HashSet<>();
        for (Object entity : entities) {
            Object id = getReferenceToEntitySupport().getReferenceIdForLink((Entity) entity);
            if (id != null) {
                convertedSet.add(id.toString());
            }
        }
        current.addAll(convertedSet);
        if (current.isEmpty()) {
            return "NULL";
        }
        StringBuilder listOfId = new StringBuilder();
        Iterator it = current.iterator();
        while (it.hasNext()) {
            listOfId.append(it.next());
            if (it.hasNext()) {
                listOfId.append(',');
            }
        }
        return listOfId.toString();
    }

    public static String removeIds(Set<String> current, Collection entities) {
        Set<String> convertedSet = new HashSet<>();
        for (Object entity : entities) {
            Object id = getReferenceToEntitySupport().getReferenceIdForLink((Entity) entity);
            if (id != null) {
                convertedSet.add(id.toString());
            }
        }
        current.removeAll(convertedSet);
        if (current.isEmpty()) {
            return "NULL";
        }
        StringBuilder listOfId = new StringBuilder();
        Iterator it = current.iterator();
        while (it.hasNext()) {
            listOfId.append(it.next());
            if (it.hasNext()) {
                listOfId.append(',');
            }
        }
        return listOfId.toString();
    }

    public static String removeEntities(String filterXml, Collection ids) {
        Document document;
        try {
            document = DocumentHelper.parseText(filterXml);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        Element param = document.getRootElement().element("and").element("c").element("param");
        String currentIds = param.getTextTrim();
        Set<String> set = parseSet(currentIds);
        String listOfIds = removeIds(set, ids);
        param.setText(listOfIds);
        return document.asXML();
    }

    public static String addEntities(String filterXml, Collection ids) {
        Document document;
        try {
            document = DocumentHelper.parseText(filterXml);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        Element param = document.getRootElement().element("and").element("c").element("param");
        String currentIds = param.getTextTrim();
        Set<String> set = parseSet(currentIds);
        String listOfIds = createIdsString(set, ids);
        param.setText(listOfIds);
        return document.asXML();
    }

    private static ReferenceToEntitySupport getReferenceToEntitySupport() {
        return AppBeans.get(ReferenceToEntitySupport.class);
    }
}
