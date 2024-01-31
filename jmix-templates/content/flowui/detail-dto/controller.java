package ${packageName};

import ${entity.fqn};<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent;<%} else {%>
import ${module_basePackage}.view.main.MainView;
<%}%>import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.flowui.view.*;

import java.util.Set;

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${detailRoute}/:${detailRouteParam}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent.class <%} else {%>MainView.class<%}%>)
@ViewController("${detailId}")
@ViewDescriptor("${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
public class ${detailControllerName} extends StandardDetailView<${entity.className}> {

    @Install(to = "${dlId}", target = Target.DATA_LOADER)
    private ${entity.className} customerDlLoadDelegate(final LoadContext<${entity.className}> loadContext) {
        Object id = loadContext.getId();
        // Here you can load the entity by id from an external storage
        return null;
    }

    @Install(target = Target.DATA_CONTEXT)
    private Set<Object> saveDelegate(final SaveContext saveContext) {
        ${entity.className} entity = getEditedEntity();
        // Here you can save the entity to an external storage and return the saved instance
        ${entity.className} saved = entity;
        return Set.of(saved);
    }
}
