package io.jmix.flowui.xml.layout.loader;

import com.google.common.collect.ImmutableMap;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.FlowuiViewProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

@Component("flowui_PropertyShortcutLoader")
public class PropertyShortcutLoader {

    protected static final Map<String, Function<FlowuiComponentProperties, String>> COMPONENTS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<FlowuiComponentProperties, String>>builder()
                    .put("GRID_CREATE_SHORTCUT", FlowuiComponentProperties::getGridCreateShortcut)
                    /*.put("GRID_ADD_SHORTCUT", FlowUiComponentProperties::getGridAddShortcut)*/
                    .put("GRID_EDIT_SHORTCUT", FlowuiComponentProperties::getGridEditShortcut)
                    .put("GRID_REMOVE_SHORTCUT", FlowuiComponentProperties::getGridRemoveShortcut)
                    /*.put("GRID_VIEW_SHORTCUT", FlowUiComponentProperties::getGridViewShortcut)*/
                    .build();

    protected static final Map<String, Function<FlowuiViewProperties, String>> VIEWS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<FlowuiViewProperties, String>>builder()
                    .put("COMMIT_SHORTCUT", FlowuiViewProperties::getCommitShortcut)
                    .put("CLOSE_SHORTCUT", FlowuiViewProperties::getCloseShortcut)
                    .build();

    protected FlowuiComponentProperties componentProperties;
    protected FlowuiViewProperties viewProperties;

    public PropertyShortcutLoader(FlowuiComponentProperties componentProperties, FlowuiViewProperties viewProperties) {
        this.componentProperties = componentProperties;
        this.viewProperties = viewProperties;
    }

    public boolean contains(String alias) {
        return COMPONENTS_SHORTCUT_ALIASES.containsKey(alias)
                || VIEWS_SHORTCUT_ALIASES.containsKey(alias);
    }

    public String getShortcut(String alias) {
        Function<FlowuiComponentProperties, String> componentsShortcut = COMPONENTS_SHORTCUT_ALIASES.get(alias);
        if (componentsShortcut != null) {
            return componentsShortcut.apply(componentProperties);
        }

        Function<FlowuiViewProperties, String> viewsShortcut = VIEWS_SHORTCUT_ALIASES.get(alias);
        if (viewsShortcut != null) {
            return viewsShortcut.apply(viewProperties);
        }

        throw new IllegalStateException(String.format("There is no shortcut for alias '%s'", alias));
    }
}
