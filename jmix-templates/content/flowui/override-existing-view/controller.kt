<%
def importedClassFqn = ""
def extendedClassName = ""
if (overrideActionType == "EMPTY") {
    importedClassFqn=overrideView.ancestorFqn
    extendedClassName=overrideView.ancestorControllerClassName
} else {
    importedClassFqn=overrideView.controllerFqn
    extendedClassName=overrideView.controllerClassName
}
%>package ${packageName}

import ${overrideView.layoutClassFqn}
import com.vaadin.flow.router.Route
import io.jmix.flowui.view.ViewController
import io.jmix.flowui.view.ViewDescriptor
import ${importedClassFqn}
<%if (classComment) {%>
    ${classComment}<%}%>
@Route(value = "${viewRoute}", layout = ${overrideView.layoutClassName}::class)
@ViewController("${api.escapeKotlinDollar(viewId)}")
@ViewDescriptor("${descriptorName}.xml")
class ${controllerName} : ${extendedClassName}()