package ${packageName}

import ${extendView.getLayoutClassFqn(module_basePackage)}
import com.vaadin.flow.router.Route
import io.jmix.flowui.view.ViewController
import io.jmix.flowui.view.ViewDescriptor
import ${extendView.controllerFqn}
<%if (classComment) {%>
${classComment}<%}%>
@Route(value = "${viewRoute}", layout = ${extendView.getLayoutClassName(module_basePackage)}::class)
@ViewController(id = "${api.escapeKotlinDollar(viewId)}")
@ViewDescriptor(path = "${descriptorName}.xml")
class ${controllerName} : ${extendView.controllerClassName}()