package ${packageName};

<%
if(!extendScreen.legacy) {%>import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
<%}%>import ${extendScreen.controllerFqn};
<%if (classComment) {%>
${classComment}<%}%><%
if(!extendScreen.legacy) {%>
@UiController("${screenId}")
@UiDescriptor("${descriptorName}.xml")<%}%>
public class ${controllerName} extends ${extendScreen.controllerClassName} {
}