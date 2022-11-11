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

package io.jmix.flowui.component.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.component.ComponentContainer;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jmix.flowui.component.UiComponentUtils.sameId;


public class ViewLayout extends VerticalLayout implements ComponentContainer {

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getChildren()
                .filter(component -> sameId(component, id))
                .findFirst();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return getChildren().sequential().collect(Collectors.toList());
    }
}
