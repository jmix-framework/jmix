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
package ${packageName};

import ${entity.fqn};
<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent;
<%} else {%>
import ${routeLayout.getControllerFqn()};
<%}%>
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
<%if (useDataRepositories){%>
import io.jmix.core.FetchPlan;

import ${repository.getQualifiedName()};

import java.util.Optional;
import ${getRepositoryIdFqn()};
<%}%>
<%if (useRepositorySaveDelegate || useUpdateServiceSaveDelegate){%>
import io.jmix.core.SaveContext;
import java.util.Set;
<%}%>
<%if (useDataRepositories || useUpdateServiceSaveDelegate){%>
import org.springframework.beans.factory.annotation.Autowired;
<%}%>
<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${detailRoute}/:${detailRouteParam}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController(id = "${detailId}")
@ViewDescriptor(path = "${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
public class ${detailControllerName} extends StandardDetailView<${entity.className}> {<%if (useDataRepositories){%>

    @Autowired
    private ${repository.getQualifiedName()} repository;
<%}%><%if (useUpdateServiceSaveDelegate){%>

    @Autowired
    private ${updateService.getQualifiedName()} updateService;
<%}%><%if (useDataRepositories){%>

    @Install(to = "${dlId}", target = Target.DATA_LOADER, subject = "loadFromRepositoryDelegate")
    private Optional<${entity.className}> loadDelegate(${getRepositoryIdClassName()} id, FetchPlan fetchPlan){
        return repository.findById(id, fetchPlan);
    }<%}%><%if (useRepositorySaveDelegate){%>

    @Install(target = Target.DATA_CONTEXT)
    private Set<Object> saveDelegate(SaveContext saveContext) {
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
        }%>return Set.of(repository.save(getEditedEntity()));
    }<%}%><%if (useUpdateServiceSaveDelegate){%>

    @Install(target = Target.DATA_CONTEXT)
    private Set<Object> saveDelegate(SaveContext saveContext) {
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
        }%>return Set.of(updateService.save(getEditedEntity(), saveContext));
    }<%}%>
}