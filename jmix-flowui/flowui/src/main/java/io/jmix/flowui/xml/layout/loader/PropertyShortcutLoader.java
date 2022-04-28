package io.jmix.flowui.xml.layout.loader;

import com.google.common.collect.ImmutableMap;
import io.jmix.flowui.FlowUiComponentProperties;
import io.jmix.flowui.FlowUiScreenProperties;
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

    protected static final Map<String, Function<FlowUiScreenProperties, String>> SCREENS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<FlowUiScreenProperties, String>>builder()
                    .put("COMMIT_SHORTCUT", FlowUiScreenProperties::getCommitShortcut)
                    .put("CLOSE_SHORTCUT", FlowUiScreenProperties::getCloseShortcut)
                    .build();

    protected FlowUiComponentProperties componentProperties;
    protected FlowUiScreenProperties screenProperties;

    public PropertyShortcutLoader(FlowUiComponentProperties componentProperties, FlowUiScreenProperties screenProperties) {
        this.componentProperties = componentProperties;
        this.screenProperties = screenProperties;
    }

    public boolean contains(String alias) {
        return COMPONENTS_SHORTCUT_ALIASES.containsKey(alias)
                || SCREENS_SHORTCUT_ALIASES.containsKey(alias);
    }

    public String getShortcut(String alias) {
        Function<FlowUiComponentProperties, String> compShortcut = COMPONENTS_SHORTCUT_ALIASES.get(alias);
        if (compShortcut != null) {
            return compShortcut.apply(componentProperties);
        }

        Function<FlowUiScreenProperties, String> screensShortcut = SCREENS_SHORTCUT_ALIASES.get(alias);
        if (screensShortcut != null) {
            return screensShortcut.apply(screenProperties);
        }

        throw new IllegalStateException(String.format("There is no shortcut for alias '%s'", alias));
    }
}
