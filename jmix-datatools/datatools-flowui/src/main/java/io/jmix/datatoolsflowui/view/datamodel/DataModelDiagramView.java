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

package io.jmix.datatoolsflowui.view.datamodel;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import io.jmix.datatoolsflowui.datamodel.DataModelDiagramStorage;
import io.jmix.flowui.component.image.JmixImage;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Route(value = "datatl/data-model-diagram/:id?", layout = DefaultMainViewParent.class)
@ViewController(id = "datatl_dataModelDiagramView")
@ViewDescriptor(path = "data-model-diagram-view.xml")
public class DataModelDiagramView extends StandardView implements DiagramView {

    @ViewComponent
    private JmixImage<?> diagramImage;

    @Autowired
    protected UrlParamSerializer urlParamSerializer;
    @Autowired
    protected DataModelDiagramStorage dataModelDiagramStorage;

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);

        event.getRouteParameters().get("id")
                .map(id ->
                        urlParamSerializer.deserialize(UUID.class, id))
                .ifPresent(id ->
                        setDiagramData(dataModelDiagramStorage.get(id))
                );
    }

    @Override
    public void setDiagramData(@Nullable byte[] diagramData) {
        DownloadHandler downloadHandler = DownloadHandler.fromInputStream(e ->
                new DownloadResponse(
                        diagramData != null
                                ? new ByteArrayInputStream(diagramData)
                                : InputStream.nullInputStream(),
                        "data-model-er-diagram.png",
                        null,
                        diagramData != null ? diagramData.length : 0));

        diagramImage.setSrc(downloadHandler);
    }
}