<%
    def dlId="${entity.uncapitalizedClassName}Dl"

    private String getRepositoryIdFqn() {
        try {
            return repository.getIdFqn()
        } catch(Exception e) {
            return "java.util.UUID"
        }
    }

    private String getRepositoryIdClassName() {
        try {
            return repository.getIdClassName()
        } catch(Exception e) {
            return "UUID"
        }
    }
%>
package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent<%} else {%>
import ${routeLayout.getControllerFqn()}<%}%>
import com.vaadin.flow.router.Route
import io.jmix.flowui.view.*
<%if (useDataRepositories){%>import io.jmix.core.repository.JmixDataRepositoryContext
import io.jmix.flowui.view.Target
import ${repository.getQualifiedName()}
import java.util.Optional
import ${getRepositoryIdFqn()}
<%}%>
<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${readRoute}/:${readRouteParam}/read", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController(id = "${api.escapeKotlinDollar(readId)}")
@ViewDescriptor(path = "${readDescriptorName}.xml")
@ReadEntityContainer("${dcId}")
class ${readControllerName}<%if (useDataRepositories){%>(private val repository: ${repository.getName()})<%}%> : StandardReadView<${entity.className}>() {<%if (useDataRepositories){%>

    @Install(to = "${dlId}", target = Target.DATA_LOADER, subject = "loadFromRepositoryDelegate")
    private fun loadDelegate(id: ${getRepositoryIdClassName()}, context: JmixDataRepositoryContext): Optional<${entity.className}> {
        return repository.findById(id, context)
    }<%}%>
}
