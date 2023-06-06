package ${packageName};

import ${entity.fqn};<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent;<%} else {%>
import ${module_basePackage}.view.main.MainView;
<%}%>import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${detailRoute}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent.class <%} else {%>MainView.class<%}%>)
@ViewController("${detailId}")
@ViewDescriptor("${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
public class ${detailControllerName} extends StandardDetailView<${entity.className}> {

    @Override
    protected void findEntityId(BeforeEnterEvent event) {
        // Because DTO entity cannot be loaded by Id, we need to prevent Id parsing from route parameters
    }
}
