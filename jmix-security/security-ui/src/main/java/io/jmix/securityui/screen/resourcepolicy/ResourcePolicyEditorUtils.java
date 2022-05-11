/*
 * Copyright 2020 Haulmont.
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

package io.jmix.securityui.screen.resourcepolicy;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.menu.MenuConfig;
import io.jmix.ui.menu.MenuItem;
import io.jmix.ui.sys.ScreensHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component("sec_ResourcePolicyEditorUtils")
public class ResourcePolicyEditorUtils {

    @Autowired
    private Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    @Autowired
    private MessageTools messageTools;

    @Autowired
    private Messages messages;

    @Autowired
    private MenuConfig menuConfig;

    @Autowired
    private WindowConfig windowConfig;

    @Autowired
    private ScreensHelper screensHelper;

    public Map<String, String> getEntityOptionsMap() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put(messages.getMessage(ResourcePolicyEditorUtils.class, "allEntities"), "*");
        result.putAll(metadata.getClasses().stream()
                .collect(Collectors.toMap(
                        this::getEntityCaption,
                        MetaClass::getName,
                        (v1, v2) -> {
                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new)));
        return result;
    }

    public Map<String, String> getEntityAttributeOptionsMap(String entityName) {
        MetaClass metaClass = metadata.getClass(entityName);
        Map<String, String> result = new LinkedHashMap<>();
        result.put(messages.getMessage(ResourcePolicyEditorUtils.class, "allAttributes"), "*");

        result.putAll(
                Streams.concat(metaClass.getProperties().stream(),
                        metadataTools.getAdditionalProperties(metaClass).stream())
                        .collect(Collectors.toMap(
                                this::getEntityAttributeCaption,
                                MetaProperty::getName,
                                (v1, v2) -> {
                                    throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                                },
                                TreeMap::new)));
        return result;
    }

    public Map<String, String> getMenuItemOptionsMap() {
        Map<String, String> collectedMenus = new TreeMap<>();
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            walkMenuItem(rootItem, collectedMenus);
        }
        collectedMenus.put(messages.getMessage(ResourcePolicyEditorUtils.class, "allMenus"), "*");
        return collectedMenus;
    }

    public Map<String, String> getScreenOptionsMap() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put(messages.getMessage(ResourcePolicyEditorUtils.class, "allScreens"), "*");
        TreeMap<String, String> map = windowConfig.getWindows().stream()
                .filter(windowInfo -> windowInfo.getType() == WindowInfo.Type.SCREEN)
                .collect(Collectors.toMap(
                        this::getDetailedScreenCaption,
                        WindowInfo::getId,
                        (v1, v2) -> {
                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
        result.putAll(map);
        return result;
    }

    public MenuItem findMenuItemById(String menuItemId) {
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            MenuItem menuItem = menuConfig.findItem(menuItemId, rootItem);
            if (menuItem != null) {
                return menuItem;
            }
        }
        return null;
    }

    public MenuItem findMenuItemByScreen(String screenId) {
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            MenuItem menuItem = findMenuItemByScreen(rootItem, screenId);
            if (menuItem != null) {
                return menuItem;
            }
        }
        return null;
    }

    public String getScreenCaption(String screenId) {
        WindowInfo windowInfo = windowConfig.findWindowInfo(screenId);
        return windowInfo == null ? screenId : getScreenCaption(windowInfo, false);
    }

    protected String getDetailedScreenCaption(WindowInfo windowInfo) {
        return getScreenCaption(windowInfo, true);
    }

    protected String getScreenCaption(WindowInfo windowInfo, boolean detailed) {
        try {
            String screenCaption = screensHelper.getScreenCaption(windowInfo);
            if (Strings.isNullOrEmpty(screenCaption)) {
                return windowInfo.getId();
            } else {
                if (!Objects.equals(screenCaption, windowInfo.getId()) && detailed) {
                    return String.format("%s (%s)", screenCaption, windowInfo.getId());
                } else {
                    return screenCaption;
                }
            }
        } catch (FileNotFoundException e) {
            return windowInfo.getId();
        }
    }

    public String getMenuCaption(MenuItem menuItem) {
        StringBuilder caption = new StringBuilder(menuConfig.getItemCaption(menuItem));
        MenuItem parent = menuItem.getParent();
        while (parent != null) {
            caption.insert(0, menuConfig.getItemCaption(parent) + " > ");
            parent = parent.getParent();
        }
        return String.format("%s (%s)", caption.toString(), menuItem.getId());
    }

    private String getEntityCaption(MetaClass metaClass) {
        return String.format("%s (%s)", messageTools.getEntityCaption(metaClass), metaClass.getName());
    }

    private String getEntityAttributeCaption(MetaProperty metaProperty) {
        return String.format("%s (%s)", messageTools.getPropertyCaption(metaProperty), metaProperty.getName());
    }

    private void walkMenuItem(MenuItem menuItem, Map<String, String> collectedMenus) {
        if (!menuItem.isSeparator()) {
            collectedMenus.put(getMenuCaption(menuItem), menuItem.getId());
        }
        if (menuItem.getChildren() != null) {
            menuItem.getChildren().forEach(childMenuItem -> walkMenuItem(childMenuItem, collectedMenus));
        }
    }

    private MenuItem findMenuItemByScreen(MenuItem rootItem, String screenId) {
        if (Objects.equals(rootItem.getScreen(), screenId)) {
            return rootItem;
        }
        for (MenuItem menuItem : rootItem.getChildren()) {
            MenuItem result = findMenuItemByScreen(menuItem, screenId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}