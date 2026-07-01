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

package io.jmix.flowui.kit.meta.element;

import com.vaadin.flow.component.html.DescriptionList;
import io.jmix.flowui.kit.meta.StudioElement;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
interface StudioHTMLElements {

    @StudioElement(
            name = "Description",
            classFqn = "com.vaadin.flow.component.html.DescriptionList$Description",
            xmlElement = StudioXmlElements.DESCRIPTION,
            target = {"com.vaadin.flow.component.html.DescriptionList"},
            visible = true,
            propertyGroups = StudioPropertyGroups.HtmlTextElementDefaultProperties.class)
    DescriptionList.Description description();

    @StudioElement(
            name = "Term",
            classFqn = "com.vaadin.flow.component.html.DescriptionList$Term",
            xmlElement = StudioXmlElements.TERM,
            target = "com.vaadin.flow.component.html.DescriptionList",
            visible = true,
            propertyGroups = StudioPropertyGroups.HtmlTextElementDefaultProperties.class)
    DescriptionList.Term term();

    @StudioElement(
            name = "Html Content",
            xmlElement = StudioXmlElements.CONTENT,
            target = "com.vaadin.flow.component.Html"
    )
    void htmlContent();
}