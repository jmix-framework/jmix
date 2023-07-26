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
package io.jmix.flowui.menu;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.common.xmlparsing.Dom4jTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.menu.MenuItem.MenuItemParameter;
import io.jmix.flowui.menu.MenuItem.MenuItemProperty;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Holds information about the main menu structure.
 */
@Component("flowui_MenuConfig")
public class MenuConfig {

    private final Logger log = LoggerFactory.getLogger(MenuConfig.class);

    public static final String MENU_CONFIG_XML_PROP = "jmix.ui.menu-config";

    protected List<MenuItem> rootItems = new ArrayList<>();

    protected Resources resources;
    protected Messages messages;
    protected MessageTools messageTools;
    protected Dom4jTools dom4JTools;
    protected Environment environment;
    protected UiProperties uiProperties;
    protected JmixModules modules;
    protected Metadata metadata;
    protected MetadataTools metadataTools;

    protected volatile boolean initialized;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    public MenuConfig(Resources resources, Messages messages, MessageTools messageTools, Dom4jTools dom4JTools,
                      Environment environment, UiProperties uiProperties, JmixModules modules,
                      Metadata metadata, MetadataTools metadataTools) {
        this.resources = resources;
        this.messages = messages;
        this.messageTools = messageTools;
        this.dom4JTools = dom4JTools;
        this.environment = environment;
        this.uiProperties = uiProperties;
        this.modules = modules;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
    }

    public String getItemTitle(String id) {
        return messages.getMessage("menu-config." + id);
    }

    public String getItemTitle(MenuItem menuItem) {
        String title = menuItem.getTitle();
        if (StringUtils.isNotEmpty(title)) {
            String localizedTitle = loadResourceString(title);
            if (StringUtils.isNotEmpty(localizedTitle)) {
                return localizedTitle;
            }
        }
        return getItemTitle(menuItem.getId());
    }

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        rootItems.clear();

        List<String> locations = uiProperties.isCompositeMenu() ?
                modules.getPropertyValues(MENU_CONFIG_XML_PROP) :
                Collections.singletonList(environment.getProperty(MENU_CONFIG_XML_PROP));

        // put the list in default order - from the app down to the add-ons
        Collections.reverse(locations);

        List<Element> rootElements = new ArrayList<>();
        for (String location : locations) {
            Resource resource = resources.getResource(location);
            if (resource.exists()) {
                try (InputStream stream = resource.getInputStream()) {
                    rootElements.add(dom4JTools.readDocument(stream).getRootElement());
                } catch (IOException e) {
                    throw new RuntimeException("Unable to read menu config", e);
                }
            } else {
                log.warn("Resource {} not found, ignore it", location);
            }
        }

        // sort according to the `order` attributes
        rootElements.sort((e1, e2) -> {
            String orderStr1 = e1.attributeValue("order");
            String orderStr2 = e2.attributeValue("order");
            int order1 = orderStr1 == null ? 0 : Integer.parseInt(orderStr1);
            int order2 = orderStr2 == null ? 0 : Integer.parseInt(orderStr2);
            return order1 - order2;
        });

        for (Element rootElement : rootElements) {
            loadMenuItems(rootElement, null);
        }
    }

    /**
     * Make the config to reload view on next request.
     */
    public void reset() {
        initialized = false;
    }

    /**
     * Main menu root items
     */
    public List<MenuItem> getRootItems() {
        lock.readLock().lock();
        try {
            checkInitialized();
            return Collections.unmodifiableList(rootItems);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void loadMenuItems(Element parentElement, @Nullable MenuItem parentItem) {
        for (Element element : parentElement.elements()) {
            MenuItem menuItem = null;

            if ("menu".equals(element.getName())) {
                String id = element.attributeValue("id");

                if (StringUtils.isBlank(id)) {
                    log.warn("Invalid menu-config: 'id' attribute not defined");
                }

                menuItem = new MenuItem(parentItem, id);

                menuItem.setMenu(true);
                menuItem.setDescriptor(element);
                loadIcon(element, menuItem);
                loadShortcutCombination(menuItem, element);
                loadClassNames(element, menuItem);
                loadOpened(element, menuItem);
                loadTitle(element, menuItem);
                loadDescription(element, menuItem);
                loadMenuItems(element, menuItem);
            } else if ("item".equals(element.getName())) {
                menuItem = createMenuItem(element, parentItem);

                if (menuItem == null) {
                    continue;
                }
            } else if ("separator".equals(element.getName())) {
                String id = element.attributeValue("id");
                if (StringUtils.isBlank(id)) {
                    id = "-";
                }

                menuItem = new MenuItem(parentItem, id);
                menuItem.setSeparator(true);

                if (StringUtils.isNotBlank(id)) {
                    menuItem.setDescriptor(element);
                }
            } else {
                log.warn(String.format("Unknown tag '%s' in menu-config", element.getName()));
            }

            if (parentItem != null) {
                parentItem.getChildren().add(menuItem);
            } else {
                rootItems.add(menuItem);
            }
        }
    }

    @Nullable
    protected MenuItem createMenuItem(Element element, @Nullable MenuItem currentParentItem) {
        String id = element.attributeValue("id");

        String idFromActions;

        String view = element.attributeValue("view");
        idFromActions = StringUtils.isNotEmpty(view) ? view : null;

        String bean = element.attributeValue("bean");
        String beanMethod = element.attributeValue("beanMethod");

        if (StringUtils.isNotEmpty(bean) && StringUtils.isEmpty(beanMethod) ||
                StringUtils.isEmpty(bean) && StringUtils.isNotEmpty(beanMethod)) {
            throw new IllegalStateException("Both bean and beanMethod should be defined.");
        }

        checkDuplicateAction(idFromActions, bean, beanMethod);

        String fqn = bean + "#" + beanMethod;
        idFromActions = StringUtils.isNotEmpty(bean) && StringUtils.isNotEmpty(beanMethod)
                ? fqn
                : idFromActions;

        if (StringUtils.isEmpty(id) && StringUtils.isEmpty(idFromActions)) {
            throw new IllegalStateException("MenuItem should have at least one action");
        }

        if (StringUtils.isEmpty(id) && StringUtils.isNotEmpty(idFromActions)) {
            id = idFromActions;
        }

        if (StringUtils.isNotEmpty(id) && StringUtils.isEmpty(idFromActions)) {
            view = id;
        }

        MenuItem menuItem = new MenuItem(currentParentItem, id);

        menuItem.setView(view);

        menuItem.setBean(bean);
        menuItem.setBeanMethod(beanMethod);

        menuItem.setDescriptor(element);
        loadIcon(element, menuItem);
        loadShortcutCombination(menuItem, element);
        loadClassNames(element, menuItem);
        loadTitle(element, menuItem);
        loadDescription(element, menuItem);

        menuItem.setProperties(loadMenuItemProperties(element));
        menuItem.setUrlQueryParameters(loadMenuItemParameters(element, "urlQueryParameters"));
        menuItem.setRouteParameters(loadMenuItemParameters(element, "routeParameters"));

        return menuItem;
    }

    protected void checkValueOrEntityProvided(Element property) {
        String value = property.attributeValue("value");
        String entityClass = property.attributeValue("entityClass");

        if (Strings.isNullOrEmpty(value) && Strings.isNullOrEmpty(entityClass)) {
            String name = property.attributeValue("name");
            throw new IllegalStateException(
                    String.format("View property '%s' has neither value nor entity load info", name)
            );
        }
    }

    protected void checkDuplicateAction(@Nullable String menuItemId, String... actionDefinition) {
        boolean actionDefined = true;

        for (String s : actionDefinition) {
            actionDefined &= StringUtils.isNotEmpty(s);
        }
        if (StringUtils.isNotEmpty(menuItemId) && actionDefined) {
            throw new IllegalStateException("MenuItem can't have more than one action.");
        }
    }

    protected void loadOpened(Element element, MenuItem menuItem) {
        String opened = element.attributeValue("opened");

        if (StringUtils.isNotEmpty(opened)) {
            menuItem.setOpened(Boolean.parseBoolean(opened));
        }
    }

    protected void loadTitle(Element element, MenuItem menuItem) {
        String title = element.attributeValue("title");

        if (StringUtils.isNotEmpty(title)) {
            menuItem.setTitle(title);
        }
    }

    protected void loadDescription(Element element, MenuItem menuItem) {
        String description = element.attributeValue("description");

        if (StringUtils.isNotBlank(description)) {
            menuItem.setDescription(description);
        }
    }

    protected void loadClassNames(Element element, MenuItem menuItem) {
        String className = element.attributeValue("classNames");

        if (StringUtils.isNotBlank(className)) {
            menuItem.setClassNames(className);
        }
    }

    protected void loadIcon(Element element, MenuItem menuItem) {
        String icon = element.attributeValue("icon");

        if (StringUtils.isNotEmpty(icon)) {
            menuItem.setIcon(icon);
        }
    }

    protected String loadResourceString(@Nullable String ref) {
        return messageTools.loadString(ref);
    }

    protected List<MenuItemProperty> loadMenuItemProperties(Element menuItem) {
        Element properties = menuItem.element("properties");

        if (properties == null) {
            return Collections.emptyList();
        }

        List<Element> propertyList = properties.elements("property");
        List<MenuItemProperty> itemProperties = new ArrayList<>(propertyList.size());

        for (Element property : propertyList) {
            String propertyName = property.attributeValue("name");

            if (Strings.isNullOrEmpty(propertyName)) {
                throw new IllegalStateException("Property cannot have empty name");
            }

            checkValueOrEntityProvided(property);

            MenuItemProperty itemProperty = new MenuItemProperty(propertyName);

            Object value = loadMenuItemPropertyValue(property);

            if (value != null) {
                itemProperty.setValue(value);
            } else {
                itemProperty.setEntityClass(loadItemPropertyEntityClass(property));
                itemProperty.setEntityId(loadItemPropertyEntityId(property, itemProperty.getEntityClass()));
                itemProperty.setFetchPlanName(loadEntityFetchPlan(property));
            }

            itemProperties.add(itemProperty);
        }

        return itemProperties;
    }

    protected List<MenuItemParameter> loadMenuItemParameters(Element menuItem, String parametersElementName) {
        Element parameters = menuItem.element(parametersElementName);

        if (parameters == null) {
            return Collections.emptyList();
        }

        List<Element> parameterList = parameters.elements("parameter");
        List<MenuItemParameter> itemParameters = new ArrayList<>(parameterList.size());

        for (Element parameter : parameterList) {
            String parameterName = parameter.attributeValue("name");

            if (Strings.isNullOrEmpty(parameterName)) {
                throw new IllegalStateException("Parameter cannot have empty name");
            }

            String parameterValue = parameter.attributeValue("value");

            if (Strings.isNullOrEmpty(parameterValue)) {
                String message = String.format("Parameter with name '%s' cannot have empty value", parameterName);
                throw new IllegalStateException(message);
            }

            MenuItemParameter itemParameter = new MenuItemParameter(parameterName, parameterValue);

            itemParameters.add(itemParameter);
        }

        return itemParameters;
    }

    @Nullable
    protected Object loadMenuItemPropertyValue(Element property) {
        String value = property.attributeValue("value");
        return !Strings.isNullOrEmpty(value)
                ? getMenuItemPropertyTypedValue(value)
                : null;
    }

    protected Object getMenuItemPropertyTypedValue(String value) {
        if (Boolean.TRUE.toString().equalsIgnoreCase(value)
                || Boolean.FALSE.toString().equalsIgnoreCase(value)) {
            return Boolean.valueOf(value);
        }

        return value;
    }

    protected MetaClass loadItemPropertyEntityClass(Element property) {
        String entityClass = property.attributeValue("entityClass");

        if (StringUtils.isEmpty(entityClass)) {
            String name = property.attributeValue("name");
            throw new IllegalStateException(
                    String.format("View property '%s' does not have entity class", name)
            );
        }

        return metadata.getClass(ReflectionHelper.getClass(entityClass));
    }

    protected Object loadItemPropertyEntityId(Element property, MetaClass metaClass) {
        String entityId = property.attributeValue("entityId");

        if (StringUtils.isEmpty(entityId)) {
            String name = property.attributeValue("name");
            throw new IllegalStateException(
                    String.format("View entity property '%s' doesn't have entity id", name)
            );
        }

        Object id = parseEntityId(metaClass, entityId);

        if (id == null) {
            throw new RuntimeException(
                    String.format("Unable to parse id value `%s` for entity '%s'",
                            entityId,
                            metaClass.getJavaClass()
                    )
            );
        }

        return id;
    }

    protected String loadEntityFetchPlan(Element propertyElement) {
        String entityFetchPlan = propertyElement.attributeValue("entityFetchPlan");
        return StringUtils.isNotEmpty(entityFetchPlan)
                ? entityFetchPlan
                : propertyElement.attributeValue("entityView"); // for backward compatibility
    }

    @Nullable
    protected Object parseEntityId(MetaClass entityMetaClass, String entityId) {
        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(entityMetaClass);

        if (primaryKeyProperty == null) {
            return null;
        }

        Class<?> primaryKeyType = primaryKeyProperty.getJavaType();

        if (String.class.equals(primaryKeyType)) {
            return entityId;
        } else if (UUID.class.equals(primaryKeyType)) {
            return UUID.fromString(entityId);
        }

        Object id = null;

        try {
            if (Long.class.equals(primaryKeyType)) {
                id = Long.valueOf(entityId);
            } else if (Integer.class.equals(primaryKeyType)) {
                id = Integer.valueOf(entityId);
            }
        } catch (Exception e) {
            log.debug("Failed to parse entity id: '{}'", entityId, e);
        }

        return id;
    }

    protected void loadShortcutCombination(MenuItem menuItem, Element element) {
        String shortcutCombination = element.attributeValue("shortcutCombination");

        if (shortcutCombination == null || shortcutCombination.isEmpty()) {
            return;
        }

        // If the shortcutCombination string looks like a property, try to get it from the application properties
        if (shortcutCombination.startsWith("${") && shortcutCombination.endsWith("}")) {
            String property = environment.getProperty(
                    shortcutCombination.substring(2, shortcutCombination.length() - 1)
            );
            if (StringUtils.isNotEmpty(property))
                shortcutCombination = property;
            else
                return;
        }

        try {
            menuItem.setShortcutCombination(KeyCombination.create(shortcutCombination));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid menu shortcutCombination value: '" + shortcutCombination + "'");
        }
    }

    @Nullable
    public MenuItem findItem(String id, MenuItem item) {
        if (id.equals(item.getId())) {
            return item;
        } else if (!item.getChildren().isEmpty()) {
            for (MenuItem child : item.getChildren()) {
                MenuItem menuItem = findItem(id, child);

                if (menuItem != null) {
                    return menuItem;
                }
            }
        }
        return null;
    }
}
