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

package io.jmix.flowui.component.accordion;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.AccordionPanel;
import io.jmix.flowui.component.ComponentContainer;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

public class JmixAccordionPanel extends AccordionPanel implements ComponentContainer {

    public JmixAccordionPanel() {
        super();
    }

    public JmixAccordionPanel(String summary) {
        super(summary);
    }

    public JmixAccordionPanel(Component summary) {
        super(summary);
    }

    public JmixAccordionPanel(String summary, Component content) {
        super(summary, content);
    }

    public JmixAccordionPanel(Component summary, Component content) {
        super(summary, content);
    }

    public JmixAccordionPanel(String summary, Component... components) {
        super(summary, components);
    }

    public JmixAccordionPanel(Component summary, Component... components) {
        super(summary, components);
    }

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getContent()
                .filter(component -> sameId(component, id))
                .findFirst();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return getContent().sequential().collect(Collectors.toList());
    }
}
