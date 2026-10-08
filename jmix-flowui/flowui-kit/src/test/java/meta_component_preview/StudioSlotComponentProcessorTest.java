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

import java.util.List;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.meta.component.preview.processor.StudioSlotComponentProcessor;
import org.junit.jupiter.api.Test;

import static io.jmix.flowui.kit.meta.component.preview.StudioPreviewSlotProcessor.CONTENT_SLOT;
import static io.jmix.flowui.kit.meta.component.preview.StudioPreviewSlotProcessor.DRAWER_SLOT;
import static io.jmix.flowui.kit.meta.component.preview.StudioPreviewSlotProcessor.NAVBAR_SLOT;
import static io.jmix.flowui.kit.meta.component.preview.StudioPreviewSlotProcessor.PREFIX_SLOT;
import static io.jmix.flowui.kit.meta.component.preview.StudioPreviewSlotProcessor.SUFFIX_SLOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudioSlotComponentProcessorTest {

    final StudioSlotComponentProcessor processor = new StudioSlotComponentProcessor();

    @Test
    void testPrefixSlotAddSetsPrefixComponentAndRemoveClearsIt() {
        TextField textField = new TextField();
        Div prefix = new Div();

        assertTrue(processor.addToSlot(textField, prefix, -1, PREFIX_SLOT));
        assertEquals(prefix, textField.getPrefixComponent());

        assertTrue(processor.removeFromSlot(textField, prefix, PREFIX_SLOT));
        assertNull(textField.getPrefixComponent());
    }

    @Test
    void testSuffixSlotAddSetsSuffixComponentAndRemoveClearsIt() {
        TextField textField = new TextField();
        Div suffix = new Div();

        assertTrue(processor.addToSlot(textField, suffix, -1, SUFFIX_SLOT));
        assertEquals(suffix, textField.getSuffixComponent());

        assertTrue(processor.removeFromSlot(textField, suffix, SUFFIX_SLOT));
        assertNull(textField.getSuffixComponent());
    }

    @Test
    void testAppLayoutNavbarAndDrawerAddsLandInTheirOwnSlots() {
        AppLayout appLayout = new AppLayout();
        Div navbarItem = new Div();
        Div drawerItem = new Div();

        assertTrue(processor.addToSlot(appLayout, navbarItem, -1, NAVBAR_SLOT));
        assertTrue(processor.addToSlot(appLayout, drawerItem, -1, DRAWER_SLOT));

        assertEquals(navbarItem, SlotUtils.getChildInSlot(appLayout, "navbar"));
        assertEquals(drawerItem, SlotUtils.getChildInSlot(appLayout, "drawer"));
        // neither add may claim the work area
        assertNull(appLayout.getContent());
    }

    @Test
    void testAppLayoutContentSlotAddSetsContent() {
        AppLayout appLayout = new AppLayout();
        Div content = new Div();

        assertTrue(processor.addToSlot(appLayout, content, -1, CONTENT_SLOT));

        assertEquals(content, appLayout.getContent());
        assertTrue(appLayout.getChildren().anyMatch(component -> component == content));
    }

    /**
     * {@code AppLayout#remove(Component...)} is slot-agnostic, so a mismatched or unknown hint
     * still detaches.
     */
    @Test
    void testAppLayoutRemoveDetachesChildForAnySlotHint() {
        AppLayout appLayout = new AppLayout();
        Div navbarItem = new Div();
        Div content = new Div();
        appLayout.addToNavbar(navbarItem);
        appLayout.setContent(content);

        assertTrue(processor.removeFromSlot(appLayout, navbarItem, PREFIX_SLOT));
        assertTrue(processor.removeFromSlot(appLayout, content, "noSuchSlot"));

        assertNull(SlotUtils.getChildInSlot(appLayout, "navbar"));
        assertNull(appLayout.getContent());
        assertTrue(appLayout.getChildren().findAny().isEmpty());
    }

    @Test
    void testSlotHintUnsupportedByParentTypeIsDeclinedAndChangesNothing() {
        TextField textField = new TextField();
        Div existingPrefix = new Div();
        textField.setPrefixComponent(existingPrefix);
        Div rejected = new Div();

        assertFalse(processor.addToSlot(textField, rejected, -1, NAVBAR_SLOT));
        assertFalse(processor.removeFromSlot(textField, existingPrefix, NAVBAR_SLOT));

        assertEquals(existingPrefix, textField.getPrefixComponent());
        assertTrue(rejected.getParent().isEmpty());
    }

    @Test
    void testUnrelatedParentTypeIsDeclinedForAddAndRemove() {
        VerticalLayout layout = new VerticalLayout();
        Div child = new Div();

        assertFalse(processor.addToSlot(layout, child, -1, PREFIX_SLOT));
        assertFalse(processor.removeFromSlot(layout, child, PREFIX_SLOT));

        assertTrue(layout.getChildren().findAny().isEmpty());
    }

    @Test
    void testIndexIsIgnoredSoSlotAddsAlwaysAppend() {
        AppLayout appLayout = new AppLayout();
        Div first = new Div();
        Div second = new Div();

        assertTrue(processor.addToSlot(appLayout, first, -1, NAVBAR_SLOT));
        // a slot has no insertion API here, so even an explicit index appends
        assertTrue(processor.addToSlot(appLayout, second, 0, NAVBAR_SLOT));

        assertEquals(List.of(first, second), appLayout.getChildren().toList());
    }
}
