package ${packageName};

<%if (controllerName != "Screen") {
%>import io.jmix.ui.screen.Screen;<%
    superClass = "Screen"
} else {
    superClass = "io.jmix.ui.screen.Screen"}
%>
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${id}")
@UiDescriptor("${descriptorName}.xml")
public class ${controllerName} extends ${superClass} {
}