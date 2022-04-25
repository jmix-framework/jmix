package io.jmix.flowui.screen.navigation;

import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenRegistry;
import io.jmix.flowui.screen.StandardEditor;
import io.jmix.flowui.sys.ScreenSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Internal
@Component("flowui_EditorNavigatorProcessor")
public class EditorNavigationProcessor extends AbstractNavigationProcessor<EditorNavigator<?>> {

    public EditorNavigationProcessor(ScreenSupport screenSupport,
                                     ScreenRegistry screenRegistry,
                                     ScreenNavigationSupport navigationSupport) {
        super(screenSupport, screenRegistry, navigationSupport);
    }

    @Override
    protected Class<? extends Screen> inferScreenClass(EditorNavigator<?> navigator) {
        return screenRegistry.getEditorScreen(navigator.getEntityClass()).getControllerClass();
    }

    @Override
    protected RouteParameters getRouteParameters(EditorNavigator<?> navigator) {
        switch (navigator.getMode()) {
            case CREATE:
                return generateNewEntityRouteParameters(navigator);
            case EDIT:
                return generateEditEntityRouteParameters(navigator);
            default:
                throw new IllegalStateException("Unknown editor mode: " + navigator.getMode());
        }
    }

    protected RouteParameters generateNewEntityRouteParameters(EditorNavigator<?> navigator) {
        return generateRouteParameters(navigator, StandardEditor.NEW_ENTITY_ID);
    }

    protected RouteParameters generateEditEntityRouteParameters(EditorNavigator<?> navigator) {
        Object entity = navigator.getEditedEntity().orElseThrow(() -> new IllegalStateException(
                String.format("Editor of %s cannot be open with mode EDIT, entity is not set",
                        navigator.getEntityClass())));

        Object id = EntityValues.getId(entity);
        return generateRouteParameters(navigator, UrlIdSerializer.serializeId(id));
    }

    protected RouteParameters generateRouteParameters(EditorNavigator<?> navigator, String entityId) {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("id", entityId);

        if (navigator.getRouteParameters().isPresent()) {
            RouteParameters routeParameters = navigator.getRouteParameters().get();
            for (String name : routeParameters.getParameterNames()) {
                //noinspection OptionalGetWithoutIsPresent
                paramsMap.put(name, routeParameters.get(name).get());
            }
        }

        return new RouteParameters(paramsMap);
    }
}
