/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.Tree;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.xml.layout.loaders.TreeLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class CubaTreeLoader extends TreeLoader {

    @SuppressWarnings("rawtypes")
    @Override
    protected void loadDataContainer() {
        Element itemsElem = element.element("treechildren");
        if (itemsElem != null) {
            String datasource = itemsElem.attributeValue("datasource");
            if (!StringUtils.isBlank(datasource)) {

                ComponentLoaderContext loaderContext = (ComponentLoaderContext) getComponentContext();

                Datasource ds = loaderContext.getDsContext().get(datasource);
                if (!(ds instanceof HierarchicalDatasource)) {
                    throw new GuiDevelopmentException("Not a HierarchicalDatasource: " + datasource, context);
                }

                ((Tree) resultComponent).setDatasource((HierarchicalDatasource) ds);
            }
        }
    }
}
