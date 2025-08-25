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

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import component_xml_load.view.BoardXmlLoadTestView;
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
public class BoardXmlLoadTest {

    @Autowired
    protected ViewNavigationSupport viewNavigationSupport;

    @Test
    @DisplayName("Load Board component from XML")
    public void loadBoardComponentFromXml() {
        viewNavigationSupport.navigate(BoardXmlLoadTestView.class);

        BoardXmlLoadTestView currentView = UiTestUtils.getCurrentView();

        Board board = currentView.board;
        Row rootRow = currentView.rootRow;

        Assertions.assertAll(
                () -> assertNotNull(board),
                () -> assertNotNull(rootRow),
                () -> assertNotNull(currentView.rootRow_checkbox),
                () -> assertNotNull(currentView.nestedRow),
                () -> assertNotNull(currentView.nestedRow_checkbox)
        );

        Assertions.assertAll(
                () -> assertEquals("board", board.getId().orElse(null)),
                () -> assertLinesMatch(Stream.of("cssClassName1", "cssClassName2"), board.getClassNames().stream()),
                () -> assertEquals("red", board.getStyle().get("color")),
                () -> assertTrue(board.isEnabled()),
                () -> assertEquals("50px", board.getHeight()),
                () -> assertEquals("55px", board.getMaxHeight()),
                () -> assertEquals("120px", board.getMaxWidth()),
                () -> assertEquals("40px", board.getMinHeight()),
                () -> assertEquals("80px", board.getMinWidth()),
                () -> assertTrue(board.isVisible()),
                () -> assertEquals("100px", board.getWidth()),
                () -> assertEquals(2, board.getComponentCount())
        );

        Assertions.assertAll(
                () -> assertEquals("rootRow", rootRow.getId().orElse(null)),
                () -> assertLinesMatch(Stream.of("cssClassName1", "cssClassName2"), rootRow.getClassNames().stream()),
                () -> assertEquals("red", rootRow.getStyle().get("color")),
                () -> assertTrue(rootRow.isEnabled()),
                () -> assertEquals("50px", rootRow.getHeight()),
                () -> assertEquals("55px", rootRow.getMaxHeight()),
                () -> assertEquals("120px", rootRow.getMaxWidth()),
                () -> assertEquals("40px", rootRow.getMinHeight()),
                () -> assertEquals("80px", rootRow.getMinWidth()),
                () -> assertTrue(rootRow.isVisible()),
                () -> assertEquals("100px", rootRow.getWidth()),
                () -> assertEquals(2, board.getComponentCount())
        );
    }
}
