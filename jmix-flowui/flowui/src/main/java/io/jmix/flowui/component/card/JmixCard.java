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

package io.jmix.flowui.component.card;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.card.Card;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.HasSubParts;
import jakarta.annotation.Nullable;

import java.util.Optional;

public class JmixCard extends Card implements HasSubParts {

    @Nullable
    @Override
    public Object getSubPart(String name) {
        if (getTitle() != null) {
            Optional<Component> component = UiComponentUtils.findComponent(getTitle(), name);
            if (component.isPresent()) {
                return component.get();
            }
        } else if (getSubtitle() != null) {
            Optional<Component> component = UiComponentUtils.findComponent(getSubtitle(), name);
            if (component.isPresent()) {
                return component.get();
            }
        } else if (getMedia() != null) {
            Optional<Component> component = UiComponentUtils.findComponent(getMedia(), name);
            if (component.isPresent()) {
                return component.get();
            }
        } else if (getHeaderPrefix() != null) {
            Optional<Component> component = UiComponentUtils.findComponent(getHeaderPrefix(), name);
            if (component.isPresent()) {
                return component.get();
            }
        } else if (getHeader() != null) {
            Optional<Component> component = UiComponentUtils.findComponent(getHeader(), name);
            if (component.isPresent()) {
                return component.get();
            }
        } else if (getHeaderSuffix() != null) {
            Optional<Component> component = UiComponentUtils.findComponent(getHeaderSuffix(), name);
            if (component.isPresent()) {
                return component.get();
            }
        }

        for (Component footerComponent : getFooterComponents()) {
            Optional<Component> component = UiComponentUtils.findComponent(footerComponent, name);
            if (component.isPresent()) {
                return component.get();
            }
        }

        return null;
    }
}
