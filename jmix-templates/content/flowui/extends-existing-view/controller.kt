package ${packageName}

import ${extendView.layoutClassFqn}
import com.vaadin.flow.router.Route
import io.jmix.ui.screen.UiController
import io.jmix.ui.screen.UiDescriptor
import ${extendView.controllerFqn}
<%if (classComment) {%>
${classComment}<%}%>
@Route(value = "${viewRoute}", layout = ${extendView.layoutClassName}::class.java)
@UiController("${api.escapeKotlinDollar(viewId)}")
@UiDescriptor("${descriptorName}.xml")
class ${controllerName} : ${extendView.controllerClassName}()