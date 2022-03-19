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

import com.vaadin.server.Resource;
import com.vaadin.shared.Registration;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.AppUI;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.security.UiPermissionDescriptor;
import io.jmix.ui.security.UiPermissionValue;
import io.jmix.ui.settings.facet.ScreenSettingsFacetResolver;
import io.jmix.ui.sys.TestIdManager;
import io.jmix.ui.widget.JmixAccordion;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class AccordionImpl extends AbstractComponent<JmixAccordion>
        implements Accordion, UiPermissionAware, SupportsChildrenSelection {

    @Autowired
    protected ScreenSettingsFacetResolver settingsFacetResolver;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected IconResolver iconResolver;

    @Autowired
    protected Icons icons;

    protected boolean postInitTaskAdded;

    protected ComponentLoader.Context context;
    protected Map<String, Tab> tabs = new HashMap<>(4);

    protected Map<com.vaadin.ui.Component, ComponentDescriptor> tabMapping = new LinkedHashMap<>(4);

    protected Set<com.vaadin.ui.Component> lazyTabs;

    protected Registration selectedTabChangeListenerRegistration;

    public AccordionImpl() {
        component = createComponent();
    }

    protected JmixAccordion createComponent() {
        return new JmixAccordion();
    }

    protected Set<com.vaadin.ui.Component> getLazyTabs() {
        if (lazyTabs == null) {
            lazyTabs = new LinkedHashSet<>();
        }
        return lazyTabs;
    }

    @Override
    public void add(Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Component getOwnComponent(String id) {
        checkNotNullArgument(id);

        return tabMapping.values().stream()
                .filter(cd -> Objects.equals(id, cd.component.getId()))
                .map(cd -> cd.component)
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
        List<Component> componentList = new ArrayList<>();
        for (ComponentDescriptor cd : tabMapping.values()) {
            componentList.add(cd.component);
        }
        return componentList;
    }

    @Override
    public Stream<Component> getOwnComponentsStream() {
        return tabMapping.values().stream()
                .map(ComponentDescriptor::getComponent);
    }

    @Override
    public Collection<Component> getComponents() {
        return ComponentsHelper.getComponents(this);
    }

    @Nullable
    @Override
    public String getDescription() {
        return getComposition().getDescription();
    }

    @Override
    public void setDescription(@Nullable String description) {
        if (getComposition() instanceof com.vaadin.ui.AbstractComponent) {
            ((com.vaadin.ui.AbstractComponent) getComposition()).setDescription(description);
        }
    }

    @Override
    public void applyPermission(UiPermissionDescriptor permissionDescriptor) {
        checkNotNullArgument(permissionDescriptor);

        final String subComponentId = permissionDescriptor.getSubComponentId();
        final Accordion.Tab tab = getTab(subComponentId);
        if (tab != null) {
            UiPermissionValue permissionValue = permissionDescriptor.getPermissionValue();
            if (permissionValue == UiPermissionValue.HIDE) {
                tab.setVisible(false);
            } else if (permissionValue == UiPermissionValue.READ_ONLY) {
                tab.setEnabled(false);
            }
        } else {
            LoggerFactory.getLogger(AccordionImpl.class).info(String.format("Couldn't find component %s in window %s",
                    subComponentId, permissionDescriptor.getScreenId()));
        }
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public void setChildSelected(Component childComponent) {
        component.setSelectedTab(childComponent.unwrap(com.vaadin.ui.Component.class));
    }

    @Override
    public boolean isChildSelected(Component component) {
        return getSelectedTab().getComponent() == component;
    }

    protected class Tab implements Accordion.Tab {
        private String name;
        private Component tabComponent;
        private String icon;

        public Tab(String name, Component tabComponent) {
            this.name = name;
            this.tabComponent = tabComponent;
        }

        protected com.vaadin.ui.Accordion.Tab getVaadinTab() {
            com.vaadin.ui.Component composition = ComponentsHelper.getComposition(tabComponent);
            return AccordionImpl.this.component.getTab(composition);
        }

        public Component getComponent() {
            return tabComponent;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getCaption() {
            return getVaadinTab().getCaption();
        }

        @Override
        public void setCaption(String caption) {
            getVaadinTab().setCaption(caption);
        }

        @Override
        public boolean isEnabled() {
            return getVaadinTab().isEnabled();
        }

        @Override
        public void setEnabled(boolean enabled) {
            getVaadinTab().setEnabled(enabled);
        }

        @Override
        public boolean isVisible() {
            return getVaadinTab().isVisible();
        }

        @Override
        public void setVisible(boolean visible) {
            getVaadinTab().setVisible(visible);
        }

        @Override
        public void setStyleName(@Nullable String styleName) {
            getVaadinTab().setStyleName(styleName);
        }

        @Nullable
        @Override
        public String getStyleName() {
            return getVaadinTab().getStyleName();
        }

        @Nullable
        @Override
        public String getIcon() {
            return icon;
        }

        @Override
        public void setIcon(@Nullable String icon) {
            this.icon = icon;
            if (!StringUtils.isEmpty(icon)) {
                Resource iconResource = iconResolver
                        .getIconResource(this.icon);
                getVaadinTab().setIcon(iconResource);
            } else {
                getVaadinTab().setIcon(null);
            }
        }

        @Override
        public void setIconFromSet(@Nullable Icons.Icon icon) {
            String iconPath = icons
                    .get(icon);
            setIcon(iconPath);
        }
    }

    @Override
    public void removeTab(String name) {
        Tab tab = tabs.get(name);
        if (tab == null) {
            throw new IllegalStateException(String.format("Can't find tab '%s'", name));
        }
        tabs.remove(name);

        Component childComponent = tab.getComponent();
        com.vaadin.ui.Component vComponent = childComponent.unwrap(com.vaadin.ui.Component.class);
        this.component.removeComponent(vComponent);

        tabMapping.remove(vComponent);

        childComponent.setParent(null);
    }

    @Override
    public void removeAllTabs() {
        tabMapping.clear();
        component.removeAllComponents();

        List<Tab> currentTabs = new ArrayList<>(tabs.values());
        tabs.clear();

        for (Tab tab : currentTabs) {
            Component childComponent = tab.getComponent();

            childComponent.setParent(null);
        }
    }

    @Override
    public Accordion.Tab addTab(String name, Component childComponent) {
        if (childComponent.getParent() != null && childComponent.getParent() != this) {
            throw new IllegalStateException("Component already has parent");
        }

        Tab tab = new Tab(name, childComponent);

        this.tabs.put(name, tab);

        com.vaadin.ui.Component tabComponent = childComponent.unwrapComposition(com.vaadin.ui.Component.class);
        tabComponent.setSizeFull();

        tabMapping.put(tabComponent, new ComponentDescriptor(name, childComponent));
        com.vaadin.ui.Accordion.Tab tabControl = this.component.addTab(tabComponent);

        if (AppUI.getCurrent() != null && getDebugId() != null) {
            this.component.setTestId(tabControl,
                    AppUI.getCurrent().getTestIdManager().getTestId(getDebugId() + "." + name));
        }
        if (AppUI.getCurrent() != null && AppUI.getCurrent().isTestMode()) {
            this.component.setJTestId(tabControl, name);
        }

        if (frame != null) {
            if (childComponent instanceof BelongToFrame
                    && ((BelongToFrame) childComponent).getFrame() == null) {
                ((BelongToFrame) childComponent).setFrame(frame);
            } else {
                ((FrameImplementation) frame).registerComponent(childComponent);
            }
        }

        childComponent.setParent(this);

        return tab;
    }

    @Override
    public void setDebugId(@Nullable String id) {
        super.setDebugId(id);

        String debugId = getDebugId();
        AppUI ui = AppUI.getCurrent();
        if (debugId != null && ui != null) {
            TestIdManager testIdManager = ui.getTestIdManager();

            for (Map.Entry<com.vaadin.ui.Component, ComponentDescriptor> tabEntry : tabMapping.entrySet()) {
                com.vaadin.ui.Component tabComponent = tabEntry.getKey();
                com.vaadin.ui.Accordion.Tab tab = component.getTab(tabComponent);
                ComponentDescriptor componentDescriptor = tabEntry.getValue();
                String name = componentDescriptor.name;

                component.setTestId(tab, testIdManager.getTestId(debugId + "." + name));
            }
        }
    }

    @Override
    public Accordion.Tab addLazyTab(String name,
                                   Element descriptor,
                                   ComponentLoader loader) {
        CssLayout tabContent = createLazyTabLayout();
        tabContent.setStyleName("jmix-tabsheet-lazytab");
        tabContent.setSizeFull();

        Tab tab = new Tab(name, tabContent);
        tabs.put(name, tab);

        com.vaadin.ui.Component tabComponent = tabContent.unwrapComposition(com.vaadin.ui.Component.class);

        tabMapping.put(tabComponent, new ComponentDescriptor(name, tabContent));
        com.vaadin.ui.Accordion.Tab tabControl = this.component.addTab(tabComponent);
        getLazyTabs().add(tabComponent);

        this.component.addSelectedTabChangeListener(createLazyTabChangeListener(tabContent, descriptor, loader));
        context = loader.getContext();

        if (!postInitTaskAdded
                && context instanceof ComponentLoader.ComponentContext) {
            ((ComponentLoader.ComponentContext) context).addPostInitTask((c, w) -> initComponentTabChangeListener());
            postInitTaskAdded = true;
        }

        if (AppUI.getCurrent() != null && getDebugId() != null) {
            this.component.setTestId(tabControl,
                    AppUI.getCurrent().getTestIdManager().getTestId(getDebugId() + "." + name));
        }
        if (AppUI.getCurrent() != null && AppUI.getCurrent().isTestMode()) {
            this.component.setJTestId(tabControl, name);
        }

        if (context instanceof ComponentLoader.ComponentContext) {
            tabContent.setFrame(((ComponentLoader.ComponentContext) context).getFrame());
        } else {
            throw new IllegalStateException("'context' must implement " +
                    "io.jmix.ui.xml.layout.ComponentLoader.ComponentContext");
        }

        return tab;
    }

    protected CssLayout createLazyTabLayout() {
        return uiComponents.create(CssLayout.NAME);
    }

    @Override
    public void setFrame(@Nullable Frame frame) {
        super.setFrame(frame);

        if (frame != null) {
            for (ComponentDescriptor descriptor : tabMapping.values()) {
                Component childComponent = descriptor.getComponent();
                if (childComponent instanceof BelongToFrame
                        && ((BelongToFrame) childComponent).getFrame() == null) {
                    ((BelongToFrame) childComponent).setFrame(frame);
                }
            }
        }
    }

    @Nullable
    @Override
    public Tab getSelectedTab() {
        com.vaadin.ui.Component component = this.component.getSelectedTab();
        if (component == null) {
            return null;
        }

        String name = tabMapping.get(component).getName();
        return tabs.get(name);
    }

    @Override
    public void setSelectedTab(Accordion.Tab tab) {
        Component component = ((Tab) tab).getComponent();
        this.component.setSelectedTab(component.unwrap(com.vaadin.ui.Component.class));
    }

    @Override
    public void setSelectedTab(String name) {
        Tab tab = tabs.get(name);
        if (tab == null) {
            throw new IllegalStateException(String.format("Can't find tab '%s'", name));
        }

        this.component.setSelectedTab(tab.getComponent().unwrap(com.vaadin.ui.Component.class));
    }

    @Nullable
    @Override
    public Accordion.Tab getTab(String name) {
        return tabs.get(name);
    }

    @Override
    public Component getTabComponent(String name) {
        Tab tab = tabs.get(name);
        return tab.getComponent();
    }

    @Override
    public Collection<Accordion.Tab> getTabs() {
        //noinspection unchecked
        return (Collection) tabs.values();
    }

    @Override
    public boolean isTabCaptionsAsHtml() {
        return component.isTabCaptionsAsHtml();
    }

    @Override
    public void setTabCaptionsAsHtml(boolean tabCaptionsAsHtml) {
        component.setTabCaptionsAsHtml(tabCaptionsAsHtml);
    }

    protected void initComponentTabChangeListener() {
        // init component SelectedTabChangeListener only when needed, making sure it is
        // after all lazy tabs listeners
        if (selectedTabChangeListenerRegistration == null) {
            selectedTabChangeListenerRegistration = component.addSelectedTabChangeListener(this::onSelectedTabChangeListener);
        }
    }

    protected void fireTabChanged() {
        publish(SelectedTabChangeEvent.class, new SelectedTabChangeEvent(this, getSelectedTab()));
    }

    @Override
    public Subscription addSelectedTabChangeListener(Consumer<SelectedTabChangeEvent> listener) {
        initComponentTabChangeListener();
        getEventHub().subscribe(SelectedTabChangeEvent.class, listener);
        return () -> internalRemoveSelectedTabChangeListener(listener);
    }

    protected void onSelectedTabChangeListener(com.vaadin.ui.TabSheet.SelectedTabChangeEvent event) {
        if (context instanceof ComponentLoader.ComponentContext) {
            ((ComponentLoader.ComponentContext) context).executeInjectTasks();
            ((ComponentLoader.ComponentContext) context).executeInitTasks();
        }
        // Fire GUI listener
        fireTabChanged();
        // Execute outstanding post init tasks after GUI listener.
        // We suppose that context.executePostInitTasks() executes a task once and then remove it from task list.
        if (context instanceof ComponentLoader.ComponentContext) {
            ((ComponentLoader.ComponentContext) context).executePostInitTasks();
        }
    }

    protected void internalRemoveSelectedTabChangeListener(Consumer<SelectedTabChangeEvent> listener) {
        unsubscribe(SelectedTabChangeEvent.class, listener);

        if (!hasSubscriptions(SelectedTabChangeEvent.class)
                && selectedTabChangeListenerRegistration != null) {
            selectedTabChangeListenerRegistration.remove();
            selectedTabChangeListenerRegistration = null;
        }
    }

    @Override
    public void attached() {
        super.attached();

        getOwnComponentsStream().forEach(component -> {
            ((AttachNotifier) component).attached();
        });
    }

    @Override
    public void detached() {
        super.detached();

        getOwnComponentsStream().forEach(component -> {
            ((AttachNotifier) component).detached();
        });
    }

    protected LazyTabChangeListener createLazyTabChangeListener(ComponentContainer tabContent, Element descriptor, ComponentLoader loader) {
        return new LazyTabChangeListener(tabContent, descriptor, loader);
    }

    protected class LazyTabChangeListener implements com.vaadin.ui.Accordion.SelectedTabChangeListener {
        protected ComponentContainer tabContent;
        protected Element descriptor;
        protected ComponentLoader loader;

        public LazyTabChangeListener(ComponentContainer tabContent, Element descriptor, ComponentLoader loader) {
            this.tabContent = tabContent;
            this.descriptor = descriptor;
            this.loader = loader;
        }

        @Override
        public void selectedTabChange(com.vaadin.ui.Accordion.SelectedTabChangeEvent event) {
            com.vaadin.ui.Component selectedTab = AccordionImpl.this.component.getSelectedTab();
            com.vaadin.ui.Component tabComponent = tabContent.unwrap(com.vaadin.ui.Component.class);
            if (selectedTab == tabComponent && getLazyTabs().remove(tabComponent)) {
                loader.createComponent();

                Component lazyContent = loader.getResultComponent();

                tabContent.add(lazyContent);
                com.vaadin.ui.Component impl = ComponentsHelper.getComposition(lazyContent);
                impl.setSizeFull();

                lazyContent.setParent(AccordionImpl.this);

                loader.loadComponent();

                // do not show icon/caption in layout, it is used in Tab only
                AbstractComponent contentComponent = (AbstractComponent) lazyContent;

                contentComponent.setIcon(null);
                contentComponent.setCaption(null);
                contentComponent.setDescription(null);

                Window window = ComponentsHelper.getWindow(AccordionImpl.this);
                applySettings(window);
            }
        }

        protected void applySettings(@Nullable Window window) {
            if (window != null) {
                settingsFacetResolver.resolveLazyTabSelectEvent(
                        window, AccordionImpl.this, tabContent.getComponents());
            }
        }
    }

    protected static class ComponentDescriptor {
        protected Component component;

        protected String name;

        public ComponentDescriptor(String name, Component component) {
            this.name = name;
            this.component = component;
        }

        public Component getComponent() {
            return component;
        }

        public String getName() {
            return name;
        }
    }
}
