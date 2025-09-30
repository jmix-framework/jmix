<%
        def dlId="${entity.uncapitalizedClassName}Dl"
        %>
package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent<%} else {%>
import ${routeLayout.getControllerFqn()}<%}%>
import com.vaadin.flow.router.Route
import io.jmix.flowui.view.*
<%if (useDataRepositories){%>import io.jmix.core.LoadContext
import io.jmix.core.SaveContext
import io.jmix.core.FetchPlan
import io.jmix.flowui.view.Target
import ${repository.getQualifiedName()}
import java.util.Optional
import java.util.UUID
<%}%>
<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${detailRoute}/:${detailRouteParam}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController(id = "${api.escapeKotlinDollar(detailId)}")
@ViewDescriptor(path = "${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
class ${detailControllerName}<%if (useDataRepositories){%>(private val repository: ${repository.getName()})<%}%> : StandardDetailView<${entity.className}>() {<%if (useDataRepositories){%>

    @Install(to = "${dlId}", target = Target.DATA_LOADER, subject = "loadFromRepositoryDelegate")
    private fun loadDelegate(id: UUID, fetchPlan: FetchPlan): Optional<${entity.className}> {
        return repository.findById(id, fetchPlan)
    }

    @Install(target = Target.DATA_CONTEXT)
    private fun saveDelegate(saveContext: SaveContext): Set<Any> {
        <%def compositeAttrs = ''
          detailFetchPlan.orderedRootProperties.each {property ->
              def propAttr = detailFetchPlan.entity.getAttribute(property.name)
              if (propAttr != null && propAttr.hasAnnotation('Composition')) {
                  compositeAttrs = compositeAttrs + property.name + ', '
              }
          }
          if (compositeAttrs.length() > 0){
              compositeAttrs = compositeAttrs.substring(0, compositeAttrs.length() - 2);
              println """// ${entity.className} has the following @Composition attributes: $compositeAttrs.
               // Make sure they have CascadeType.ALL in @OneToMany annotation."""}
        %>return mutableSetOf(repository.save(editedEntity))
    }<%}%>
}