package io.jmix.flowui.view.navigation;

import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.sys.ViewSupport;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

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
        return navigator.getRouteParameters().orElseGet(() -> {
            switch (navigator.getMode()) {
                case CREATE:
                    return generateNewEntityRouteParameters(navigator);
                case EDIT:
                    return generateEditEntityRouteParameters(navigator);
                default:
                    throw new IllegalStateException("Unknown detail view mode: " + navigator.getMode());
            }
        });
    }

    protected RouteParameters generateNewEntityRouteParameters(DetailViewNavigator<?> navigator) {
        return NavigationUtils.generateRouteParameters(navigator, "id", StandardDetailView.NEW_ENTITY_ID);
    }

    protected RouteParameters generateEditEntityRouteParameters(DetailViewNavigator<?> navigator) {
        Object entity = navigator.getEditedEntity().orElseThrow(() -> new IllegalStateException(
                String.format("Detail View of %s cannot be open with mode EDIT, entity is not set",
                        navigator.getEntityClass())));

        Object id = requireNonNull(EntityValues.getId(entity));
        return NavigationUtils.generateRouteParameters(navigator, "id", UrlIdSerializer.serializeId(id));
    }
}
