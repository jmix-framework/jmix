<%
    def dlId="${entity.uncapitalizedClassName}Dl"
    def useUpdateServiceSaveDelegate = useUpdateService && binding.hasVariable('updateService') && updateService != null && updateService.isSaveDelegate()
    def useRepositorySaveDelegate = useDataRepositories && !useUpdateServiceSaveDelegate

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
<%if (useDataRepositories){%>import io.jmix.core.LoadContext
import io.jmix.core.FetchPlan
import io.jmix.flowui.view.Target
import ${repository.getQualifiedName()}
import java.util.Optional
import ${getRepositoryIdFqn()}
<%}%>
<%if (useRepositorySaveDelegate || useUpdateServiceSaveDelegate){%>import io.jmix.core.SaveContext
<%}%>
<%if (useUpdateServiceSaveDelegate){%>import ${updateService.getQualifiedName()}
<%}%>
<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${detailRoute}/:${detailRouteParam}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController(id = "${api.escapeKotlinDollar(detailId)}")
@ViewDescriptor(path = "${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
class ${detailControllerName}<%if (useDataRepositories || useUpdateServiceSaveDelegate){%>(<%if (useDataRepositories){%>private val repository: ${repository.getName()}<%}%><%if (useDataRepositories && useUpdateServiceSaveDelegate){%>, <%}%><%if (useUpdateServiceSaveDelegate){%>private val updateService: ${updateService.getName()}<%}%>)<%}%> : StandardDetailView<${entity.className}>() {<%if (useDataRepositories){%>

    @Install(to = "${dlId}", target = Target.DATA_LOADER, subject = "loadFromRepositoryDelegate")
    private fun loadDelegate(id: ${getRepositoryIdClassName()}, fetchPlan: FetchPlan): Optional<${entity.className}> {
        return repository.findById(id, fetchPlan)
    }<%}%><%if (useRepositorySaveDelegate){%>

    @Install(target = Target.DATA_CONTEXT)
    private fun saveDelegate(saveContext: SaveContext): Set<Any> {
        <%
        def compositeAttrs = []
        detailFetchPlan.orderedRootProperties.each { property ->
            def propAttr = detailFetchPlan.entity.getAttribute(property.name)
            if (propAttr != null && propAttr.hasAnnotation('Composition')) {
                    compositeAttrs << property.name
            }
        }
        if (!compositeAttrs.isEmpty()) {
            out.println("        // ${entity.className} has the following @Composition attributes: ${compositeAttrs.join(', ')}.")
            out.println("        // Make sure they have CascadeType.ALL in @OneToMany annotation.")
        }%>return mutableSetOf(repository.save(editedEntity))
    }<%}%><%if (useUpdateServiceSaveDelegate){%>

    @Install(target = Target.DATA_CONTEXT)
    private fun saveDelegate(saveContext: SaveContext): Set<Any> {
        <%
        def compositeAttrs = []
        detailFetchPlan.orderedRootProperties.each { property ->
            def propAttr = detailFetchPlan.entity.getAttribute(property.name)
            if (propAttr != null && propAttr.hasAnnotation('Composition')) {
                    compositeAttrs << property.name
            }
        }
        if (!compositeAttrs.isEmpty()) {
            out.println("        // ${entity.className} has the following @Composition attributes: ${compositeAttrs.join(', ')}.")
            out.println("        // Make sure they have CascadeType.ALL in @OneToMany annotation.")
        }%>return mutableSetOf(updateService.save(editedEntity, saveContext))
    }<%}%>
}
