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

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractSplitPanel;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.AttachNotifier;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.SizeUnit;
import io.jmix.ui.component.SplitPanel;
import io.jmix.ui.widget.JmixDockableSplitPanel;
import io.jmix.ui.widget.JmixHorizontalSplitPanel;
import io.jmix.ui.widget.JmixVerticalSplitPanel;
import io.jmix.ui.widget.client.split.SplitPanelDockMode;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class SplitPanelImpl extends AbstractComponent<AbstractSplitPanel> implements SplitPanel {

    protected List<Component> ownComponents = new ArrayList<>(3);

    protected int orientation;

    protected float currentPosition = 0;
    protected boolean inverse = false;

    @Override
    public void add(Component childComponent) {
        if (childComponent.getParent() != null && childComponent.getParent() != this) {
            throw new IllegalStateException("Component already has parent");
        }

        if (component == null) {
            createComponentImpl();
        }

        com.vaadin.ui.Component vComponent = childComponent.unwrapComposition(com.vaadin.ui.Component.class);

        component.addComponent(vComponent);

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

    protected void createComponentImpl() {
        if (orientation == SplitPanel.ORIENTATION_HORIZONTAL) {
            component = new JmixHorizontalSplitPanel() {
                @Override
                public void setSplitPosition(float pos, Unit unit, boolean reverse) {
                    currentPosition = super.getSplitPosition();
                    inverse = super.isSplitPositionReversed();

                    super.setSplitPosition(pos, unit, reverse);
                }
            };
        } else {
            component = new JmixVerticalSplitPanel() {
                @Override
                public void setSplitPosition(float pos, Unit unit, boolean reverse) {
                    currentPosition = super.getSplitPosition();
                    inverse = super.isSplitPositionReversed();

                    super.setSplitPosition(pos, unit, reverse);
                }
            };
        }

        component.addSplitPositionChangeListener(this::fireSplitPositionChangeListener);
    }

    protected void fireSplitPositionChangeListener(AbstractSplitPanel.SplitPositionChangeEvent event) {
        SplitPositionChangeEvent jmixEvent = new SplitPositionChangeEvent(this,
                event.getOldSplitPosition(), event.getSplitPosition(), event.isUserOriginated());
        publish(SplitPositionChangeEvent.class, jmixEvent);
    }

    @Override
    public void remove(Component childComponent) {
        checkNotNullArgument(childComponent);

        component.removeComponent(childComponent.unwrapComposition(com.vaadin.ui.Component.class));
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
        checkNotNullArgument(id);

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
    public int getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(int orientation) {
        this.orientation = orientation;

        if (component == null) {
            createComponentImpl();
        }
    }

    @Override
    public void setSplitPosition(int pos) {
        component.setSplitPosition(pos);
    }

    @Override
    public void setSplitPosition(int pos, SizeUnit unit) {
        component.setSplitPosition(pos, WrapperUtils.toVaadinUnit(unit));
    }

    @Override
    public void setSplitPosition(int pos, SizeUnit unit, boolean reversePosition) {
        component.setSplitPosition(pos, WrapperUtils.toVaadinUnit(unit), reversePosition);
    }

    @Override
    public float getSplitPosition() {
        return component.getSplitPosition();
    }

    @Override
    public SizeUnit getSplitPositionSizeUnit() {
        return WrapperUtils.toSizeUnit(component.getSplitPositionUnit());
    }

    @Override
    public boolean isSplitPositionReversed() {
        return component.isSplitPositionReversed();
    }

    @Override
    public void setMinSplitPosition(int pos, SizeUnit unit) {
        component.setMinSplitPosition(pos, WrapperUtils.toVaadinUnit(unit));
    }

    @Override
    public float getMinSplitPosition() {
        return component.getMinSplitPosition();
    }

    @Override
    public SizeUnit getMinSplitPositionSizeUnit() {
        return WrapperUtils.toSizeUnit(component.getMinSplitPositionUnit());
    }

    @Override
    public void setMaxSplitPosition(int pos, SizeUnit unit) {
        component.setMaxSplitPosition(pos, WrapperUtils.toVaadinUnit(unit));
    }

    @Override
    public float getMaxSplitPosition() {
        return component.getMaxSplitPosition();
    }

    @Override
    public SizeUnit getMaxSplitPositionSizeUnit() {
        return WrapperUtils.toSizeUnit(component.getMaxSplitPositionUnit());
    }

    @Override
    public void setLocked(boolean locked) {
        component.setLocked(locked);
    }

    @Override
    public boolean isLocked() {
        return component.isLocked();
    }

    protected Unit convertLegacyUnit(int unit) {
        switch (unit) {
            case 0:
                return Unit.PIXELS;
            case 8:
                return Unit.PERCENTAGE;
            default:
                return Unit.PIXELS;
        }
    }

    @Override
    public void setDockable(boolean dockable) {
        ((JmixDockableSplitPanel) component).setDockable(dockable);
    }

    @Override
    public boolean isDockable() {
        return ((JmixDockableSplitPanel) component).isDockable();
    }

    @Override
    public void setDockMode(DockMode dockMode) {
        SplitPanelDockMode mode = SplitPanelDockMode.valueOf(dockMode.name());
        ((JmixDockableSplitPanel) component).setDockMode(mode);
    }

    @Override
    public DockMode getDockMode() {
        SplitPanelDockMode mode = ((JmixDockableSplitPanel) component).getDockMode();
        return DockMode.valueOf(mode.name());
    }

    @Override
    public Subscription addSplitPositionChangeListener(Consumer<SplitPositionChangeEvent> listener) {
        return getEventHub().subscribe(SplitPositionChangeEvent.class, listener);
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
