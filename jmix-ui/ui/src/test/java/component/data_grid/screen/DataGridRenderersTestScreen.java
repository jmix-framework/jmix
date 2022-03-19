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

package component.data_grid.screen;

import io.jmix.ui.component.DataGrid;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.model_objects.RendererObject;

@UiController
@UiDescriptor("datagrid-renderers-test-screen.xml")
public class DataGridRenderersTestScreen extends Screen {
    @Autowired
    protected DataGrid<RendererObject> renderersDataGrid;
    @Autowired
    protected ObjectProvider<DataGrid.ButtonRenderer<RendererObject>> buttonRendererObjectProvider;
    @Autowired
    protected ObjectProvider<DataGrid.ClickableTextRenderer<RendererObject>> clickableTextRendererObjectProvider;
    @Autowired
    protected ObjectProvider<DataGrid.ImageRenderer<RendererObject>> imageRendererObjectProvider;

    @Subscribe
    public void onInit(InitEvent event) {
        DataGrid.ButtonRenderer<RendererObject> buttonRenderer = buttonRendererObjectProvider.getObject();
        buttonRenderer.setNullRepresentation("buttonRenderer");
        renderersDataGrid.getColumnNN("button").setRenderer(buttonRenderer);

        DataGrid.ClickableTextRenderer<RendererObject> clickableTextRenderer =
                clickableTextRendererObjectProvider.getObject();
        clickableTextRenderer.setNullRepresentation("clickableTextRenderer");
        renderersDataGrid.getColumnNN("clickableText").setRenderer(clickableTextRenderer);

        DataGrid.ImageRenderer<RendererObject> imageRenderer = imageRendererObjectProvider.getObject();
        renderersDataGrid.getColumnNN("image").setRenderer(imageRenderer);
    }
}
