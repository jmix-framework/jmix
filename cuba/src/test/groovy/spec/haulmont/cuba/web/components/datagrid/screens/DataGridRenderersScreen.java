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

package spec.haulmont.cuba.web.components.datagrid.screens;

import com.haulmont.cuba.gui.components.DataGrid;
import com.haulmont.cuba.web.model.sample.RendererEntity;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController
@UiDescriptor("datagrid-renderers-screen.xml")
public class DataGridRenderersScreen extends Screen {
    @Autowired
    protected DataGrid<RendererEntity> renderersDataGrid;

    @Subscribe
    public void onInit(InitEvent event) {
        DataGrid.ButtonRenderer<RendererEntity> buttonRenderer = renderersDataGrid.createRenderer(DataGrid.ButtonRenderer.class);
        buttonRenderer.setNullRepresentation("buttonRenderer");
        renderersDataGrid.getColumn("button").setRenderer(buttonRenderer);

        DataGrid.ClickableTextRenderer<RendererEntity> clickableTextRenderer = renderersDataGrid.createRenderer(DataGrid.ClickableTextRenderer.class);
        clickableTextRenderer.setNullRepresentation("clickableTextRenderer");
        renderersDataGrid.getColumn("clickableText").setRenderer(clickableTextRenderer);

        DataGrid.ImageRenderer<RendererEntity> imageRenderer = renderersDataGrid.createRenderer(DataGrid.ImageRenderer.class);
        renderersDataGrid.getColumn("image").setRenderer(imageRenderer);
    }
}
