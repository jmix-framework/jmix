package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent<%} else {%>
import ${routeLayout.getControllerFqn()}
<%}%>import com.vaadin.flow.router.Route
import io.jmix.core.LoadContext
import io.jmix.flowui.view.*
import io.jmix.flowui.view.Target

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController("${viewId}")
@ViewDescriptor("${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "50em")
class ${viewControllerName} : StandardListView<${entity.className}>() {

    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    fun ${tableDl}LoadDelegate(loadContext: LoadContext<${entity.className}>): MutableList<${entity.className}> {
        // Here you can load entities from an external store.
        // Set the loaded entities to the not-new state using EntityStates.setNew(entity, false).
        return mutableListOf()
    }

    @Install(to = "${tableId}.remove", subject = "delegate")
    fun ${tableId}RemoveDelegate(entities: Collection<${entity.className}>) {
        // Here you can remove entities from an external storage
    }
}