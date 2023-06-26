package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent<%} else {%>
import ${module_basePackage}.view.main.MainView<%}%>
import com.vaadin.flow.router.Route
import io.jmix.flowui.view.*

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent::class <%} else {%>MainView::class<%}%>)
@ViewController("${viewId}")
@ViewDescriptor("${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "64em")
class ${viewControllerName} : StandardListView<${entity.className}>() {
}