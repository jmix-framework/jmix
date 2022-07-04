package io.jmix.flowui.view.navigation;

import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.sys.ViewSupport;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Internal
@Component("flowui_DetailViewNavigationProcessor")
public class DetailViewNavigationProcessor extends AbstractNavigationProcessor<DetailViewNavigator<?>> {

    public DetailViewNavigationProcessor(ViewSupport viewSupport,
                                         ViewRegistry viewRegistry,
                                         ViewNavigationSupport navigationSupport) {
        super(viewSupport, viewRegistry, navigationSupport);
    }

    @Override
    protected Class<? extends View> inferViewClass(DetailViewNavigator<?> navigator) {
        return viewRegistry.getDetailViewInfo(navigator.getEntityClass()).getControllerClass();
    }

    @Override
    protected RouteParameters getRouteParameters(DetailViewNavigator<?> navigator) {
        switch (navigator.getMode()) {
            case CREATE:
                return generateNewEntityRouteParameters(navigator);
            case EDIT:
                return generateEditEntityRouteParameters(navigator);
            default:
                throw new IllegalStateException("Unknown detail view mode: " + navigator.getMode());
        }
    }

    protected RouteParameters generateNewEntityRouteParameters(DetailViewNavigator<?> navigator) {
        return generateRouteParameters(navigator, StandardDetailView.NEW_ENTITY_ID);
    }

    protected RouteParameters generateEditEntityRouteParameters(DetailViewNavigator<?> navigator) {
        Object entity = navigator.getEditedEntity().orElseThrow(() -> new IllegalStateException(
                String.format("Detail View of %s cannot be open with mode EDIT, entity is not set",
                        navigator.getEntityClass())));

        Object id = EntityValues.getId(entity);
        return generateRouteParameters(navigator, UrlIdSerializer.serializeId(id));
    }

    protected RouteParameters generateRouteParameters(DetailViewNavigator<?> navigator, String entityId) {
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
