/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.meta.component.preview.loader;

import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.xml.layout.support.ComponentLoaderUtils;
import io.jmix.flowui.kit.xml.layout.support.LoaderUtils;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Shared helpers for preview loaders that build menu items from XML.
 */
public final class PreviewActionSupport {

    private static final String MESSAGE_REF_PREFIX = "msg://";
    private static final int PLACEHOLDER_ITEM_COUNT = 5;

    private PreviewActionSupport() {
    }

    /**
     * Resolves a {@code msg://} message reference through the environment, falling back to
     * the raw value when the reference isn't a message key or the environment can't resolve it
     * (e.g. {@link StudioPreviewEnvironment#NOOP}). Null-safe: a {@code null} value passes through.
     */
    @Nullable
    public static String resolveText(StudioPreviewEnvironment environment, @Nullable String value) {
        if (value == null || !value.startsWith(MESSAGE_REF_PREFIX)) {
            return value;
        }
        String resolved = environment.resolveMessage(value);
        return resolved != null ? resolved : value;
    }

    /**
     * Builds a {@link BaseAction} from a declarative {@code <action>} element: {@code id} attribute
     * (falling back to {@code fallbackId}), {@code text} (resolved via {@link #resolveText}),
     * {@code icon} (via {@link ComponentLoaderUtils#loadIconSetIcon(Element)}), and
     * {@code enabled}.
     */
    public static BaseAction<?> buildAction(Element actionElement, String fallbackId, StudioPreviewEnvironment environment) {
        String actionId = LoaderUtils.loadString(actionElement, "id").orElse(fallbackId);
        BaseAction<?> action = new BaseAction<>(actionId);
        LoaderUtils.loadString(actionElement, "text")
                .ifPresent(text -> action.withText(resolveText(environment, text)));
        ComponentLoaderUtils.loadIconSetIcon(actionElement).ifPresent(action::setIcon);
        LoaderUtils.loadBoolean(actionElement, "enabled", action::setEnabled);
        return action;
    }

    /**
     * Recursively searches {@code parent}'s descendants for an {@code <action id="...">} element
     * matching {@code actionId} (covers e.g. an {@code <actions>} block declared in the view).
     *
     * @return the matching element, or {@code null} if none is found
     */
    @Nullable
    public static Element findDescendantAction(Element parent, String actionId) {
        for (Element child : parent.elements()) {
            if (StudioXmlElements.ACTION.equals(child.getName()) && actionId.equals(child.attributeValue("id"))) {
                return child;
            }
            Element found = findDescendantAction(child, actionId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public static void loadActionItem(Element itemElement, Element viewElement, StudioPreviewEnvironment environment,
                                      BiConsumer<String, BaseAction<?>> actionItemAdder,
                                      BiConsumer<String, String> textItemAdder) {
        String id = LoaderUtils.loadString(itemElement, "id").orElse(null);
        if (id == null) {
            // Runtime throws without an id, preview skips silently.
            return;
        }

        Element actionElement = itemElement.element(StudioXmlElements.ACTION);
        if (actionElement != null) {
            actionItemAdder.accept(id, buildAction(actionElement, id, environment));
            return;
        }

        String ref = LoaderUtils.loadString(itemElement, "ref").orElse(null);
        if (ref == null) {
            // Neither an inline action nor a ref: runtime throws, preview skips silently.
            return;
        }

        Element refActionElement = findDescendantAction(viewElement, ref);
        if (refActionElement != null) {
            actionItemAdder.accept(id, buildAction(refActionElement, ref, environment));
        } else {
            textItemAdder.accept(id, id);
        }
    }

    /** Default text + icon per standard declarative action type, mirroring the runtime actions. */
    private record ActionDefaults(@Nullable String text, @Nullable JmixFontIcon icon) {
    }

    private static final Map<String, ActionDefaults> ACTION_TYPE_DEFAULTS = Map.ofEntries(
            Map.entry("list_create", new ActionDefaults("Create", JmixFontIcon.CREATE_ACTION)),
            Map.entry("list_edit", new ActionDefaults("Edit", JmixFontIcon.EDIT_ACTION)),
            Map.entry("list_remove", new ActionDefaults("Remove", JmixFontIcon.REMOVE_ACTION)),
            Map.entry("list_refresh", new ActionDefaults("Refresh", JmixFontIcon.REFRESH_ACTION)),
            Map.entry("list_read", new ActionDefaults("Read", JmixFontIcon.READ_ACTION)),
            Map.entry("list_add", new ActionDefaults("Add", JmixFontIcon.ADD_ACTION)),
            Map.entry("list_exclude", new ActionDefaults("Exclude", JmixFontIcon.EXCLUDE_ACTION)),
            Map.entry("lookup_select", new ActionDefaults("Select", JmixFontIcon.LOOKUP_SELECT_ACTION)),
            Map.entry("lookup_discard", new ActionDefaults("Cancel", JmixFontIcon.LOOKUP_DISCARD_ACTION)),
            Map.entry("detail_saveClose", new ActionDefaults("OK", JmixFontIcon.DETAIL_SAVE_CLOSE_ACTION)),
            Map.entry("detail_save", new ActionDefaults("Save", JmixFontIcon.DETAIL_SAVE_ACTION)),
            Map.entry("detail_close", new ActionDefaults("Close", JmixFontIcon.DETAIL_CLOSE_ACTION)),
            Map.entry("detail_enableEditing", new ActionDefaults("Enable editing",
                    JmixFontIcon.DETAIL_ENABLE_EDITING_ACTION)),
            Map.entry("detail_discard", new ActionDefaults("Discard", JmixFontIcon.DETAIL_DISCARD_ACTION)),
            Map.entry("entity_lookup", new ActionDefaults(null, JmixFontIcon.ENTITY_LOOKUP_ACTION)),
            Map.entry("entity_clear", new ActionDefaults(null, JmixFontIcon.ENTITY_CLEAR_ACTION)),
            Map.entry("entity_open", new ActionDefaults(null, JmixFontIcon.ENTITY_OPEN_ACTION)),
            Map.entry("value_clear", new ActionDefaults(null, JmixFontIcon.VALUE_CLEAR_ACTION)),
            // add-on action types: icon only, the text falls back to the humanized action id
            Map.entry("sec_showRoleAssignments",
                    new ActionDefaults(null, JmixFontIcon.SHOW_ROLE_ASSIGNMENTS_ACTION)),
            Map.entry("grdexp_excelExport", new ActionDefaults(null, JmixFontIcon.EXCEL_EXPORT_ACTION)),
            Map.entry("grdexp_jsonExport", new ActionDefaults(null, JmixFontIcon.JSON_EXPORT_ACTION)));

    /** Picker-style types whose runtime buttons are icon-only; everything else gets a text. */
    private static final Set<String> ICON_ONLY_TYPES =
            Set.of("entity_lookup", "entity_clear", "entity_open", "value_clear");

    /** Types of the implicit view actions that exist at runtime without an {@code <action>} element. */
    private static final Map<String, String> IMPLICIT_ACTION_TYPES = Map.of(
            "selectAction", "lookup_select",
            "discardAction", "lookup_discard",
            "saveAndCloseAction", "detail_saveClose",
            "saveAction", "detail_save",
            "closeAction", "detail_close",
            "enableEditingAction", "detail_enableEditing");

    /**
     * Applies an {@code action="..."} reference to a button the way the runtime binder does:
     * the button's own text/icon win, then the action element's attributes, then the standard
     * type defaults. Unresolvable references (e.g. actions created in the controller) fall back
     * to a humanized action id so the button is not blank.
     */
    public static void applyButtonAction(JmixButton button, String actionRef, Element viewElement,
                                         StudioPreviewEnvironment environment) {
        Element actionElement = findActionByRef(viewElement, actionRef);
        String type = actionElement != null
                ? actionElement.attributeValue("type")
                : IMPLICIT_ACTION_TYPES.get(actionIdOf(actionRef));
        ActionDefaults defaults = type != null ? ACTION_TYPE_DEFAULTS.get(type) : null;

        if (button.getText() == null || button.getText().isEmpty()) {
            String text = actionElement != null
                    ? LoaderUtils.loadString(actionElement, "text")
                            .map(value -> resolveText(environment, value))
                            .orElse(null)
                    : null;
            if (text == null && defaults != null) {
                text = defaults.text();
            }
            if (text == null && (type == null || !ICON_ONLY_TYPES.contains(type))) {
                text = humanizeActionId(actionIdOf(actionRef));
            }
            if (text != null) {
                button.setText(text);
            }
        }

        if (button.getIcon() == null) {
            com.vaadin.flow.component.Component icon = actionElement != null
                    ? ComponentLoaderUtils.loadIconSetIcon(actionElement).orElse(null)
                    : null;
            if (icon == null && defaults != null && defaults.icon() != null) {
                icon = defaults.icon().create();
            }
            if (icon != null) {
                button.setIcon(icon);
            }
        }

        if (actionElement != null) {
            LoaderUtils.loadString(actionElement, "description")
                    .ifPresent(description -> button.setTitle(resolveText(environment, description)));
            LoaderUtils.loadBoolean(actionElement, "enabled", button::setEnabled);
            LoaderUtils.loadBoolean(actionElement, "visible", button::setVisible);
            LoaderUtils.loadString(actionElement, "variant").ifPresent(variant -> {
                switch (variant) {
                    case "PRIMARY" -> button.getThemeNames().add("primary");
                    case "DANGER" -> button.getThemeNames().add("error");
                    case "SUCCESS" -> button.getThemeNames().add("success");
                    default -> { /* no theme for unknown variants */ }
                }
            });
        }
    }

    /**
     * Resolves an action reference: {@code holderId.actionId} searches the {@code <action>} inside
     * the element with {@code holderId}, a bare {@code actionId} searches the whole view.
     */
    @Nullable
    private static Element findActionByRef(Element viewElement, String actionRef) {
        int separator = actionRef.lastIndexOf('.');
        if (separator > 0) {
            Element holder = findElementById(viewElement, actionRef.substring(0, separator));
            return holder != null
                    ? findDescendantAction(holder, actionRef.substring(separator + 1))
                    : null;
        }
        return findDescendantAction(viewElement, actionRef);
    }

    @Nullable
    private static Element findElementById(Element parent, String id) {
        for (Element child : parent.elements()) {
            if (id.equals(child.attributeValue("id"))) {
                return child;
            }
            Element found = findElementById(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static String actionIdOf(String actionRef) {
        int separator = actionRef.lastIndexOf('.');
        return separator > 0 ? actionRef.substring(separator + 1) : actionRef;
    }

    /** {@code showRoleAssignmentsAction} → {@code Show role assignments}. */
    private static String humanizeActionId(String actionId) {
        String base = actionId.endsWith("Action") && actionId.length() > "Action".length()
                ? actionId.substring(0, actionId.length() - "Action".length())
                : actionId;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < base.length(); i++) {
            char c = base.charAt(i);
            if (i == 0) {
                result.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                result.append(' ').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static void addPlaceholderItems(BiConsumer<String, String> itemAdder) {
        for (int i = 0; i < PLACEHOLDER_ITEM_COUNT; i++) {
            itemAdder.accept("menuItem" + i, "Menu item " + i);
        }
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * A menu entry from {@link StudioPreviewEnvironment#mainMenuItems()}.
     */
    public record MenuEntry(String id, String title, @Nullable String icon, List<MenuEntry> items) {
    }

    /**
     * Parses the {@link StudioPreviewEnvironment#mainMenuItems()} JSON. Empty list on any problem.
     */
    public static List<MenuEntry> parseMenuItems(@Nullable String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = JSON.readTree(json);
            return root.isArray() ? readMenuEntries(root) : List.of();
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private static List<MenuEntry> readMenuEntries(JsonNode arrayNode) {
        List<MenuEntry> entries = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            String id = textField(node, "id");
            String title = textField(node, "title");
            if (title == null) {
                title = id;
            }
            if (id == null && title == null) {
                continue;
            }
            JsonNode items = node.get("items");
            entries.add(new MenuEntry(id != null ? id : title, title, textField(node, "icon"),
                    items != null && items.isArray() ? readMenuEntries(items) : List.of()));
        }
        return entries;
    }

    /**
     * Parses a flat JSON array of objects with string fields (entityProperties/enumItems formats).
     * Empty list on any problem.
     */
    public static List<Map<String, String>> parseObjectArray(@Nullable String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = JSON.readTree(json);
            if (!root.isArray()) {
                return List.of();
            }
            List<Map<String, String>> result = new ArrayList<>();
            for (JsonNode node : root) {
                Map<String, String> object = new LinkedHashMap<>();
                node.properties().forEach(property -> {
                    if (property.getValue().isValueNode()) {
                        object.put(property.getKey(), property.getValue().asString());
                    }
                });
                result.add(object);
            }
            return result;
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    @Nullable
    private static String textField(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isValueNode() && !value.isNull() ? value.asString() : null;
    }

    /**
     * The data container id relevant for an element: an inherited {@code dataContainer}, or — for
     * filter components that reference a {@code dataLoader} — the container owning that loader
     * in the descriptor's {@code <data>} section (Studio resolves containers, not loaders).
     */
    @Nullable
    public static String resolveDataContainer(Element element) {
        String dataContainer = findInheritedAttribute(element, "dataContainer");
        if (dataContainer != null) {
            return dataContainer;
        }
        String dataLoader = findInheritedAttribute(element, "dataLoader");
        if (dataLoader == null || element.getDocument() == null) {
            return dataLoader;
        }
        Element data = element.getDocument().getRootElement().element("data");
        if (data == null) {
            return dataLoader;
        }
        for (Element container : data.elements()) {
            Element loader = container.element("loader");
            if (loader != null && dataLoader.equals(loader.attributeValue("id"))) {
                return container.attributeValue("id");
            }
        }
        return dataLoader;
    }

    /**
     * The {@code class} attribute of the data container with the given id in this element's own
     * document, {@code null} when the container or the attribute is absent.
     */
    @Nullable
    public static String containerEntityClass(Element element, @Nullable String containerId) {
        if (containerId == null || element.getDocument() == null) {
            return null;
        }
        Element data = element.getDocument().getRootElement().element("data");
        if (data == null) {
            return null;
        }
        for (Element container : data.elements()) {
            if (containerId.equals(container.attributeValue("id"))) {
                return container.attributeValue("class");
            }
        }
        return null;
    }

    @Nullable
    private static String findInheritedAttribute(Element element, String attributeName) {
        for (Element current = element; current != null; current = current.getParent()) {
            String value = current.attributeValue(attributeName);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Placeholder items for a list component bound to an enum entity attribute: the enum's
     * localized constants instead of the generic "Item N". Empty when the property is not an
     * enum or the environment cannot resolve it.
     */
    public static List<String> enumPlaceholderItems(Element element, StudioPreviewEnvironment environment) {
        String property = LoaderUtils.loadString(element, "property").orElse(null);
        if (property == null) {
            return List.of();
        }
        String dataContainer = resolveDataContainer(element);
        String metaClass = LoaderUtils.loadString(element, "metaClass")
                .orElseGet(() -> containerEntityClass(element, dataContainer));
        String type = parseObjectArray(environment.entityProperties(dataContainer, metaClass)).stream()
                .filter(p -> property.equals(p.get("name")))
                .map(p -> p.get("type"))
                .filter(t -> t != null && t.contains("."))
                .findFirst()
                .orElse(null);
        if (type == null) {
            return List.of();
        }
        return parseObjectArray(environment.enumItems(type)).stream()
                .map(item -> {
                    String caption = item.get("caption");
                    return caption != null && !caption.isBlank() ? caption : item.get("name");
                })
                .filter(caption -> caption != null && !caption.isBlank())
                .toList();
    }

    /**
     * Whether {@code itemsElement} holds at least one child the caller can actually render.
     * An {@code <items>} block of only non-renderable children (e.g. {@code componentItem}, which
     * needs the runtime {@code LayoutLoader}) would otherwise preview as an empty menu, so callers
     * use this to fall back to {@link #addPlaceholderItems} instead.
     *
     * @param renderableNames item tag names the calling loader renders
     */
    public static boolean hasRenderableItem(@Nullable Element itemsElement, Set<String> renderableNames) {
        if (itemsElement == null) {
            return false;
        }
        for (Element child : itemsElement.elements()) {
            if (renderableNames.contains(child.getName())) {
                return true;
            }
        }
        return false;
    }
}
