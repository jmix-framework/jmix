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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.xml.layout.support.BaseComponentLoaderSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

/**
 * Studio preview loader for {@code gridColumnVisibility}: instantiates a {@link JmixMenuBar}
 * styled as the runtime component and, when a real environment is available, builds the root
 * dropdown item and its submenu entries from the referenced grid's declared columns, so the
 * designer preview shows the real column list instead of 5 fake placeholder entries.
 * <p>
 * <b>Env-gated</b>, like {@link StudioGridPreviewLoader} and unlike
 * {@link StudioDropdownButtonPreviewLoader}: Studio's designer special-cases
 * {@code gridColumnVisibility} in its generic {@code HasMenuItemsEnhanced} post-init (every
 * released version, since {@link JmixMenuBar} is one), adding its own empty root item plus 5 fake
 * submenu entries. Without an environment handshake (old Studio, routed through the 2-arg
 * {@link #load(Element, Element)} to {@link StudioPreviewEnvironment#NOOP NOOP}) there is no
 * bind-by-key guard on the caller side, so a loader-built root here would stack on top of old
 * Studio's own root, giving the preview two dropdown roots. So when {@code environment} is
 * {@code NOOP}, the loader returns a bare {@link JmixMenuBar} with only the base attributes and
 * the {@code jmix-role} styling attribute applied (mirrors Studio's post-init styling hook) and
 * builds neither the root item nor any submenu entries, leaving old Studio's own post-init to
 * build them exactly as it does today.
 * <p>
 * Limitations inherent to a data-less preview:
 * <ul>
 *     <li>Only explicitly declared {@code column} children of the referenced grid's
 *     {@code columns} element are considered; other {@code columns} children (e.g.
 *     {@code editorActionsColumn}) are not meaningful visibility toggles and are skipped.</li>
 *     <li>The root item shows either its {@code icon} attribute (via
 *     {@link BaseComponentLoaderSupport#loadIconSetIcon(Element)}) or its {@code text} attribute,
 *     never both — a nested {@code <icon>} component element is not built (would need the
 *     runtime {@code LayoutLoader}, unavailable to a spring-free kit loader), matching the
 *     dropdown loader's same limitation.</li>
 *     <li>{@code showAllEnabled}/{@code hideAllEnabled} header items are not built: they are
 *     click-driven runtime behavior with no meaningful static representation.</li>
 * </ul>
 */
public class StudioGridColumnVisibilityPreviewLoader implements StudioPreviewComponentLoader {

    protected static final String DATA_GRID_ATTRIBUTE = "dataGrid";
    protected static final String INCLUDE_ATTRIBUTE = "include";
    protected static final String EXCLUDE_ATTRIBUTE = "exclude";
    protected static final String TEXT_ATTRIBUTE = "text";
    protected static final String ID_ATTRIBUTE = "id";
    protected static final String KEY_ATTRIBUTE = "key";
    protected static final String PROPERTY_ATTRIBUTE = "property";
    protected static final String HEADER_ATTRIBUTE = "header";
    protected static final String DATA_CONTAINER_ATTRIBUTE = "dataContainer";
    protected static final String META_CLASS_ATTRIBUTE = "metaClass";

    protected static final String REF_COLUMN_ATTRIBUTE = "refColumn";

    protected static final String ATTRIBUTE_JMIX_ROLE_NAME = "jmix-role";
    protected static final String ATTRIBUTE_JMIX_ROLE_VALUE = "jmix-grid-column-visibility";

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && StudioXmlElements.GRID_COLUMN_VISIBILITY.equals(element.getName());
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        JmixMenuBar menuBar = new JmixMenuBar();
        loadComponentBaseAttributes(menuBar, componentElement);
        menuBar.getElement().setAttribute(ATTRIBUTE_JMIX_ROLE_NAME, ATTRIBUTE_JMIX_ROLE_VALUE);

        // Old-Studio compatibility: see class javadoc. Without an environment handshake, old
        // Studio's postInitHasMenuItems adds its own root + 5 fake entries on top of whatever the
        // loader returns, so neither the root item nor submenu entries are built here.
        if (environment != StudioPreviewEnvironment.NOOP) {
            buildMenu(menuBar, componentElement, viewElement, environment);
        }

        return menuBar;
    }

    protected void buildMenu(JmixMenuBar menuBar, Element componentElement, Element viewElement,
                             StudioPreviewEnvironment environment) {
        // Missing/empty dataGrid attribute (normal transient state while typing the XML): mirrors
        // Studio's old postInitHasMenuItems placeholder so Studio itself can stay silent.
        String gridId = loadString(componentElement, DATA_GRID_ATTRIBUTE).orElse(null);
        if (gridId == null) {
            buildPlaceholderMenu(menuBar, componentElement, environment);
            return;
        }

        JmixMenuItem rootItem = loadRootItem(menuBar, componentElement, environment);

        Element gridElement = findGridElement(viewElement, gridId);
        if (gridElement == null) {
            return;
        }

        Element columnsElement = gridElement.element(StudioXmlElements.COLUMNS);
        if (columnsElement == null) {
            return;
        }

        loadSubMenuEntries(rootItem, componentElement, columnsElement, gridElement, environment);
    }

    protected void buildPlaceholderMenu(JmixMenuBar menuBar, Element componentElement,
                                        StudioPreviewEnvironment environment) {
        JmixMenuItem rootItem = loadRootItem(menuBar, componentElement, environment);
        for (int i = 0; i < 5; i++) {
            rootItem.getSubMenu().addItem("Menu item " + i);
        }
    }

    /**
     * Root item: {@code icon} attribute (added as a component, no text) takes precedence over
     * {@code text} (resolved via {@link PreviewActionSupport#resolveText}); with neither, an
     * empty-text root is added, matching {@code JmixGridColumnVisibility}'s own bare-root look.
     */
    protected JmixMenuItem loadRootItem(JmixMenuBar menuBar, Element componentElement,
                                        StudioPreviewEnvironment environment) {
        return BaseComponentLoaderSupport.loadIconSetIcon(componentElement)
                .<JmixMenuItem>map(menuBar::addItem)
                .orElseGet(() -> loadString(componentElement, TEXT_ATTRIBUTE)
                        .map(text -> menuBar.addItem(PreviewActionSupport.resolveText(environment, text)))
                        .orElseGet(() -> menuBar.addItem("")));
    }

    /**
     * Builds one submenu entry per {@code column} child of {@code columnsElement}, in grid
     * document order (mirrors {@code AssignGridColumnVisibilityPropertiesInitTask}'s runtime
     * semantics: it filters the grid's already-ordered {@code Grid#getColumns()} by include/exclude
     * membership rather than iterating the {@code include} list, so declaration order always wins
     * over include-list order). Columns without a {@code key}/{@code property} are skipped, as the
     * runtime would throw building them.
     */
    protected void loadSubMenuEntries(JmixMenuItem rootItem, Element componentElement, Element columnsElement,
                                      Element gridElement, StudioPreviewEnvironment environment) {
        Map<String, String> menuItemTexts = loadMenuItemTexts(componentElement);
        Set<String> includeKeys = loadKeys(componentElement, INCLUDE_ATTRIBUTE);
        Set<String> excludeKeys = loadKeys(componentElement, EXCLUDE_ATTRIBUTE);

        for (Element columnElement : columnsElement.elements(StudioXmlElements.COLUMN)) {
            String property = loadString(columnElement, PROPERTY_ATTRIBUTE).orElse(null);
            String key = loadString(columnElement, KEY_ATTRIBUTE).orElse(property);
            if (key == null) {
                continue;
            }
            if (!includeKeys.isEmpty() && !includeKeys.contains(key)) {
                continue;
            }
            if (excludeKeys.contains(key)) {
                continue;
            }

            String label = resolveLabel(key, property, columnElement, gridElement, menuItemTexts, environment);
            rootItem.getSubMenu().addItem(label);
        }
    }

    protected Map<String, String> loadMenuItemTexts(Element componentElement) {
        Map<String, String> texts = new HashMap<>();
        for (Element menuItemElement : componentElement.elements(StudioXmlElements.MENU_ITEM)) {
            loadString(menuItemElement, REF_COLUMN_ATTRIBUTE).ifPresent(refColumn ->
                    loadString(menuItemElement, TEXT_ATTRIBUTE)
                            .ifPresent(text -> texts.put(refColumn, text)));
        }
        return texts;
    }

    protected Set<String> loadKeys(Element componentElement, String attributeName) {
        return loadString(componentElement, attributeName)
                .map(value -> Set.copyOf(split(value)))
                .orElse(Set.of());
    }

    /**
     * Label precedence: a {@code menuItem[@refColumn=key]/@text} override, then the column's
     * {@code header} attribute, then {@link StudioPreviewEnvironment#propertyCaption}, then the
     * raw {@code property} (or {@code key}, if no property was declared) — mirrors
     * {@link StudioGridPreviewLoader#loadColumnHeader} plus a final raw-key fallback so a submenu
     * entry always has a non-blank label.
     */
    protected String resolveLabel(String key, @Nullable String property, Element columnElement, Element gridElement,
                                  Map<String, String> menuItemTexts, StudioPreviewEnvironment environment) {
        String menuItemText = menuItemTexts.get(key);
        if (menuItemText != null) {
            return PreviewActionSupport.resolveText(environment, menuItemText);
        }

        String header = loadString(columnElement, HEADER_ATTRIBUTE).orElse(null);
        if (header != null) {
            return PreviewActionSupport.resolveText(environment, header);
        }

        if (property != null) {
            String dataContainerId = loadString(gridElement, DATA_CONTAINER_ATTRIBUTE).orElse(null);
            String metaClass = loadString(gridElement, META_CLASS_ATTRIBUTE).orElse(null);
            String caption = environment.propertyCaption(dataContainerId, metaClass, property);
            return caption != null ? caption : property;
        }

        return key;
    }

    /**
     * Recursively searches {@code parent}'s descendants for a {@code dataGrid}/{@code treeDataGrid}
     * element matching {@code gridId} (the referenced grid may live anywhere in the view, not
     * necessarily a sibling of the {@code gridColumnVisibility} element).
     */
    @Nullable
    protected Element findGridElement(Element parent, String gridId) {
        for (Element child : parent.elements()) {
            boolean isGrid = StudioXmlElements.DATA_GRID.equals(child.getName())
                    || StudioXmlElements.TREE_DATA_GRID.equals(child.getName());
            if (isGrid && gridId.equals(child.attributeValue(ID_ATTRIBUTE))) {
                return child;
            }
            Element found = findGridElement(child, gridId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
