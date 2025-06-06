package ${packageName};

import ${entity.fqn};<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent;<%} else {%>
import ${routeLayout.getControllerFqn()};
<%}%>import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.flowui.view.*;

import java.util.Collection;
import java.util.List;

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController(id = "${viewId}")
@ViewDescriptor(path = "${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "50em")
public class ${viewControllerName} extends StandardListView<${entity.className}> {
<%if (generateDelegates) {%>
    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    protected List<${entity.className}> ${tableDl}LoadDelegate(LoadContext<${entity.className}> loadContext) {
        // Here you can load entities from an external storage.
        // Set the loaded entities to the not-new state using EntityStates.setNew(entity, false).
        return List.of();
    }<%if (tableActions.contains("remove")) {%>

    @Install(to = "${tableId}.removeAction", subject = "delegate")
    private void ${tableId}RemoveActionDelegate(final Collection<${entity.className}> collection) {
        for (${entity.className} entity : collection) {
            // Here you can remove entities from an external storage
        }
    }<%}%><%}%>
}
