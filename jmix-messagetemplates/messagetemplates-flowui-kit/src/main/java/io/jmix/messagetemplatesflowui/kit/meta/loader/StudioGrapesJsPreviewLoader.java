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

package io.jmix.messagetemplatesflowui.kit.meta.loader;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.meta.component.preview.loader.PreviewActionSupport;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJsBlock;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Studio preview loader for the {@code msgtmp:grapesJs} component: instantiates an empty
 * {@link GrapesJs} with its pure-XML attributes and declared {@code <blocks>} applied, instead of
 * the reflection-fallback placeholder.
 * <p>
 * {@code <plugins>} is intentionally skipped: resolving a {@code plugin/@name} to a
 * {@code GrapesJsPlugin} instance goes through the (Spring-bean) {@code GrapesJsPluginRegistry} in
 * {@code messagetemplates-flowui}, which this spring-free kit module has no access to.
 * <p>
 * Studio has no block-specific post-processing to duplicate, so blocks are built unconditionally
 * (no {@link StudioPreviewEnvironment} handshake gate, unlike {@code StudioGridPreviewLoader}'s
 * columns), hence the default (empty) {@link #ownedAspects(Element)}.
 */
public class StudioGrapesJsPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String MESSAGETEMPLATES_SCHEMA = "http://jmix.io/schema/messagetemplates/ui";
    protected static final String GRAPES_JS_ELEMENT = "grapesJs";

    protected static final String BLOCKS_ELEMENT = "blocks";
    protected static final String BLOCK_ELEMENT = "block";

    @Override
    public boolean isSupported(Element element) {
        return MESSAGETEMPLATES_SCHEMA.equals(element.getNamespaceURI())
                && GRAPES_JS_ELEMENT.equals(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        GrapesJs grapesJs = new GrapesJs();

        loadComponentBaseAttributes(grapesJs, componentElement);
        loadBoolean(componentElement, "readOnly", grapesJs::setReadOnly);

        Element blocksElement = componentElement.element(BLOCKS_ELEMENT);
        if (blocksElement != null) {
            for (Element blockElement : blocksElement.elements(BLOCK_ELEMENT)) {
                loadBlock(grapesJs, blockElement, environment);
            }
        }

        return grapesJs;
    }

    /**
     * Builds a block from a {@code <block>} element. Silently skips it when {@code id} is missing:
     * the runtime loader throws in that case, but a preview should degrade gracefully rather than
     * fail the whole component.
     */
    protected void loadBlock(GrapesJs grapesJs, Element blockElement, StudioPreviewEnvironment environment) {
        String id = loadString(blockElement, "id").orElse(null);
        if (id == null) {
            return;
        }

        GrapesJsBlock block = new GrapesJsBlock(id);

        loadString(blockElement, "label")
                .ifPresent(label -> block.setLabel(PreviewActionSupport.resolveText(environment, label)));
        loadString(blockElement, "category")
                .ifPresent(category -> block.setCategory(PreviewActionSupport.resolveText(environment, category)));
        loadString(blockElement, "icon", block::setIcon);
        loadStringOrText(blockElement, "content").ifPresent(block::setContent);
        loadStringOrText(blockElement, "attributes").ifPresent(block::setAttributes);

        grapesJs.addBlock(block);
    }

    /**
     * Loads {@code childName} as the trimmed text of a child element if present (the XSD models
     * {@code content}/{@code attributes} as mixed-content elements), falling back to the
     * same-named attribute otherwise.
     */
    protected Optional<String> loadStringOrText(Element element, String childName) {
        Element childElement = element.element(childName);
        if (childElement != null) {
            String text = childElement.getTextTrim();
            return text.isEmpty() ? Optional.empty() : Optional.of(text);
        }
        return loadString(element, childName);
    }
}
