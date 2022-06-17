package ${packageName};

import com.vaadin.flow.router.Route;
<%if (controllerName != "StandardScreen") {
%>import io.jmix.flowui.screen.StandardScreen;<%
superClass = "StandardScreen"
} else {
superClass = "io.jmix.flowui.screen.StandardScreen"}
%>
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;

<%if (classComment) {%>
        ${classComment}
<%}%>@Route(value = "${route}")
@UiController("${id}")
@UiDescriptor("${descriptorName}.xml")
public class ${controllerName} extends ${superClass} {
}