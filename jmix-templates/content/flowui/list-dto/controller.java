package ${packageName};

import ${entity.fqn};<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent;<%} else {%>
import ${module_basePackage}.view.main.MainView;
<%}%>import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.flowui.view.*;

import java.util.Collection;
import java.util.List;

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent.class <%} else {%>MainView.class<%}%>)
@ViewController("${viewId}")
@ViewDescriptor("${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "50em")
public class ${viewControllerName} extends StandardListView<${entity.className}> {

    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    protected List<${entity.className}> ${tableDl}LoadDelegate(LoadContext<${entity.className}> loadContext) {
        // Here you can load entities from an external storage
        return List.of();
    }

    @Install(to = "${tableId}.remove", subject = "delegate")
    private void ${tableId}RemoveDelegate(final Collection<${entity.className}> collection) {
        for (${entity.className} entity : collection) {
            // Here you can remove entities from an external storage
        }
    }
}
