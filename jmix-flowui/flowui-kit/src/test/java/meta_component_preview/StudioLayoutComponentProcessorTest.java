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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import io.jmix.flowui.kit.meta.component.preview.processor.StudioLayoutComponentProcessor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudioLayoutComponentProcessorTest {

    final StudioLayoutComponentProcessor processor = new StudioLayoutComponentProcessor();

    @Test
    void testSupportsScrollerSplitLayoutAccordionDetailsAndGenericHasComponents() {
        assertTrue(processor.isSupported(new Scroller()));
        assertTrue(processor.isSupported(new SplitLayout()));
        assertTrue(processor.isSupported(new Accordion()));
        assertTrue(processor.isSupported(new Details()));
        assertTrue(processor.isSupported(new VerticalLayout()));
    }

    @Test
    void testDoesNotSupportComponentWithoutAChildSlot() {
        Component leaf = new Text("leaf");
        assertFalse(processor.isSupported(leaf));
        assertFalse(processor.addChild(leaf, new Div(), -1));
        assertFalse(processor.removeChild(leaf, new Div()));
    }

    @Test
    void testScrollerAddSetsSingleContentAndRemoveClearsIt() {
        Scroller scroller = new Scroller();
        Div content = new Div();

        assertTrue(processor.addChild(scroller, content, -1));
        assertEquals(content, scroller.getContent());

        assertTrue(processor.removeChild(scroller, content));
        assertNull(scroller.getContent());
    }

    @Test
    void testSplitLayoutFillsPrimaryThenSecondaryAndRemoveDetaches() {
        SplitLayout splitLayout = new SplitLayout();
        Div first = new Div();
        Div second = new Div();

        assertTrue(processor.addChild(splitLayout, first, -1));
        assertEquals(first, splitLayout.getPrimaryComponent());

        assertTrue(processor.addChild(splitLayout, second, -1));
        assertEquals(second, splitLayout.getSecondaryComponent());

        assertTrue(processor.removeChild(splitLayout, first));
        assertNull(splitLayout.getPrimaryComponent());
        assertEquals(second, splitLayout.getSecondaryComponent());
    }

    @Test
    void testAccordionAddsPanelAndRemoveDetachesIt() {
        Accordion accordion = new Accordion();
        AccordionPanel panel = new AccordionPanel("summary", new Div());

        assertTrue(processor.addChild(accordion, panel, -1));
        assertTrue(accordion.getChildren().anyMatch(component -> component == panel));

        assertTrue(processor.removeChild(accordion, panel));
        assertTrue(accordion.getChildren().noneMatch(component -> component == panel));
    }

    @Test
    void testAccordionRejectsNonPanelChild() {
        Accordion accordion = new Accordion();
        assertFalse(processor.addChild(accordion, new Div(), -1));
    }

    @Test
    void testDetailsAddsAndRemovesContentLikeGenericHasComponents() {
        Details details = new Details();
        Div content = new Div();

        assertTrue(processor.addChild(details, content, -1));
        assertTrue(details.getContent().anyMatch(component -> component == content));

        assertTrue(processor.removeChild(details, content));
        assertTrue(details.getContent().noneMatch(component -> component == content));
    }

    @Test
    void testGenericHasComponentsAppendsWhenIndexNegative() {
        VerticalLayout layout = new VerticalLayout();
        Div first = new Div();
        Div second = new Div();
        layout.add(first);

        assertTrue(processor.addChild(layout, second, -1));

        assertEquals(List.of(first, second), layout.getChildren().toList());
    }

    @Test
    void testGenericHasComponentsInsertsAtIndex() {
        VerticalLayout layout = new VerticalLayout();
        Div first = new Div();
        Div second = new Div();
        layout.add(first);

        assertTrue(processor.addChild(layout, second, 0));

        assertEquals(List.of(second, first), layout.getChildren().toList());
    }

    @Test
    void testGenericHasComponentsRemoveDetaches() {
        VerticalLayout layout = new VerticalLayout();
        Div child = new Div();
        layout.add(child);

        assertTrue(processor.removeChild(layout, child));

        assertTrue(layout.getChildren().noneMatch(component -> component == child));
    }
}
