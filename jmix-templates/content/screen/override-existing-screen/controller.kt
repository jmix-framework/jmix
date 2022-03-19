<%
def importedClassFqn = ""
def extendedClassName = ""
if (overrideActionType == "EMPTY") {
    importedClassFqn=overrideScreen.ancestorFqn
    extendedClassName=overrideScreen.ancestorControllerClassName
} else {
    importedClassFqn=overrideScreen.controllerFqn
    extendedClassName=overrideScreen.controllerClassName
}
%>package ${packageName}

import io.jmix.ui.screen.UiController
import io.jmix.ui.screen.UiDescriptor
import ${importedClassFqn}
<%if (classComment) {%>
${classComment}<%}%>
@UiController("${api.escapeKotlinDollar(screenId)}")
@UiDescriptor("${descriptorName}.xml")
class ${controllerName} : ${extendedClassName}()