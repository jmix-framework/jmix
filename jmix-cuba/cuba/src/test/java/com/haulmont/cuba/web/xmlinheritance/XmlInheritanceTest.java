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

package com.haulmont.cuba.web.xmlinheritance;

import com.haulmont.cuba.web.testsupport.WebTest;
import io.jmix.core.Resources;
import io.jmix.core.common.util.Dom4j;
import io.jmix.ui.sys.ScreenXmlParser;
import io.jmix.ui.sys.XmlInheritanceProcessor;
import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("ReassignmentInjectVariable")
@WebTest
@ExtendWith(SpringExtension.class)
public class XmlInheritanceTest {

    @Autowired
    protected Resources resources;
    @Autowired
    protected ScreenXmlParser screenXmlParser;
    @Autowired
    protected ApplicationContext applicationContext;

    @Test
    public void testExtIndexNew() {
        int index = getIndexOfMovedField("com/haulmont/cuba/web/xmlinheritance/test-extends-screen-new.xml", "test");

        assertEquals(3, index);
    }

    @Test
    public void testExtIndexExtended() {
        int index = getIndexOfMovedField("com/haulmont/cuba/web/xmlinheritance/test-extends-screen-old.xml", "login");

        assertEquals(3, index);
    }

    @Test
    public void testExtIndexExtendedDown() {
        int index = getIndexOfMovedField("com/haulmont/cuba/web/xmlinheritance/test-extends-screen-old-down.xml", "name");

        assertEquals(0, index);
    }

    @Test
    public void testExtIndexExtendedUp() {
        Document document = Dom4j.readDocument(
                resources.getResourceAsStream(
                        "com/haulmont/cuba/web/xmlinheritance/test-extends-screen-old-up.xml"));

        XmlInheritanceProcessor processor =
                applicationContext.getBean(XmlInheritanceProcessor.class, document, emptyMap());

        Element resultXml = processor.getResultRoot();

        Element layoutElement = resultXml.element("layout");
        Element fieldGroupElement = layoutElement.element("fieldGroup");
        Element columnElement = fieldGroupElement.element("column");
        //noinspection unchecked
        List<Element> fieldElements = columnElement.elements("field");

        int index = 0;
        for (Element fieldElement : fieldElements) {
            if ("login".equals(fieldElement.attributeValue("id"))) {
                break;
            }
            index++;
        }

        assertEquals(7, index);
    }

    private int getIndexOfMovedField(String extendedXml, String fieldName) {
        Document document = Dom4j.readDocument(resources.getResourceAsStream(extendedXml));

        XmlInheritanceProcessor processor =
                applicationContext.getBean(XmlInheritanceProcessor.class, document, emptyMap());

        Element resultXml = processor.getResultRoot();

        Element layoutElement = resultXml.element("layout");
        Element fieldGroupElement = layoutElement.element("fieldGroup");
        Element columnElement = fieldGroupElement.element("column");
        //noinspection unchecked
        List<Element> fieldElements = columnElement.elements("field");

        int index = 0;
        for (Element fieldElement : fieldElements) {
            if (fieldName.equals(fieldElement.attributeValue("id"))) {
                break;
            }
            index++;
        }
        return index;
    }
}