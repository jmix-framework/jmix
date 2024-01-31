<%
def processAnnotation = api.processSnippet('bpm_processFormAnnotation.xml',
    ['outputes': processFormOutcomes,
     'formTemplate': processFormTemplate,
     'api': api])
def processComponents = api.processSnippet('bpm_components.xml',
     ['injects': bpmInjects,
     'formTemplate': processFormTemplate,
     'entity': entity != null ? entity : entityObject,
     'entityVarName': studioUtils.nullize(entityVarName) != null ? entityVarName : entityObjectName,
     'api': api])
def processHandlers = api.processSnippet('bpm_handlers.xml',
    ['outputes': processFormOutcomes,
     'formTemplate': processFormTemplate,
     'formType': processFormType,
     'entityVarName': studioUtils.nullize(entityVarName) != null ? entityVarName : entityObjectName,
     'api': api])
%>package ${packageName};

<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent;
<%} else {%>
import ${module_basePackage}.view.main.MainView;
<%}%>import com.vaadin.flow.router.Route;
import io.jmix.bpmflowui.processform.ProcessFormContext;
import io.jmix.bpmflowui.processform.annotation.ProcessForm;
<%if (controllerName != "StandardView") {
%>import io.jmix.flowui.view.StandardView;<%
superClass = "StandardView"
} else {
superClass = "io.jmix.flowui.view.StandardView"}
%>
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

${processAnnotation}
@Route(value = "${route}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent.class <%} else {%>MainView.class<%}%>)
@ViewController("${id}")
@ViewDescriptor("${descriptorName}.xml")
public class ${controllerName} extends ${superClass} {

    @Autowired
    private ProcessFormContext processFormContext;${processComponents}
${processHandlers}}