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
import io.jmix.flowui.view.Target
import ${repository.getQualifiedName()}
import io.jmix.core.repository.JmixDataRepositoryUtils.*
<%}%>
<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${detailRoute}/:${detailRouteParam}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController("${api.escapeKotlinDollar(detailId)}")
@ViewDescriptor("${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
class ${detailControllerName}(private val repository: ${repository.getName()}) : StandardDetailView<${entity.className}>() {<%if (useDataRepositories){%>


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
               // To save them, either add cascade in JPA annotation or pass to appropriate repository manually."""}
        %>return mutableSetOf(repository.save(editedEntity))
    }

    @Install(to = "${dlId}", target = Target.DATA_LOADER)
    private fun loadDelegate(context: LoadContext<${entity.className}>): ${entity.className} {
        return repository.getById(extractId(context), context.fetchPlan)
    }<%}%>
}