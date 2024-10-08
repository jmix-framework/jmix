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

import ${overrideView.getLayoutClassFqn(module_basePackage)}
import com.vaadin.flow.router.Route
import io.jmix.flowui.view.ViewController
import io.jmix.flowui.view.ViewDescriptor
import ${importedClassFqn}
<%if (classComment) {%>
    ${classComment}<%}%>
@Route(value = "${viewRoute}", layout = ${overrideView.getLayoutClassName(module_basePackage)}::class)
@ViewController(id = "${api.escapeKotlinDollar(viewId)}")
@ViewDescriptor(path = "${descriptorName}.xml")
class ${controllerName} : ${extendedClassName}()