package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent<%} else {%>
import ${routeLayout.getControllerFqn()}<%}%>
import com.vaadin.flow.router.Route
import io.jmix.core.LoadContext
import io.jmix.flowui.view.*
import io.jmix.flowui.view.Target

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController(id = "${viewId}")
@ViewDescriptor(path = "${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "50em")
class ${viewControllerName} : StandardListView<${entity.className}>() {
<%if (generateDelegates) {%>
    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    fun ${tableDl}LoadDelegate(loadContext: LoadContext<${entity.className}>): MutableList<${entity.className}> {
        // Here you can load entities from an external store.
        // Set the loaded entities to the not-new state using EntityStates.setNew(entity, false).
        return mutableListOf()
    }<%if (tableActions.contains("remove")) {%>

    @Install(to = "${tableId}.removeAction", subject = "delegate")
    fun ${tableId}RemoveActionDelegate(entities: Collection<${entity.className}>) {
        // Here you can remove entities from an external storage
    }<%}%><%}%>
}