package ${packageName}

<%
if(!extendScreen.legacy) {%>import io.jmix.ui.screen.UiController
import io.jmix.ui.screen.UiDescriptor
<%}%>import ${extendScreen.controllerFqn}
<%if (classComment) {%>
${classComment}<%}%><%
if(!extendScreen.legacy) {%>
@UiController("${api.escapeKotlinDollar(screenId)}")
@UiDescriptor("${descriptorName}.xml")<%}%>
class ${controllerName} : ${extendScreen.controllerClassName}()