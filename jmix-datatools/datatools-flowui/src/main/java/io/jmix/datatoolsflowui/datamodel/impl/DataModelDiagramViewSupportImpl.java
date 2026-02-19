/*
 * Copyright 2026 Haulmont.
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

package io.jmix.datatoolsflowui.datamodel.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.UuidProvider;
import io.jmix.datatoolsflowui.datamodel.DataModelDiagramViewSupport;
import io.jmix.datatoolsflowui.datamodel.DataModelDiagramStorage;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class DataModelDiagramViewSupportImpl implements DataModelDiagramViewSupport {

    @Autowired
    protected ViewRegistry viewRegistry;
    @Autowired
    protected DataModelDiagramStorage dataModelDiagramStorage;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;

    @Override
    public void open(byte[] diagramData) {
        Class<? extends Component> navigationTarget = viewRegistry.getViewInfo("datatl_dataModelDiagramView")
                .getControllerClass();

        UUID id = UuidProvider.createUuidV7();
        dataModelDiagramStorage.put(id, diagramData);

        String url = viewRegistry.getRouteConfiguration()
                .getUrl(navigationTarget, new RouteParameters("id", urlParamSerializer.serialize(id)));
        UI.getCurrent().getPage().open(url);
    }
}
