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

package meta_grapesjs_preview;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJsBlock;
import io.jmix.messagetemplatesflowui.kit.meta.loader.StudioGrapesJsPreviewLoader;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudioGrapesJsPreviewLoaderTest {

    static final Namespace MESSAGETEMPLATES_NS = Namespace.get("http://jmix.io/schema/messagetemplates/ui");
    static final Namespace OTHER_NS = Namespace.get("http://jmix.io/schema/flowui/view");

    final StudioGrapesJsPreviewLoader loader = new StudioGrapesJsPreviewLoader();

    /** Fake env backed by a message map, per the chart/grid loader tests' FakeEnv pattern. */
    static class FakeEnv implements StudioPreviewEnvironment {
        final Map<String, String> messages = new HashMap<>();

        @Override
        public String resolveMessage(String messageKey) {
            return messages.get(messageKey);
        }

        @Override
        public String propertyCaption(String dataContainerId, String metaClass, String propertyPath) {
            return null;
        }
    }

    BaseElement grapesJsElement(String... nameValuePairs) {
        BaseElement element = new BaseElement("grapesJs", MESSAGETEMPLATES_NS);
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            element.addAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return element;
    }

    BaseElement blocksElement(Element... blocks) {
        BaseElement element = new BaseElement("blocks", MESSAGETEMPLATES_NS);
        for (Element block : blocks) {
            element.add(block);
        }
        return element;
    }

    BaseElement blockElement(String... nameValuePairs) {
        BaseElement element = new BaseElement("block", MESSAGETEMPLATES_NS);
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            element.addAttribute(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return element;
    }

    @Test
    void testIsSupportedForGrapesJsElement() {
        assertTrue(loader.isSupported(grapesJsElement()));
    }

    @Test
    void testIsSupportedFalseForWrongNamespace() {
        assertFalse(loader.isSupported(new BaseElement("grapesJs", OTHER_NS)));
    }

    @Test
    void testIsSupportedFalseForWrongElementName() {
        assertFalse(loader.isSupported(new BaseElement("blocks", MESSAGETEMPLATES_NS)));
    }

    @Test
    void testLoadReturnsGrapesJs() {
        Component component = loader.load(grapesJsElement(), grapesJsElement());
        assertInstanceOf(GrapesJs.class, component);
    }

    @Test
    void testBaseAttributesApplied() {
        Element element = grapesJsElement("width", "100%", "visible", "false", "classNames", "foo bar");
        GrapesJs grapesJs = (GrapesJs) loader.load(element, element);

        assertEquals("100%", grapesJs.getWidth());
        assertFalse(grapesJs.isVisible());
        assertTrue(grapesJs.getClassNames().contains("foo"));
        assertTrue(grapesJs.getClassNames().contains("bar"));
    }

    @Test
    void testReadOnlyAttributeApplied() {
        Element element = grapesJsElement("readOnly", "true");
        GrapesJs grapesJs = (GrapesJs) loader.load(element, element);

        assertTrue(grapesJs.isReadOnly());
    }

    @Test
    void testReadOnlyDefaultsFalse() {
        GrapesJs grapesJs = (GrapesJs) loader.load(grapesJsElement(), grapesJsElement());

        assertFalse(grapesJs.isReadOnly());
    }

    @Test
    void testNoBlocksElementLeavesBlocksEmpty() {
        GrapesJs grapesJs = (GrapesJs) loader.load(grapesJsElement(), grapesJsElement());

        assertTrue(grapesJs.getBlocks().isEmpty());
    }

    @Test
    void testBlockRawAttributesApplied() {
        Element element = grapesJsElement();
        element.add(blocksElement(blockElement(
                "id", "text-block",
                "label", "Text",
                "category", "Basic",
                "icon", "vaadin:text-label",
                "content", "<div>Hello</div>",
                "attributes", "{\"title\":\"tip\"}")));

        GrapesJs grapesJs = (GrapesJs) loader.load(element, element);

        List<GrapesJsBlock> blocks = grapesJs.getBlocks();
        assertEquals(1, blocks.size());

        GrapesJsBlock block = blocks.get(0);
        assertEquals("text-block", block.getId());
        assertEquals("Text", block.getLabel());
        assertEquals("Basic", block.getCategory());
        assertEquals("vaadin:text-label", block.getIcon());
        assertEquals("<div>Hello</div>", block.getContent());
        assertEquals("{\"title\":\"tip\"}", block.getAttributes());
    }

    @Test
    void testBlockContentAndAttributesFromChildElementText() {
        Element element = grapesJsElement();

        BaseElement block = blockElement("id", "html-block");
        BaseElement contentElement = new BaseElement("content", MESSAGETEMPLATES_NS);
        contentElement.setText("<p>From child element</p>");
        block.add(contentElement);
        BaseElement attributesElement = new BaseElement("attributes", MESSAGETEMPLATES_NS);
        attributesElement.setText("{\"title\":\"tip\"}");
        block.add(attributesElement);

        element.add(blocksElement(block));

        GrapesJs grapesJs = (GrapesJs) loader.load(element, element);

        GrapesJsBlock resultBlock = grapesJs.getBlocks().get(0);
        assertEquals("<p>From child element</p>", resultBlock.getContent());
        assertEquals("{\"title\":\"tip\"}", resultBlock.getAttributes());
    }

    @Test
    void testBlockWithoutIdIsSkipped() {
        Element element = grapesJsElement();
        element.add(blocksElement(blockElement("label", "No id")));

        GrapesJs grapesJs = (GrapesJs) loader.load(element, element);

        assertTrue(grapesJs.getBlocks().isEmpty());
    }

    @Test
    void testMultipleBlocksLoadedInOrder() {
        Element element = grapesJsElement();
        element.add(blocksElement(
                blockElement("id", "first"),
                blockElement("id", "second")));

        GrapesJs grapesJs = (GrapesJs) loader.load(element, element);

        assertEquals(List.of("first", "second"),
                grapesJs.getBlocks().stream().map(GrapesJsBlock::getId).toList());
    }

    @Test
    void testBlockLabelMessageReferenceResolvedWithEnv() {
        FakeEnv env = new FakeEnv();
        env.messages.put("msg://block.label", "Text Block");

        Element element = grapesJsElement();
        element.add(blocksElement(blockElement("id", "text-block", "label", "msg://block.label")));

        GrapesJs grapesJs = (GrapesJs) loader.load(element, element, env);

        assertEquals("Text Block", grapesJs.getBlocks().get(0).getLabel());
    }

    @Test
    void testBlockLabelMessageReferenceFallsBackToRawKeyWithNoopEnv() {
        Element element = grapesJsElement();
        element.add(blocksElement(blockElement("id", "text-block", "label", "msg://unresolved.label")));

        // 2-arg load: routes through StudioPreviewEnvironment.NOOP.
        GrapesJs grapesJs = (GrapesJs) loader.load(element, element);

        assertEquals("msg://unresolved.label", grapesJs.getBlocks().get(0).getLabel());
    }

    @Test
    void testOwnedAspectsIsEmpty() {
        assertEquals(Set.of(), loader.ownedAspects(grapesJsElement()));
    }

    @Test
    void testDiscoverableViaServiceLoader() {
        var loaded = ServiceLoader.load(StudioPreviewComponentLoader.class).stream()
                .map(ServiceLoader.Provider::type)
                .toList();

        assertTrue(loaded.contains(StudioGrapesJsPreviewLoader.class));
    }
}
