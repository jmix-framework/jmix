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

package io.jmix.securityflowui.view.resourcepolicy;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.HasListDataView;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Component("sec_ResourcePolicyViewUtils")
public class ResourcePolicyViewUtils {

    private ViewRegistry viewRegistry;
    private ViewSupport viewSupport;
    private MenuConfig menuConfig;
    private Metadata metadata;
    private MetadataTools metadataTools;
    private Messages messages;
    private MessageTools messageTools;

    public ResourcePolicyViewUtils(ViewRegistry viewRegistry,
                                   ViewSupport viewSupport,
                                   MenuConfig menuConfig,
                                   Metadata metadata,
                                   MetadataTools metadataTools,
                                   Messages messages,
                                   MessageTools messageTools) {
        this.viewRegistry = viewRegistry;
        this.viewSupport = viewSupport;
        this.menuConfig = menuConfig;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.messages = messages;
        this.messageTools = messageTools;
    }

    public Map<String, String> getEntityOptionsMap() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("*", messages.getMessage(ResourcePolicyViewUtils.class, "allEntities"));
        result.putAll(metadata.getClasses().stream()
                .collect(Collectors.toMap(
                        MetaClass::getName,
                        this::getEntityCaption,
                        this::throwDuplicateException,
                        TreeMap::new)));
        return result;
    }

    public Map<String, String> getEntityAttributeOptionsMap(@Nullable String entityName) {
        if (Strings.isNullOrEmpty(entityName)) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<>();
        result.put("*", messages.getMessage(ResourcePolicyViewUtils.class, "allAttributes"));

        if ("*".equals(entityName)) {
            return result;
        }

        MetaClass metaClass = metadata.getClass(entityName);
        result.putAll(
                Streams.concat(metaClass.getProperties().stream(),
                                metadataTools.getAdditionalProperties(metaClass).stream())
                        .collect(Collectors.toMap(
                                MetaProperty::getName,
                                this::getEntityAttributeCaption,
                                this::throwDuplicateException,
                                TreeMap::new)));
        return result;
    }

    public Map<String, String> getMenuItemOptionsMap() {
        Map<String, String> collectedMenus = new TreeMap<>();
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            walkMenuItem(rootItem, collectedMenus);
        }
        collectedMenus.put("*", messages.getMessage(ResourcePolicyViewUtils.class, "allMenus"));
        return collectedMenus;
    }

    public Map<String, String> getViewsOptionsMap() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("*", messages.getMessage(ResourcePolicyViewUtils.class, "allViews"));
        TreeMap<String, String> map = viewRegistry.getViewInfos().stream()
                .collect(Collectors.toMap(
                        ViewInfo::getId,
                        this::getDetailedViewTitle,
                        this::throwDuplicateException,
                        TreeMap::new));
        result.putAll(map);
        return result;
    }

    @Nullable
    public MenuItem findMenuItemById(String menuItemId) {
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            MenuItem menuItem = menuConfig.findItem(menuItemId, rootItem);
            if (menuItem != null) {
                return menuItem;
            }
        }

        return null;
    }

    @Nullable
    public MenuItem findMenuItemByView(String viewId) {
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            MenuItem menuItem = findMenuItemByView(rootItem, viewId);
            if (menuItem != null) {
                return menuItem;
            }
        }

        return null;
    }

    protected String getDetailedViewTitle(ViewInfo viewInfo) {
        return getViewTitle(viewInfo, true);
    }

    public String getViewTitle(String viewId) {
        return viewRegistry.findViewInfo(viewId)
                .map(viewInfo ->
                        getViewTitle(viewInfo, false))
                .orElse(viewId);
    }

    protected String getViewTitle(ViewInfo viewInfo, boolean detailed) {
        String viewTitle = viewSupport.getLocalizedTitle(viewInfo);
        if (Strings.isNullOrEmpty(viewTitle)) {
            return viewInfo.getId();
        } else {
            if (!Objects.equals(viewTitle, viewInfo.getId()) && detailed) {
                return String.format("%s (%s)", viewTitle, viewInfo.getId());
            } else {
                return viewTitle;
            }
        }
    }

    public String getMenuTitle(MenuItem menuItem) {
        StringBuilder title = new StringBuilder(menuConfig.getItemTitle(menuItem));
        MenuItem parent = menuItem.getParent();
        while (parent != null) {
            title.insert(0, menuConfig.getItemTitle(parent) + " > ");
            parent = parent.getParent();
        }

        return String.format("%s (%s)", title, menuItem.getId());
    }

    protected String getEntityCaption(MetaClass metaClass) {
        return String.format("%s (%s)", messageTools.getEntityCaption(metaClass), metaClass.getName());
    }

    protected String getEntityAttributeCaption(MetaProperty metaProperty) {
        return String.format("%s (%s)", messageTools.getPropertyCaption(metaProperty), metaProperty.getName());
    }

    protected void walkMenuItem(MenuItem menuItem, Map<String, String> collectedMenus) {
        collectedMenus.put(menuItem.getId(), getMenuTitle(menuItem));

        if (!menuItem.getChildren().isEmpty()) {
            menuItem.getChildren().forEach(childMenuItem ->
                    walkMenuItem(childMenuItem, collectedMenus));
        }
    }

    @Nullable
    protected MenuItem findMenuItemByView(MenuItem rootItem, String screenId) {
        if (Objects.equals(rootItem.getView(), screenId)) {
            return rootItem;
        }

        for (MenuItem menuItem : rootItem.getChildren()) {
            MenuItem result = findMenuItemByView(menuItem, screenId);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    protected String throwDuplicateException(String v1, String v2) {
        throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
    }

    public <T extends Enum<T> & EnumClass<String>> void setEnumItemsAsString(ComboBox<String> comboBox, Class<T> enumClass) {
        setEnumItemsAsStringInternal(comboBox, enumClass);
        comboBox.setItemLabelGenerator(createItemLabelGenerator(enumClass));
    }

    public <T extends Enum<T> & EnumClass<String>> void setEnumItemsAsString(Select<String> select, Class<T> enumClass) {
        setEnumItemsAsStringInternal(select, enumClass);
        select.setItemLabelGenerator(createItemLabelGenerator(enumClass));
    }

    protected <T extends Enum<T> & EnumClass<String>> void setEnumItemsAsStringInternal(HasListDataView<String, ?> component,
                                                                                        Class<T> enumClass) {
        List<String> actions = Arrays.stream(enumClass.getEnumConstants())
                .map(T::getId)
                .collect(Collectors.toList());
        component.setItems(actions);
    }

    protected <T extends Enum<T> & EnumClass<String>> ItemLabelGenerator<String> createItemLabelGenerator(Class<T> enumClass) {
        return item -> metadataTools.format(Enum.valueOf(enumClass, item.toUpperCase()));
    }
}