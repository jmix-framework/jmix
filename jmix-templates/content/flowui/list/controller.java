<%
        def pluralForm = api.pluralForm(entity.uncapitalizedClassName)
        def tableDl = entity.uncapitalizedClassName.equals(pluralForm) ? pluralForm + "CollectionDl" : pluralForm + "Dl"
        def useUpdateServiceRemoveDelegate = useUpdateService && binding.hasVariable('updateService') && updateService != null && updateService.isRemoveDelegate() && tableActions.contains("remove")
        def useRepositoryRemoveDelegate = useDataRepositories && tableActions.contains("remove") && !useUpdateServiceRemoveDelegate
        %>
package ${packageName};

import ${entity.fqn};
<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent;
<%} else {%>
import ${routeLayout.getControllerFqn()};
<%}%>
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
<%if (useDataRepositories){%>import io.jmix.core.repository.JmixDataRepositoryContext;
import org.springframework.data.domain.Pageable;

import java.util.List;<%}%>
<%if (useDataRepositories || useUpdateServiceRemoveDelegate){%>
import org.springframework.beans.factory.annotation.Autowired;
<%}%>
<%if (useRepositoryRemoveDelegate || useUpdateServiceRemoveDelegate){%>
import java.util.Collection;
<%}%>

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController(id = "${viewId}")
@ViewDescriptor(path = "${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "64em")
public class ${viewControllerName} extends StandardListView<${entity.className}> {<%if (useDataRepositories){%>

    @Autowired
    private ${repository.getQualifiedName()} repository;
<%}%><%if (useUpdateServiceRemoveDelegate){%>

    @Autowired
    private ${updateService.getQualifiedName()} updateService;
<%}%><%if (useDataRepositories){%>

    @Install(to = "${tableDl}", target = Target.DATA_LOADER, subject = "loadFromRepositoryDelegate")
    private List<${entity.className}> loadDelegate(Pageable pageable, JmixDataRepositoryContext context){
        return repository.findAllSlice(pageable, context).getContent();
    }

    @Install(to = "pagination", subject = "totalCountByRepositoryDelegate")
    private Long paginationTotalCountByRepositoryDelegate(final JmixDataRepositoryContext context) {
        return repository.count(context);
    }<%}%><%if (useRepositoryRemoveDelegate) {%>

    @Install(to = "${tableId}.removeAction", subject = "delegate")
    private void ${tableId}RemoveDelegate(final Collection<${entity.className}> collection) {
        repository.deleteAll(collection);
    }<%}%>
<%if (useUpdateServiceRemoveDelegate){%>

    @Install(to = "${tableId}.removeAction", subject = "delegate")
    private void ${tableId}RemoveDelegate(final Collection<${entity.className}> collection) {
        collection.forEach(updateService::remove);
    }<%}%>
}