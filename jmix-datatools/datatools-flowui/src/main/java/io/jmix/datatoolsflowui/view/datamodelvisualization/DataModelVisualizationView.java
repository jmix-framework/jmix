/*
 * Copyright 2025 Haulmont.
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

package io.jmix.datatoolsflowui.view.datamodelvisualization;


import com.vaadin.flow.router.Route;
import io.jmix.datatools.datamodelvisualization.DataModelVisualization;
import io.jmix.flowui.component.image.JmixImage;
import io.jmix.flowui.view.*;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Route(value = "data-model-visualization-view", layout = DefaultMainViewParent.class)
@ViewController(id = "DataModelVisualizationView")
@ViewDescriptor(path = "data-model-visualization-view.xml")
public class DataModelVisualizationView extends StandardView {

    @ViewComponent
    protected JmixImage<Object> diagramImage;

    @Autowired
    protected DataModelVisualization dataModelVisualization;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        drawImage();
    }

    @SuppressWarnings("all")
    protected void drawImage() {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            DiagramDescription dd = dataModelVisualization.createStringReader(outputStream);

            com.vaadin.flow.server.StreamResource streamResource = new com.vaadin.flow.server.StreamResource("diagram.png",
                    () -> new ByteArrayInputStream(outputStream.toByteArray()));
            diagramImage.setSrc(streamResource);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}