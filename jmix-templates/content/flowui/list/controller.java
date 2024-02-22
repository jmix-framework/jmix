package ${packageName};

import ${entity.fqn};
<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent;
<%} else {%>
import ${routeLayout.getControllerFqn()};
<%}%>
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController("${viewId}")
@ViewDescriptor("${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "64em")
public class ${viewControllerName} extends StandardListView<${entity.className}> {
}