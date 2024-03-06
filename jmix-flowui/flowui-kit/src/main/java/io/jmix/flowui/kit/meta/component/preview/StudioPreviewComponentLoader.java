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

package io.jmix.flowui.kit.meta.component.preview;

import com.vaadin.flow.component.Component;
import jakarta.annotation.Nullable;
import org.dom4j.Element;

/**
 * Vaadin component loader for Studio view designer preview.
 * <h4>
 *     Register new loaders via SPI in {@code META-INF/services/io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader}
 * </h4>
 * @see StudioPreviewComponentProvider
 */
public interface StudioPreviewComponentLoader {

    /**
     * Define a components xml tags that this loader can load.
     */
    boolean isSuitable(String...componentTags);

    /**
     * Create vaadin component from component xml element.
     * @param componentElement xml element of component
     * @param viewElement xml element of view containing {@code componentElement}
     * @see Element
     */
    @Nullable
    Component load(Element componentElement, Element viewElement);
}
