<%
        def pluralForm = api.pluralForm(entity.uncapitalizedClassName)
        def tableDl = entity.uncapitalizedClassName.equals(pluralForm) ? pluralForm + "CollectionDl" : pluralForm + "Dl"
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

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController(id = "${viewId}")
@ViewDescriptor(path = "${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "64em")
class ${viewControllerName}<%if (useDataRepositories){%>(private val repository: ${repository.getName()})<%}%> : StandardListView<${entity.className}>() {<%if (useDataRepositories){%>

    @Install(to = "${tableDl}", target = Target.DATA_LOADER, subject = "loadFromRepositoryDelegate")
    private fun loadDelegate(pageable: Pageable, context: JmixDataRepositoryContext): List<${entity.className}> {
        return repository.findAll(pageable, context).content
    }<%if (tableActions.contains("remove")) {%>

    @Install(to = "${tableId}.removeAction", subject = "delegate")
    private fun ${tableId}RemoveDelegate(collection: Collection<${entity.className}>) {
        repository.deleteAll(collection)
    }

    @Install(to = "pagination", subject = "totalCountByRepositoryDelegate")
    private fun paginationTotalCountByRepositoryDelegate(context: JmixDataRepositoryContext): Long {
        return repository.count(context)
    }<%}%><%}%>
}