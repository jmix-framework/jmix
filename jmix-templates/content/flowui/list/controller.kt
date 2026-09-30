<%
        def pluralForm = api.pluralForm(entity.uncapitalizedClassName)
        def tableDl = entity.uncapitalizedClassName.equals(pluralForm) ? pluralForm + "CollectionDl" : pluralForm + "Dl"
        def useUpdateServiceRemoveDelegate = useUpdateService && binding.hasVariable('updateService') && updateService != null && updateService.isRemoveDelegate() && tableActions.contains("remove")
        def useRepositoryRemoveDelegate = useDataRepositories && tableActions.contains("remove") && !useUpdateServiceRemoveDelegate
        %>
package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent<%} else {%>
import ${routeLayout.getControllerFqn()}<%}%>
import com.vaadin.flow.router.Route
import io.jmix.flowui.view.*
<%if (useDataRepositories){%>import io.jmix.core.repository.JmixDataRepositoryContext
import io.jmix.flowui.view.Target
import org.springframework.data.domain.Pageable
import ${repository.getQualifiedName()}
<%}%>
<%if (useUpdateServiceRemoveDelegate){%>import ${updateService.getQualifiedName()}
<%}%>

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController(id = "${viewId}")
@ViewDescriptor(path = "${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "64em")
class ${viewControllerName}<%if (useDataRepositories || useUpdateServiceRemoveDelegate){%>(<%if (useDataRepositories){%>private val repository: ${repository.getName()}<%}%><%if (useDataRepositories && useUpdateServiceRemoveDelegate){%>, <%}%><%if (useUpdateServiceRemoveDelegate){%>private val updateService: ${updateService.getName()}<%}%>)<%}%> : StandardListView<${entity.className}>() {<%if (useDataRepositories){%>

    @Install(to = "${tableDl}", target = Target.DATA_LOADER, subject = "loadFromRepositoryDelegate")
    private fun loadDelegate(pageable: Pageable, context: JmixDataRepositoryContext): List<${entity.className}> {
        return repository.findAllSlice(pageable, context).content
    }

    @Install(to = "pagination", subject = "totalCountByRepositoryDelegate")
    private fun paginationTotalCountByRepositoryDelegate(context: JmixDataRepositoryContext): Long {
        return repository.count(context)
    }<%}%><%if (useRepositoryRemoveDelegate) {%>

    @Install(to = "${tableId}.removeAction", subject = "delegate")
    private fun ${tableId}RemoveDelegate(collection: Collection<${entity.className}>) {
        repository.deleteAll(collection)
    }<%}%>
<%if (useUpdateServiceRemoveDelegate){%>

    @Install(to = "${tableId}.removeAction", subject = "delegate")
    private fun ${tableId}RemoveDelegate(collection: Collection<${entity.className}>) {
        collection.forEach(updateService::remove)
    }<%}%>
}
