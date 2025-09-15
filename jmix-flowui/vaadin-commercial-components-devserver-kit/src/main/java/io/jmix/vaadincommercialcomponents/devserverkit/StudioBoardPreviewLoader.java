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

package io.jmix.vaadincommercialcomponents.devserverkit;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import jakarta.annotation.Nullable;
import org.dom4j.Element;

public class StudioBoardPreviewLoader implements StudioPreviewComponentLoader {

    @Override
    public boolean isSupported(Element element) {
        return "http://jmix.io/schema/vaadin-commercial-components/ui".equals(element.getNamespaceURI())
                && "board".equals(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element element, Element viewElement) {
        Board resultComponent = new Board();

        loadEnabled(resultComponent, element);
        loadSizeAttributes(resultComponent, element);
        loadClassNames(resultComponent, element);

        return resultComponent;
    }
}
