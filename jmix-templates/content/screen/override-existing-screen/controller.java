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
%>package ${packageName};

import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import ${importedClassFqn};
<%if (classComment) {%>
${classComment}<%}%>
@UiController("${screenId}")
@UiDescriptor("${descriptorName}.xml")
public class ${controllerName} extends ${extendedClassName} {
}