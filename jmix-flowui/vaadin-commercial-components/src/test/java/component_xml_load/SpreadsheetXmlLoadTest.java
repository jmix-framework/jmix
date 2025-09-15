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

package component_xml_load;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import component_xml_load.view.SpreadsheetXmlLoadTestView;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.VaadinCommercialComponentsTestConfiguration;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"component_xml_load.view", "test_support.view"})
@SpringBootTest(classes = {VaadinCommercialComponentsTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class SpreadsheetXmlLoadTest {

    @Autowired
    protected ViewNavigationSupport viewNavigationSupport;

    @Test
    @DisplayName("Load Spreadsheet component from XML")
    public void loadSpreadsheetComponentFromXml() {
        viewNavigationSupport.navigate(SpreadsheetXmlLoadTestView.class);

        SpreadsheetXmlLoadTestView currentView = UiTestUtils.getCurrentView();

        Spreadsheet spreadsheet = currentView.spreadsheet;

        Assertions.assertAll(
                () -> assertNotNull(spreadsheet),
                () -> assertEquals("spreadsheet", spreadsheet.getId().orElse(null)),
                () -> assertEquals(0, spreadsheet.getActiveSheetIndex()),
                () -> assertEquals(0, spreadsheet.getActiveSheetPOIIndex()),
                () -> assertLinesMatch(Stream.of("hidefunctionbar", "hidetabsheet", "cssClassName1", "cssClassName2"),
                        spreadsheet.getClassNames().stream()),
                () -> assertEquals("red", spreadsheet.getStyle().get("color")),
                () -> assertFalse(spreadsheet.isChartsEnabled()),
                () -> assertEquals(10, spreadsheet.getDefaultColumnWidth()),
                () -> assertEquals(11, spreadsheet.getDefaultRowHeight()),
                () -> assertEquals(12, spreadsheet.getDefaultColumnCount()),
                () -> assertEquals(13, spreadsheet.getDefaultRowCount()),
                () -> assertEquals("0.000%", spreadsheet.getDefaultPercentageFormat()),
                () -> assertFalse(spreadsheet.isFunctionBarVisible()),
                () -> assertEquals("50px", spreadsheet.getHeight()),
                () -> assertEquals("55px", spreadsheet.getMaxHeight()),
                () -> assertEquals("120px", spreadsheet.getMaxWidth()),
                () -> assertEquals("40px", spreadsheet.getMinHeight()),
                () -> assertEquals("80px", spreadsheet.getMinWidth()),
                () -> assertEquals(40, spreadsheet.getMinimumRowHeightForComponents()),
                () -> assertEquals(14, spreadsheet.getColumns()),
                () -> assertEquals(15, spreadsheet.getRows()),
                () -> assertEquals(250, spreadsheet.getRowBufferSize()),
                () -> assertEquals(252, spreadsheet.getColBufferSize()),
                () -> assertTrue(spreadsheet.isReportStyle()),
                () -> assertFalse(spreadsheet.isSheetSelectionBarVisible()),
                () -> assertEquals("statusLabelValue", spreadsheet.getStatusLabelValue()),
                () -> assertEquals("invalidFormulaErrorMessage",
                        spreadsheet.getElement().getProperty("invalidFormulaErrorMessage")),
                () -> assertEquals("lumo", spreadsheet.getElement().getAttribute("theme")),
                () -> assertTrue(spreadsheet.isVisible()),
                () -> assertEquals("100px", spreadsheet.getWidth())
        );
    }
}
