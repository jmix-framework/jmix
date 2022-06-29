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
package io.jmix.ui.menu;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.common.xmlparsing.Dom4jTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.jmix.ui.icon.Icons.ICON_NAME_REGEX;

/**
 * GenericUI class holding information about the main menu structure.
 */
@Component("ui_MenuConfig")
public class MenuConfig {

    private final Logger log = LoggerFactory.getLogger(MenuConfig.class);

    public static final String MENU_CONFIG_XML_PROP = "jmix.ui.menu-config";

    protected List<MenuItem> rootItems = new ArrayList<>();

    protected Resources resources;
    protected Messages messages;
    protected MessageTools messageTools;
    protected ThemeConstantsManager themeConstantsManager;
    protected Dom4jTools dom4JTools;
    protected Environment environment;
    protected UiProperties uiProperties;
    protected JmixModules modules;
    protected Icons icons;
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected FetchPlanRepository fetchPlanRepository;
    protected DataManager dataManager;

    protected volatile boolean initialized;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setFetchPlanRepository(FetchPlanRepository fetchPlanRepository) {
        this.fetchPlanRepository = fetchPlanRepository;
    }

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setThemeConstantsManager(ThemeConstantsManager themeConstantsManager) {
        this.themeConstantsManager = themeConstantsManager;
    }

    @Autowired
    public void setDom4JTools(Dom4jTools dom4JTools) {
        this.dom4JTools = dom4JTools;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setModules(JmixModules modules) {
        this.modules = modules;
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icons = icons;
    }

    public String getItemCaption(String id) {
        return messages.getMessage("menu-config." + id);
    }

    public String getItemCaption(MenuItem menuItem) {
        String caption = menuItem.getCaption();
        if (StringUtils.isNotEmpty(caption)) {
            String localizedCaption = loadResourceString(caption);
            if (StringUtils.isNotEmpty(localizedCaption)) {
                return localizedCaption;
            }
        }
        return getItemCaption(menuItem.getId());
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

        for (String location : locations) {
            Resource resource = resources.getResource(location);
            if (resource.exists()) {
                try (InputStream stream = resource.getInputStream()) {
                    Element rootElement = dom4JTools.readDocument(stream).getRootElement();
                    loadMenuItems(rootElement, null);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to read menu config", e);
                }
            } else {
                log.warn("Resource {} not found, ignore it", location);
            }
        }
    }

    /**
     * Make the config to reload screens on next request.
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
            MenuItem currentParentItem = parentItem;
            MenuItem nextToItem = null;
            boolean addItem = true;
            boolean before = true;
            String nextTo = element.attributeValue("insertBefore");
            if (StringUtils.isBlank(nextTo)) {
                before = false;
                nextTo = element.attributeValue("insertAfter");
            }
            if (!StringUtils.isBlank(nextTo)) {
                for (MenuItem rootItem : rootItems) {
                    nextToItem = findItem(nextTo, rootItem);
                    if (nextToItem != null) {
                        if (nextToItem.getParent() != null)
                            currentParentItem = nextToItem.getParent();
                        break;
                    }
                }
            }

            if ("menu".equals(element.getName())) {
                String id = element.attributeValue("id");

                if (StringUtils.isBlank(id)) {
                    log.warn("Invalid menu-config: 'id' attribute not defined");
                }

                MenuItem existingMenuItem = rootItems.stream()
                        .filter(item -> Objects.nonNull(findItem(id, item)))
                        .findFirst()
                        .orElse(null);

                menuItem = existingMenuItem != null ? existingMenuItem : new MenuItem(currentParentItem, id);
                addItem = existingMenuItem == null;

                menuItem.setMenu(true);
                menuItem.setDescriptor(element);
                loadIcon(element, menuItem);
                loadShortcut(menuItem, element);
                loadStylename(element, menuItem);
                loadExpanded(element, menuItem);
                loadCaption(element, menuItem);
                loadDescription(element, menuItem);
                loadMenuItems(element, menuItem);
            } else if ("item".equals(element.getName())) {
                menuItem = createMenuItem(element, currentParentItem);

                if (menuItem == null) {
                    continue;
                }
            } else if ("separator".equals(element.getName())) {
                String id = element.attributeValue("id");
                if (StringUtils.isBlank(id))
                    id = "-";
                menuItem = new MenuItem(currentParentItem, id);
                menuItem.setSeparator(true);
                if (!StringUtils.isBlank(id)) {
                    menuItem.setDescriptor(element);
                }
            } else {
                log.warn(String.format("Unknown tag '%s' in menu-config", element.getName()));
            }

            if (addItem) {
                if (currentParentItem != null) {
                    addItem(currentParentItem.getChildren(), menuItem, nextToItem, before);
                } else {
                    addItem(rootItems, menuItem, nextToItem, before);
                }
            }
        }
    }

    @Nullable
    protected MenuItem createMenuItem(Element element, @Nullable MenuItem currentParentItem) {
        String id = element.attributeValue("id");

        String idFromActions;

        String screen = element.attributeValue("screen");
        idFromActions = StringUtils.isNotEmpty(screen) ? screen : null;

        String runnableClass = element.attributeValue("class");
        checkDuplicateAction(idFromActions, runnableClass);
        idFromActions = StringUtils.isNotEmpty(runnableClass) ? runnableClass : idFromActions;

        String bean = element.attributeValue("bean");
        String beanMethod = element.attributeValue("beanMethod");

        if (StringUtils.isNotEmpty(bean) && StringUtils.isEmpty(beanMethod) ||
                StringUtils.isEmpty(bean) && StringUtils.isNotEmpty(beanMethod)) {
            throw new IllegalStateException("Both bean and beanMethod should be defined.");
        }

        checkDuplicateAction(idFromActions, bean, beanMethod);

        String fqn = bean + "#" + beanMethod;
        idFromActions = StringUtils.isNotEmpty(bean) && StringUtils.isNotEmpty(beanMethod) ? fqn : idFromActions;

        if (StringUtils.isEmpty(id) && StringUtils.isEmpty(idFromActions)) {
            throw new IllegalStateException("MenuItem should have at least one action");
        }

        if (StringUtils.isEmpty(id) && StringUtils.isNotEmpty(idFromActions)) {
            id = idFromActions;
        }

        if (StringUtils.isNotEmpty(id) && StringUtils.isEmpty(idFromActions)) {
            screen = id;
        }

        MenuItem menuItem = new MenuItem(currentParentItem, id);

        menuItem.setScreen(screen);
        menuItem.setRunnableClass(runnableClass);
        menuItem.setBean(bean);
        menuItem.setBeanMethod(beanMethod);

        menuItem.setDescriptor(element);
        loadIcon(element, menuItem);
        loadShortcut(menuItem, element);
        loadStylename(element, menuItem);
        loadCaption(element, menuItem);
        loadDescription(element, menuItem);

        menuItem.setProperties(loadMenuItemProperties(element));

        return menuItem;
    }

    protected void checkValueOrEntityProvided(Element property) {
        String value = property.attributeValue("value");
        String entityClass = property.attributeValue("entityClass");

        if (Strings.isNullOrEmpty(value) && Strings.isNullOrEmpty(entityClass)) {
            String name = property.attributeValue("name");
            throw new IllegalStateException(
                    String.format("Screen property '%s' has neither value nor entity load info", name));
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

    protected void loadExpanded(Element element, MenuItem menuItem) {
        String expanded = element.attributeValue("expanded");
        if (StringUtils.isNotEmpty(expanded)) {
            menuItem.setExpanded(Boolean.parseBoolean(expanded));
        }
    }

    protected void loadCaption(Element element, MenuItem menuItem) {
        String caption = element.attributeValue("caption");
        if (StringUtils.isNotEmpty(caption)) {
            menuItem.setCaption(caption);
        }
    }

    protected void loadDescription(Element element, MenuItem menuItem) {
        String description = element.attributeValue("description");
        if (StringUtils.isNotBlank(description)) {
            menuItem.setDescription(description);
        }
    }

    protected void loadStylename(Element element, MenuItem menuItem) {
        String stylename = element.attributeValue("stylename");
        if (StringUtils.isNotBlank(stylename)) {
            menuItem.setStylename(stylename);
        }
    }

    protected void loadIcon(Element element, MenuItem menuItem) {
        String icon = element.attributeValue("icon");
        if (StringUtils.isNotEmpty(icon)) {
            menuItem.setIcon(getIconPath(icon));
        }
    }

    @Nullable
    protected String getIconPath(@Nullable String icon) {
        if (icon == null || icon.isEmpty()) {
            return null;
        }

        String iconPath = null;

        if (ICON_NAME_REGEX.matcher(icon).matches()) {
            iconPath = icons.get(icon);
        }

        if (StringUtils.isEmpty(iconPath)) {
            String themeValue = loadThemeString(icon);
            iconPath = loadResourceString(themeValue);
        }

        return iconPath;
    }

    protected String loadResourceString(@Nullable String caption) {
        return messageTools.loadString(caption);
    }

    @Nullable
    protected String loadThemeString(@Nullable String value) {
        if (value != null && value.startsWith(ThemeConstants.PREFIX)) {
            value = themeConstantsManager.getConstants()
                    .get(value.substring(ThemeConstants.PREFIX.length()));
        }
        return value;
    }

    protected List<MenuItem.MenuItemProperty> loadMenuItemProperties(Element menuItem) {
        Element properties = menuItem.element("properties");
        if (properties == null) {
            return Collections.emptyList();
        }

        List<Element> propertyList = properties.elements("property");
        List<MenuItem.MenuItemProperty> itemProperties = new ArrayList<>(propertyList.size());

        for (Element property : propertyList) {
            String propertyName = property.attributeValue("name");
            if (Strings.isNullOrEmpty(propertyName)) {
                throw new IllegalStateException("Property cannot have empty name");
            }

            checkValueOrEntityProvided(property);

            MenuItem.MenuItemProperty itemProperty = new MenuItem.MenuItemProperty(propertyName);

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

    @Nullable
    protected Object loadMenuItemPropertyValue(Element property) {
        String value = property.attributeValue("value");
        return !Strings.isNullOrEmpty(value) ? getMenuItemPropertyTypedValue(value) : null;
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
            throw new IllegalStateException(String.format("Screen property '%s' does not have entity class", name));
        }

        return metadata.getClass(ReflectionHelper.getClass(entityClass));
    }

    protected Object loadItemPropertyEntityId(Element property, MetaClass metaClass) {
        String entityId = property.attributeValue("entityId");
        if (StringUtils.isEmpty(entityId)) {
            String name = property.attributeValue("name");
            throw new IllegalStateException(String.format("Screen entity property '%s' doesn't have entity id", name));
        }

        Object id = parseEntityId(metaClass, entityId);
        if (id == null) {
            throw new RuntimeException(String.format("Unable to parse id value `%s` for entity '%s'",
                    entityId, metaClass.getJavaClass()));
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
        MetaProperty pkProperty = metadataTools.getPrimaryKeyProperty(entityMetaClass);

        if (pkProperty == null) {
            return null;
        }

        Class<?> pkType = pkProperty.getJavaType();

        if (String.class.equals(pkType)) {
            return entityId;
        } else if (UUID.class.equals(pkType)) {
            return UUID.fromString(entityId);
        }

        Object id = null;

        try {
            if (Long.class.equals(pkType)) {
                id = Long.valueOf(entityId);
            } else if (Integer.class.equals(pkType)) {
                id = Integer.valueOf(entityId);
            }
        } catch (Exception e) {
            log.debug("Failed to parse entity id: '{}'", entityId, e);
        }

        return id;
    }

    protected void addItem(List<MenuItem> items, @Nullable MenuItem menuItem, @Nullable MenuItem beforeItem, boolean before) {
        if (beforeItem == null) {
            items.add(menuItem);
        } else {
            int i = items.indexOf(beforeItem);
            if (before)
                items.add(i, menuItem);
            else
                items.add(i + 1, menuItem);
        }
    }

    @Nullable
    public MenuItem findItem(String id, MenuItem item) {
        if (id.equals(item.getId()))
            return item;
        else if (!item.getChildren().isEmpty()) {
            for (MenuItem child : item.getChildren()) {
                MenuItem menuItem = findItem(id, child);
                if (menuItem != null)
                    return menuItem;
            }
        }
        return null;
    }

    protected void loadShortcut(MenuItem menuItem, Element element) {
        String shortcut = element.attributeValue("shortcut");
        if (shortcut == null || shortcut.isEmpty()) {
            return;
        }
        // If the shortcut string looks like a property, try to get it from the application properties
        if (shortcut.startsWith("${") && shortcut.endsWith("}")) {
            String property = environment.getProperty(shortcut.substring(2, shortcut.length() - 1));
            if (!StringUtils.isEmpty(property))
                shortcut = property;
            else
                return;
        }
        try {
            menuItem.setShortcut(KeyCombination.create(shortcut));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid menu shortcut value: '" + shortcut + "'");
        }
    }
}
