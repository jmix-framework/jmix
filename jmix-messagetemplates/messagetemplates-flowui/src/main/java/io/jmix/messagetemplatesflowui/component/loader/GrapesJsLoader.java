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

package io.jmix.messagetemplatesflowui.component.loader;

import com.google.common.base.Strings;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.messagetemplatesflowui.GrapesJsPluginRegistry;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJsBlock;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJsPlugin;
import io.jmix.messagetemplatesflowui.kit.component.JmixGrapesJs;
import org.dom4j.Element;

import java.util.function.Consumer;

public class GrapesJsLoader extends AbstractComponentLoader<JmixGrapesJs> {

    protected GrapesJsPluginRegistry grapesJsPluginRegistry;

    @Override
    protected JmixGrapesJs createComponent() {
        return factory.create(JmixGrapesJs.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);

        loadPlugins(resultComponent, element);
        loadBlocks(resultComponent, element);
    }

    protected void loadPlugins(JmixGrapesJs resultComponent, Element element) {
        Element plugins = element.element("plugins");
        if (plugins == null) {
            return;
        }

        for (Element pluginElement : plugins.elements("plugin")) {
            String name = loadString(pluginElement, "name")
                    .orElseThrow(
                            () -> new GuiDevelopmentException(
                                    "Name is required for %s".formatted(GrapesJsPlugin.class.getSimpleName()), context)
                    );
            GrapesJsPlugin foundPlugin = getGrapesJsPluginRegistry().get(name);
            resultComponent.addPlugin(foundPlugin);
        }
    }

    protected void loadBlocks(JmixGrapesJs resultComponent, Element element) {
        Element blocks = element.element("blocks");
        if (blocks == null) {
            return;
        }

        for (Element blockElement : blocks.elements("block")) {
            loadBlock(resultComponent, blockElement);
        }
    }

    protected void loadBlock(JmixGrapesJs resultComponent, Element element) {
        String id = loadString(element, "id")
                .orElseThrow(
                        () -> new GuiDevelopmentException(
                                "ID is required for %s".formatted(GrapesJsBlock.class.getSimpleName()), context
                        )
                );

        GrapesJsBlock block = new GrapesJsBlock(id);

        loadResourceString(element, "label", getContext().getMessageGroup(), block::setLabel);
        loadResourceString(element, "category", getContext().getMessageGroup(), block::setCategory);
        loadStringText(element, "content", block::setContent);
        loadStringText(element, "attributes", block::setAttributes);

        resultComponent.addBlock(block);
    }

    protected void loadStringText(Element element, String attributeName, Consumer<String> setter) {
        Element attributeElement = element.element(attributeName);

        if (attributeElement != null) {
            String text = attributeElement.getText();

            if (Strings.isNullOrEmpty(text)) {
                throw new GuiDevelopmentException(
                        String.format("'%s' element cannot be empty", attributeName), context);
            }

            setter.accept(text);
            return;
        }

        loadString(element, attributeName, setter);
    }

    protected GrapesJsPluginRegistry getGrapesJsPluginRegistry() {
        if (grapesJsPluginRegistry == null) {
            grapesJsPluginRegistry = applicationContext.getBean(GrapesJsPluginRegistry.class);
        }

        return grapesJsPluginRegistry;
    }
}
