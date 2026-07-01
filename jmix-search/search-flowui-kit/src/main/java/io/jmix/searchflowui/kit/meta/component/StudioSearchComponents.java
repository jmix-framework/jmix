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

package io.jmix.searchflowui.kit.meta.component;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
public interface StudioSearchComponents {

    @StudioComponent(
            name = "SearchField",
            classFqn = "io.jmix.searchflowui.component.SearchField",
            category = "Components",
            xmlElement = StudioXmlElements.SEARCH_FIELD,
            xmlns = "http://jmix.io/schema/search/ui",
            xmlnsAlias = "search",
            icon = "io/jmix/searchflowui/kit/meta/icon/component/searchField.svg",
            propertyGroups = StudioSearchPropertyGroups.SearchFieldComponent.class)
    TextField searchField();

    @StudioComponent(
            name = "Full-text filter",
            classFqn = "io.jmix.searchflowui.component.FullTextFilter",
            category = "Components",
            xmlElement = StudioXmlElements.FULL_TEXT_FILTER,
            xmlns = "http://jmix.io/schema/search/ui",
            xmlnsAlias = "search",
            icon = "io/jmix/searchflowui/kit/meta/icon/component/searchField.svg",
            propertyGroups = StudioSearchPropertyGroups.FullTextFilterComponent.class)
    HorizontalLayout fullTextFilter();
}

