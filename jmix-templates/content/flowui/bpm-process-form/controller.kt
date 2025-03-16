<%
def processAnnotation = api.processSnippet('bpm_processFormAnnotation.xml',
    ['outputes': processFormOutcomes,
     'injects': bpmInjects,
     'entity': entity != null ? entity : entityObject,
     'entityVarName': studioUtils.nullize(entityVarName) != null ? entityVarName : entityObjectName,
     'formTemplate': processFormTemplate,
     'isKotlin': true,
     'api': api])
%>package ${packageName}

<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent
<%} else {%>
import ${routeLayout.getControllerFqn()}
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

${processAnnotation}
@Route(value = "${route}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>MainView::class<%}%>)
@ViewController(id = "${id}")
@ViewDescriptor(path = "${descriptorName}.xml")
class ${controllerName} : ${superClass}() {

    @Autowired
    private lateinit var processFormContext: ProcessFormContext

}