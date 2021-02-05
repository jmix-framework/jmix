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

package component.data_grid

import component.data_grid.screen.DataGridRenderersTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.DataGrid
import io.jmix.ui.component.renderer.*
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.model_objects.RendererObject

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DataGridRenderersTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.data_grid"])
    }

    def "Renderer is applied for DataGrid column"(String id, Class<DataGrid.Renderer> rendererClass) {
        showTestMainScreen()

        def dataGridScreen = screens.create(DataGridRenderersTestScreen)
        dataGridScreen.show()

        when: "Screen is loaded"

        DataGrid<RendererObject> dataGrid = dataGridScreen.getWindow().getComponent("renderersDataGrid")

        then: "Renderer is applied for DataGrid column"

        dataGrid.getColumn(id).getRenderer().getClass() == rendererClass

        where:

        id              | rendererClass
        "button"        | ButtonRendererImpl
        "checkBox"      | CheckBoxRendererImpl
        "clickableText" | ClickableTextRendererImpl
        "component"     | ComponentRendererImpl
        "date"          | DateRendererImpl
        "icon"          | IconRendererImpl
        "image"         | ImageRendererImpl
        "html"          | HtmlRendererImpl
        "localDate"     | LocalDateRendererImpl
        "localDateTime" | LocalDateTimeRendererImpl
        "number"        | NumberRendererImpl
        "progressBar"   | ProgressBarRendererImpl
        "text"          | TextRendererImpl
    }

    def "NullRepresentation renderer parameter is applied for DataGrid column"(String id, String nullRepresentation) {
        showTestMainScreen()

        def dataGridScreen = screens.create(DataGridRenderersTestScreen)
        dataGridScreen.show()

        when: "Screen is loaded"

        DataGrid<RendererObject> dataGrid = dataGridScreen.getWindow().getComponent("renderersDataGrid")
        def renderer = dataGrid.getColumn(id).getRenderer()

        then: "NullRepresentation renderer parameter is applied for DataGrid column"

        renderer instanceof DataGrid.HasNullRepresentation
        ((DataGrid.HasNullRepresentation) renderer).getNullRepresentation() == nullRepresentation

        where:

        id              | nullRepresentation
        "button"        | "buttonRenderer"
        "clickableText" | "clickableTextRenderer"
        "date"          | "dateRenderer"
        "html"          | "htmlRenderer"
        "localDate"     | "localDateRenderer"
        "localDateTime" | "localDateTimeRenderer"
        "number"        | "numberRenderer"
        "text"          | "textRenderer"
    }

    def "FormatString renderer parameter is applied for DataGrid column"(String id, String formatString) {
        showTestMainScreen()

        def dataGridScreen = screens.create(DataGridRenderersTestScreen)
        dataGridScreen.show()

        when: "Screen is loaded"

        DataGrid<RendererObject> dataGrid = dataGridScreen.getWindow().getComponent("renderersDataGrid")
        def renderer = dataGrid.getColumn(id).getRenderer()

        then: "FormatString renderer parameter is applied for DataGrid column"

        renderer instanceof DataGrid.HasFormatString
        ((DataGrid.HasFormatString) renderer).getFormatString() == formatString

        where:

        id       | formatString
        "date"   | "yyyy-MM-dd HH:mm:ss"
        "number" | "%f"
    }

    def "FormatPattern renderer parameter is applied for DataGrid column"(String id, String formatPattern) {
        showTestMainScreen()

        def dataGridScreen = screens.create(DataGridRenderersTestScreen)
        dataGridScreen.show()

        when: "Screen is loaded"

        DataGrid<RendererObject> dataGrid = dataGridScreen.getWindow().getComponent("renderersDataGrid")
        def renderer = dataGrid.getColumn(id).getRenderer()

        then: "FormatPattern renderer parameter is applied for DataGrid column"

        renderer instanceof DataGrid.HasDateTimeFormatter
        ((DataGrid.HasDateTimeFormatter) renderer).getFormatPattern() == formatPattern

        where:

        id              | formatPattern
        "localDate"     | "dd/MM/YYYY"
        "localDateTime" | "dd/MM/YYYY HH:mm:ss"
    }
}
