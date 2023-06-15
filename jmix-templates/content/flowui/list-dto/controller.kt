package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent<%} else {%>
import ${module_basePackage}.view.main.MainView
<%}%>import com.vaadin.flow.router.Route
import io.jmix.core.LoadContext
import io.jmix.flowui.view.*
import io.jmix.flowui.view.Target

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent::class <%} else {%>MainView::class<%}%>)
@ViewController("${viewId}")
@ViewDescriptor("${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "50em", height = "37.5em")
class ${viewControllerName} : StandardListView<${entity.className}>() {

    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    fun ${tableDl}LoadDelegate(loadContext: LoadContext<${entity.className}>): MutableList<${entity.className}> {
        // Here you can load entities from an external store
        return mutableListOf()
    }
}