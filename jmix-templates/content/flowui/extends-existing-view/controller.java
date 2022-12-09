package ${packageName};

import ${extendView.layoutClassFqn};
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import ${extendView.controllerFqn};
<%if (classComment) {%>
${classComment}<%}%>
@Route(value = "${viewRoute}", layout = ${extendView.layoutClassName}.class)
@ViewController("${viewId}")
@ViewDescriptor("${descriptorName}.xml")
public class ${controllerName} extends ${extendView.controllerClassName} {
}