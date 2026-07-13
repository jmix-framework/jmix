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

package meta_component_preview;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import io.jmix.flowui.kit.meta.component.preview.processor.StudioTabComponentProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudioTabComponentProcessorTest {

    final StudioTabComponentProcessor processor = new StudioTabComponentProcessor();

    @Test
    void testAddTabAppendsWithContentAndRemoveDetachesByTabIdentity() {
        TabSheet tabSheet = new TabSheet();
        Tab tab = new Tab("First");
        Div content = new Div();

        assertTrue(processor.addTab(tabSheet, tab, content, -1));

        assertEquals(1, tabSheet.getTabCount());
        assertEquals(content, tabSheet.getComponent(tab));

        assertTrue(processor.removeTab(tabSheet, tab));

        assertEquals(0, tabSheet.getTabCount());
    }

    @Test
    void testAddTabInsertsAtIndex() {
        TabSheet tabSheet = new TabSheet();
        Tab first = new Tab("First");
        Tab second = new Tab("Second");
        tabSheet.add(first, new Div());

        assertTrue(processor.addTab(tabSheet, second, new Div(), 0));

        assertEquals(0, tabSheet.getIndexOf(second));
        assertEquals(1, tabSheet.getIndexOf(first));
    }

    @Test
    void testAddAndRemoveTabReturnFalseForNonTabSheetParentOrNonTabChild() {
        VerticalLayout layout = new VerticalLayout();
        Tab tab = new Tab("First");
        Div content = new Div();

        assertFalse(processor.addTab(layout, tab, content, -1));
        assertFalse(processor.removeTab(layout, tab));

        TabSheet tabSheet = new TabSheet();
        assertFalse(processor.addTab(tabSheet, content, content, -1));
    }
}
