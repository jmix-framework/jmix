package ${packageName}

<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent
<%} else {%>
import ${module_basePackage}.view.main.MainView
<%}%>import com.vaadin.flow.router.Route
import io.jmix.bpmflowui.processform.ProcessFormContext
import io.jmix.bpmflowui.processform.annotation.ProcessForm
<%if (controllerName != "StandardView") {
%>import io.jmix.flowui.view.StandardView<%
superClass = "StandardView"
} else {
superClass = "io.jmix.flowui.view.StandardView"}
%>
import io.jmix.flowui.view.ViewController
import io.jmix.flowui.view.ViewDescriptor
import org.springframework.beans.factory.annotation.Autowired

@ProcessForm
@Route(value = "${route}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent::class <%} else {%>MainView::class<%}%>)
@ViewController("${id}")
@ViewDescriptor("${descriptorName}.xml")
class ${controllerName} : ${superClass}() {

    @Autowired
    private ProcessFormContext processFormContext

}