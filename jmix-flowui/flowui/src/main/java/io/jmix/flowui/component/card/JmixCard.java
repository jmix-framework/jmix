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
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.component.HasSubParts;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiPredicate;

import static io.jmix.flowui.component.UiComponentUtils.findFragment;

public class JmixCard extends Card implements HasSubParts {

    @Nullable
    @Override
    public Object getSubPart(String name) {
        if (getTitle() != null) {
            Optional<Component> component = findComponent(getTitle(), name);
            if (component.isPresent()) {
                return component.get();
            }
        }

        if (getSubtitle() != null) {
            Optional<Component> component = findComponent(getSubtitle(), name);
            if (component.isPresent()) {
                return component.get();
            }
        }

        if (getMedia() != null) {
            Optional<Component> component = findComponent(getMedia(), name);
            if (component.isPresent()) {
                return component.get();
            }
        }

        if (getHeaderPrefix() != null) {
            Optional<Component> component = findComponent(getHeaderPrefix(), name);
            if (component.isPresent()) {
                return component.get();
            }
        }

        if (getHeader() != null) {
            Optional<Component> component = findComponent(getHeader(), name);
            if (component.isPresent()) {
                return component.get();
            }
        }

        if (getHeaderSuffix() != null) {
            Optional<Component> component = findComponent(getHeaderSuffix(), name);
            if (component.isPresent()) {
                return component.get();
            }
        }

        for (Component footerComponent : getFooterComponents()) {
            Optional<Component> component = findComponent(footerComponent, name);
            if (component.isPresent()) {
                return component.get();
            }
        }

        return null;
    }

    protected Optional<Component> findComponent(Component component, String id) {
        BiPredicate<Component, String> idComparator = findFragment(component) == null
                ? UiComponentUtils::sameId
                : FragmentUtils::sameId;

        if (idComparator.test(component, id)) {
            return Optional.of(component);
        }

        return UiComponentUtils.findComponent(component, id);
    }
}
