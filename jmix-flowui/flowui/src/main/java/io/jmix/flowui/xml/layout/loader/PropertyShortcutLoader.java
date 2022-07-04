package io.jmix.flowui.xml.layout.loader;

import com.google.common.collect.ImmutableMap;
import io.jmix.flowui.FlowUiComponentProperties;
import io.jmix.flowui.FlowUiViewProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

@Component("flowui_PropertyShortcutLoader")
public class PropertyShortcutLoader {

    protected static final Map<String, Function<FlowUiComponentProperties, String>> COMPONENTS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<FlowUiComponentProperties, String>>builder()
                    .put("GRID_CREATE_SHORTCUT", FlowUiComponentProperties::getGridCreateShortcut)
                    /*.put("GRID_ADD_SHORTCUT", FlowUiComponentProperties::getGridAddShortcut)*/
                    .put("GRID_EDIT_SHORTCUT", FlowUiComponentProperties::getGridEditShortcut)
                    .put("GRID_REMOVE_SHORTCUT", FlowUiComponentProperties::getGridRemoveShortcut)
                    /*.put("GRID_VIEW_SHORTCUT", FlowUiComponentProperties::getGridViewShortcut)*/
                    .build();

    protected static final Map<String, Function<FlowUiViewProperties, String>> VIEWS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<FlowUiViewProperties, String>>builder()
                    .put("COMMIT_SHORTCUT", FlowUiViewProperties::getCommitShortcut)
                    .put("CLOSE_SHORTCUT", FlowUiViewProperties::getCloseShortcut)
                    .build();

    protected FlowUiComponentProperties componentProperties;
    protected FlowUiViewProperties viewProperties;

    public PropertyShortcutLoader(FlowUiComponentProperties componentProperties, FlowUiViewProperties viewProperties) {
        this.componentProperties = componentProperties;
        this.viewProperties = viewProperties;
    }

    public boolean contains(String alias) {
        return COMPONENTS_SHORTCUT_ALIASES.containsKey(alias)
                || VIEWS_SHORTCUT_ALIASES.containsKey(alias);
    }

    public String getShortcut(String alias) {
        Function<FlowUiComponentProperties, String> componentsShortcut = COMPONENTS_SHORTCUT_ALIASES.get(alias);
        if (componentsShortcut != null) {
            return componentsShortcut.apply(componentProperties);
        }

        Function<FlowUiViewProperties, String> viewsShortcut = VIEWS_SHORTCUT_ALIASES.get(alias);
        if (viewsShortcut != null) {
            return viewsShortcut.apply(viewProperties);
        }

        throw new IllegalStateException(String.format("There is no shortcut for alias '%s'", alias));
    }
}
