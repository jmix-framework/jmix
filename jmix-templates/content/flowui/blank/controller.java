package ${packageName};

import ${module_basePackage}.view.main.MainView;
import com.vaadin.flow.router.Route;
<%if (controllerName != "StandardView") {
%>import io.jmix.flowui.view.StandardView;<%
superClass = "StandardView"
} else {
superClass = "io.jmix.flowui.view.StandardView"}
%>
import io.jmix.flowui.view.UiController;
import io.jmix.flowui.view.UiDescriptor;

<%if (classComment) {%>
        ${classComment}
<%}%>@Route(value = "${route}", layout = MainView.class)
@UiController("${id}")
@UiDescriptor("${descriptorName}.xml")
public class ${controllerName} extends ${superClass} {
}