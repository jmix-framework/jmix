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

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.meta.component.preview.processor.StudioSlotComponentProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudioSlotComponentProcessorTest {

    final StudioSlotComponentProcessor processor = new StudioSlotComponentProcessor();

    @Test
    void testPrefixSlotSetsAndClearsPrefixComponent() {
        TextField textField = new TextField();
        Div prefix = new Div();

        assertTrue(processor.addToSlot(textField, prefix, -1, "prefix"));
        assertEquals(prefix, textField.getPrefixComponent());

        assertTrue(processor.removeFromSlot(textField, prefix, "prefix"));
        assertNull(textField.getPrefixComponent());
    }

    @Test
    void testSuffixSlotSetsAndClearsSuffixComponent() {
        TextField textField = new TextField();
        Div suffix = new Div();

        assertTrue(processor.addToSlot(textField, suffix, -1, "suffix"));
        assertEquals(suffix, textField.getSuffixComponent());

        assertTrue(processor.removeFromSlot(textField, suffix, "suffix"));
        assertNull(textField.getSuffixComponent());
    }

    @Test
    void testMismatchedSlotHintForHasPrefixHasSuffixParentReturnsFalse() {
        TextField textField = new TextField();
        Div child = new Div();

        assertFalse(processor.addToSlot(textField, child, -1, "navbar"));
        assertFalse(processor.addToSlot(textField, child, -1, null));
    }

    @Test
    void testAppLayoutNavbarDrawerAndContentSlotsDispatchToRealVaadinApi() {
        AppLayout appLayout = new AppLayout();
        Div navbarItem = new Div();
        Div drawerItem = new Div();
        Div content = new Div();

        assertTrue(processor.addToSlot(appLayout, navbarItem, -1, "navbar"));
        assertTrue(appLayout.getChildren().anyMatch(component -> component == navbarItem));

        assertTrue(processor.addToSlot(appLayout, drawerItem, -1, "drawer"));
        assertTrue(appLayout.getChildren().anyMatch(component -> component == drawerItem));

        assertTrue(processor.addToSlot(appLayout, content, -1, "content"));
        assertEquals(content, appLayout.getContent());
    }

    @Test
    void testAppLayoutRejectsUnrecognizedSlotHint() {
        AppLayout appLayout = new AppLayout();
        assertFalse(processor.addToSlot(appLayout, new Div(), -1, "bogus"));
    }

    @Test
    void testAppLayoutRemoveIsSlotAgnostic() {
        AppLayout appLayout = new AppLayout();
        Div navbarItem = new Div();
        appLayout.addToNavbar(navbarItem);

        assertTrue(processor.removeFromSlot(appLayout, navbarItem, "navbar"));
        assertFalse(appLayout.getChildren().anyMatch(component -> component == navbarItem));

        Div content = new Div();
        appLayout.setContent(content);
        // AppLayout#remove is uniform: even a mismatched slotHint string still removes the child.
        assertTrue(processor.removeFromSlot(appLayout, content, "drawer"));
        assertNull(appLayout.getContent());
    }
}
