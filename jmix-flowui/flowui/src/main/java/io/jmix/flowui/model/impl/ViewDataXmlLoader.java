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

package io.jmix.flowui.model.impl;

import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.xml.layout.support.DataComponentsLoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @deprecated Use {@link DataComponentsLoaderSupport} instead
 */
@Deprecated(since = "2.3", forRemoval = true)
@Component("flowui_ViewDataXmlLoader")
public class ViewDataXmlLoader {

    public static final String GENERATED_PREFIX = "generated_";

    protected DataComponentsLoaderSupport dataComponentsLoaderSupport;

    @Autowired
    public void setDataComponentsLoaderSupport(DataComponentsLoaderSupport dataComponentsLoaderSupport) {
        this.dataComponentsLoaderSupport = dataComponentsLoaderSupport;
    }

    public void load(ViewData viewData, Element element) {
        dataComponentsLoaderSupport.load(viewData, element);
    }
}
