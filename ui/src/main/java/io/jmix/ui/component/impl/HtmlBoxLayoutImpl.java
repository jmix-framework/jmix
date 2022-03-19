/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component.impl;

import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.AttachNotifier;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.HtmlBoxLayout;
import io.jmix.ui.widget.JmixCustomLayout;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class HtmlBoxLayoutImpl extends AbstractComponent<JmixCustomLayout> implements HtmlBoxLayout {

    protected List<Component> ownComponents = new ArrayList<>();

    public HtmlBoxLayoutImpl() {
        component = createComponent();
    }

    protected JmixCustomLayout createComponent() {
        return new JmixCustomLayout();
    }

    @Nullable
    @Override
    public String getTemplateName() {
        return component.getTemplateName();
    }

    @Override
    public void setTemplateName(@Nullable String templateName) {
        component.setTemplateName(templateName);
    }

    @Nullable
    @Override
    public String getTemplateContents() {
        return component.getTemplateContents();
    }

    @Override
    public void setTemplateContents(@Nullable String templateContents) {
        component.setTemplateContents(sanitize(templateContents));
    }

    @Override
    public void add(Component childComponent) {
        if (childComponent.getParent() != null && childComponent.getParent() != this) {
            throw new IllegalStateException("Component already has parent");
        }

        final com.vaadin.ui.Component vComponent = ComponentsHelper.getComposition(childComponent);

        if (childComponent.getId() != null) {
            component.addComponent(vComponent, childComponent.getId());
        } else {
            component.addComponent(vComponent);
        }

        if (frame != null) {
            if (childComponent instanceof BelongToFrame
                    && ((BelongToFrame) childComponent).getFrame() == null) {
                ((BelongToFrame) childComponent).setFrame(frame);
            } else {
                ((FrameImplementation) frame).registerComponent(childComponent);
            }
        }

        ownComponents.add(childComponent);

        childComponent.setParent(this);
    }

    @Override
    public void remove(Component childComponent) {
        if (childComponent.getId() != null) {
            component.removeComponent(childComponent.getId());
        } else {
            component.removeComponent(ComponentsHelper.getComposition(childComponent));
        }
        ownComponents.remove(childComponent);

        childComponent.setParent(null);
    }

    @Override
    public void removeAll() {
        component.removeAllComponents();

        Component[] components = ownComponents.toArray(new Component[0]);
        ownComponents.clear();

        for (Component childComponent : components) {
            childComponent.setParent(null);
        }
    }

    @Override
    public void setFrame(@Nullable Frame frame) {
        super.setFrame(frame);

        if (frame != null) {
            for (Component childComponent : ownComponents) {
                if (childComponent instanceof BelongToFrame
                        && ((BelongToFrame) childComponent).getFrame() == null) {
                    ((BelongToFrame) childComponent).setFrame(frame);
                }
            }
        }
    }

    @Nullable
    @Override
    public Component getOwnComponent(String id) {
        Preconditions.checkNotNullArgument(id);

        return ownComponents.stream()
                .filter(component -> Objects.equals(id, component.getId()))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    @Override
    public Component getComponent(String id) {
        return ComponentsHelper.getComponent(this, id);
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return Collections.unmodifiableCollection(ownComponents);
    }

    @Override
    public Stream<Component> getOwnComponentsStream() {
        return ownComponents.stream();
    }

    @Override
    public Collection<Component> getComponents() {
        return ComponentsHelper.getComponents(this);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return component.isRequiredIndicatorVisible();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
        component.setRequiredIndicatorVisible(visible);
    }

    @Override
    public void attached() {
        super.attached();

        for (Component component : ownComponents) {
            ((AttachNotifier) component).attached();
        }
    }

    @Override
    public void detached() {
        super.detached();

        for (Component component : ownComponents) {
            ((AttachNotifier) component).detached();
        }
    }
}
