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

import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import io.jmix.datatools.datamodel.DataModelSupport;
import io.jmix.flowui.component.image.JmixImage;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;

@Route(value = "datatl/data-model-diagram", layout = DefaultMainViewParent.class)
@ViewController(id = "datatl_dataModelDiagramView")
@ViewDescriptor(path = "data-model-diagram-view.xml")
public class DataModelDiagramView extends StandardView {

    @Autowired
    protected DataModelSupport dataModelSupport;

    @ViewComponent
    private JmixImage<Object> diagramImage;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        generateDiagram();
    }

    public void generateDiagram() {
        // hack to passing filtered entities list to this view that open in a browser tab
        byte[] rawResult = dataModelSupport.filteredModelsCount() == dataModelSupport.getDataModelProvider().getModelsCount()
                ? dataModelSupport.generateDiagram()
                : dataModelSupport.generateFilteredDiagram();

        DownloadHandler downloadHandler = DownloadHandler.fromInputStream(e ->
                new DownloadResponse(
                        new ByteArrayInputStream(rawResult),
                        "data-model-er-diagram.png",
                        null,
                        rawResult.length));

        diagramImage.setSrc(downloadHandler);
    }
}