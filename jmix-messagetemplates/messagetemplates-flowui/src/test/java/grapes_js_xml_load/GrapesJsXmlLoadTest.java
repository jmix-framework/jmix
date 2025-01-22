/*
 * Copyright 2024 Haulmont.
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

package grapes_js_xml_load;

import grapes_js_xml_load.view.GrapesJsXmlLoadTestView;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJsBlock;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJsPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.MessageTemplatesFlowuiTestConfiguration;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"grapes_js_xml_load.view", "test_support.view"})
@SpringBootTest(classes = {MessageTemplatesFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class GrapesJsXmlLoadTest {

    @Autowired
    protected ViewNavigationSupport viewNavigationSupport;

    @Test
    @DisplayName("Load GrapesJS component from XML")
    public void loadGrapesJsComponentFromXml() {
        viewNavigationSupport.navigate(GrapesJsXmlLoadTestView.class);

        GrapesJsXmlLoadTestView currentView = UiTestUtils.getCurrentView();

        GrapesJs grapesJs = currentView.grapesJs;

        assertNotNull(grapesJs);
        assertEquals("grapesJs", grapesJs.getId().orElseThrow());
        assertLinesMatch(Stream.of("cssClassName1", "cssClassName2"), grapesJs.getClassNames().stream());
        assertEquals("red", grapesJs.getStyle().get("color"));
        assertEquals("50px", grapesJs.getHeight());
        assertEquals("55px", grapesJs.getMaxHeight());
        assertEquals("120px", grapesJs.getMaxWidth());
        assertEquals("40px", grapesJs.getMinHeight());
        assertEquals("80px", grapesJs.getMinWidth());
        assertTrue(grapesJs.isVisible());
        assertTrue(grapesJs.isReadOnly());
        assertEquals("100px", grapesJs.getWidth());

        List<GrapesJsPlugin> plugins = grapesJs.getPlugins();
        assertNotNull(plugins);
        assertEquals(10, plugins.size());

        List<GrapesJsBlock> blocks = grapesJs.getBlocks();
        assertNotNull(blocks);
        assertEquals(1, blocks.size());

        GrapesJsBlock block = blocks.get(0);

        assertNotNull(block);
        assertEquals("myBlock", block.getId());
        assertEquals("Block label", block.getLabel());
        assertEquals("Extra", block.getCategory());
        assertEquals("<p>This is my first block</p>", block.getContent());
        assertEquals("{ \"title\": \"Insert h1 block\" }", block.getAttributes());
    }
}
