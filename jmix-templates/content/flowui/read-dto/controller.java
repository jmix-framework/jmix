package ${packageName};

import ${entity.fqn};<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent;<%} else {%>
import ${routeLayout.getControllerFqn()};
<%}%>import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.flowui.view.*;

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${readRoute}/:${readRouteParam}/read", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController(id = "${readId}")
@ViewDescriptor(path = "${readDescriptorName}.xml")
@ReadEntityContainer("${dcId}")
public class ${readControllerName} extends StandardReadView<${entity.className}> {
<%if (generateDelegates) {%>
    @Install(to = "${dlId}", target = Target.DATA_LOADER)
    private ${entity.className} loadDelegate(final LoadContext<${entity.className}> loadContext) {
        Object id = loadContext.getId();
        // Here you can load the entity by id from an external storage.
        return null;
    }<%}%>
}
