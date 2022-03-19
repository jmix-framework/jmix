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

import com.vaadin.event.LayoutEvents;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.MarginInfo;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.*;
import io.jmix.ui.widget.JmixGridLayout;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.jmix.ui.component.impl.WrapperUtils.toVaadinAlignment;

public class GridLayoutImpl extends AbstractComponent<JmixGridLayout> implements GridLayout {

    protected List<Component> ownComponents = new ArrayList<>();
    protected Registration layoutClickRegistration;
    protected Map<ShortcutAction, ShortcutListener> shortcuts;

    public GridLayoutImpl() {
        component = createComponent();
    }

    protected JmixGridLayout createComponent() {
        return new JmixGridLayout();
    }

    @Override
    public void add(Component childComponent) {
        if (childComponent.getParent() != null && childComponent.getParent() != this) {
            throw new IllegalStateException("Component already has parent");
        }

        com.vaadin.ui.Component vComponent = childComponent.unwrapComposition(com.vaadin.ui.Component.class);

        component.addComponent(vComponent);
        component.setComponentAlignment(vComponent, toVaadinAlignment(childComponent.getAlignment()));

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
    public float getColumnExpandRatio(int col) {
        return component.getColumnExpandRatio(col);
    }

    @Override
    public void setColumnExpandRatio(int col, float ratio) {
        component.setColumnExpandRatio(col, ratio);
    }

    @Override
    public float getRowExpandRatio(int row) {
        return component.getRowExpandRatio(row);
    }

    @Override
    public void setRowExpandRatio(int row, float ratio) {
        component.setRowExpandRatio(row, ratio);
    }

    @Override
    public void add(Component component, int col, int row) {
        add(component, col, row, col, row);
    }

    @Override
    public void add(Component childComponent, int col, int row, int col2, int row2) {
        if (childComponent.getParent() != null && childComponent.getParent() != this) {
            throw new IllegalStateException("Component already has parent");
        }

        com.vaadin.ui.Component vComponent = childComponent.unwrapComposition(com.vaadin.ui.Component.class);

        component.addComponent(vComponent, col, row, col2, row2);
        component.setComponentAlignment(vComponent, toVaadinAlignment(childComponent.getAlignment()));

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
    public int getRows() {
        return component.getRows();
    }

    @Override
    public void setRows(int rows) {
        component.setRows(rows);
    }

    @Override
    public int getColumns() {
        return component.getColumns();
    }

    @Override
    public void setColumns(int columns) {
        component.setColumns(columns);
    }

    @Nullable
    @Override
    public Component getComponent(int columnIndex, int rowIndex) {
        com.vaadin.ui.Component vComponent = this.component.getComponent(columnIndex, rowIndex);
        return findChildComponent(vComponent);
    }

    @Nullable
    @Override
    public Area getComponentArea(Component childComponent) {
        com.vaadin.ui.Component vComponent = childComponent.unwrapComposition(com.vaadin.ui.Component.class);
        com.vaadin.ui.GridLayout.Area vArea = component.getComponentArea(vComponent);
        if (vArea == null) {
            return null;
        }

        return new Area(childComponent,
                vArea.getColumn1(), vArea.getRow1(),
                vArea.getColumn2(), vArea.getRow2());
    }

    @Override
    public void remove(Component childComponent) {
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
    public void setMargin(io.jmix.ui.component.MarginInfo marginInfo) {
        MarginInfo vMargin = new MarginInfo(marginInfo.hasTop(), marginInfo.hasRight(), marginInfo.hasBottom(),
                marginInfo.hasLeft());
        component.setMargin(vMargin);
    }

    @Override
    public io.jmix.ui.component.MarginInfo getMargin() {
        MarginInfo vMargin = component.getMargin();
        return new io.jmix.ui.component.MarginInfo(vMargin.hasTop(), vMargin.hasRight(), vMargin.hasBottom(),
                vMargin.hasLeft());
    }

    @Override
    public void setSpacing(boolean enabled) {
        component.setSpacing(enabled);
    }

    @Override
    public boolean getSpacing() {
        return component.isSpacing();
    }

    @Override
    public Subscription addLayoutClickListener(Consumer<LayoutClickEvent> listener) {
        if (layoutClickRegistration == null) {
            LayoutEvents.LayoutClickListener layoutClickListener = event -> {
                Component childComponent = findChildComponent(event.getChildComponent());
                Component clickedComponent = findChildComponent(event.getClickedComponent());
                MouseEventDetails mouseEventDetails = WrapperUtils.toMouseEventDetails(event);

                LayoutClickEvent layoutClickEvent =
                        new LayoutClickEvent(this, childComponent, clickedComponent, mouseEventDetails);


                publish(LayoutClickEvent.class, layoutClickEvent);
            };
            layoutClickRegistration = component.addLayoutClickListener(layoutClickListener);
        }

        getEventHub().subscribe(LayoutClickEvent.class, listener);
        return () -> internalRemoveLayoutClickListener(listener);
    }

    @Nullable
    protected Component findChildComponent(com.vaadin.ui.Component vComponent) {
        for (Component component : getComponents()) {
            if (component.unwrapComposition(com.vaadin.ui.Component.class) == vComponent) {
                return component;
            }
        }
        return null;
    }

    protected void internalRemoveLayoutClickListener(Consumer<LayoutClickEvent> listener) {
        unsubscribe(LayoutClickEvent.class, listener);

        if (!hasSubscriptions(LayoutClickEvent.class)) {
            layoutClickRegistration.remove();
            layoutClickRegistration = null;
        }
    }

    @Override
    public void addShortcutAction(ShortcutAction action) {
        KeyCombination keyCombination = action.getShortcutCombination();
        com.vaadin.event.ShortcutListener shortcut =
                new ContainerShortcutActionWrapper(action, this, keyCombination);
        component.addShortcutListener(shortcut);

        if (shortcuts == null) {
            shortcuts = new HashMap<>(4);
        }
        shortcuts.put(action, shortcut);
    }

    @Override
    public void removeShortcutAction(ShortcutAction action) {
        if (shortcuts != null) {
            component.removeShortcutListener(shortcuts.remove(action));

            if (shortcuts.isEmpty()) {
                shortcuts = null;
            }
        }
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
