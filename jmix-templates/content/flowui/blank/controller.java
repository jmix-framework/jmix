package ${packageName};

<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent;
<%} else {%>
import ${module_basePackage}.view.main.MainView;
<%}%>
import com.vaadin.flow.router.Route;
<%if (controllerName != "StandardView") {
%>import io.jmix.flowui.view.StandardView;<%
superClass = "StandardView"
} else {
superClass = "io.jmix.flowui.view.StandardView"}
%>
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

<%if (classComment) {%>
        ${classComment}
<%}%>@Route(value = "${route}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent.class <%} else {%>MainView.class<%}%>)
@ViewController("${id}")
@ViewDescriptor("${descriptorName}.xml")
public class ${controllerName} extends ${superClass} {
}