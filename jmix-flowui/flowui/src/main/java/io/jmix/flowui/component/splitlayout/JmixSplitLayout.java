/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.splitlayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import io.jmix.flowui.component.ComponentContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

public class JmixSplitLayout extends SplitLayout implements ComponentContainer {

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getOwnComponents().stream()
                .filter(component -> sameId(component, id))
                .findAny();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        List<Component> ownComponents = new ArrayList<>();

        if (getPrimaryComponent() != null) {
            ownComponents.add(getPrimaryComponent());
        }

        if (getSecondaryComponent() != null) {
            ownComponents.add(getSecondaryComponent());
        }

        return ownComponents.isEmpty() ? Collections.emptyList() : List.copyOf(ownComponents);
    }
}
