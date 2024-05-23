<%
        def dlId="${entity.uncapitalizedClassName}Dl"
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
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static io.jmix.core.repository.JmixDataRepositoryUtils.*;
<%}%>
<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${detailRoute}/:${detailRouteParam}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController("${detailId}")
@ViewDescriptor("${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
public class ${detailControllerName} extends StandardDetailView<${entity.className}> {<%if (useDataRepositories){%>

    @Autowired
    private ${repository.getQualifiedName()} repository;


    @Install(target = Target.DATA_CONTEXT)
    private Set<Object> saveDelegate(SaveContext saveContext) {
        <%
    def compositeAttrs = ''
    detailFetchPlan.orderedRootProperties.each {property ->
            def propAttr = detailFetchPlan.entity.getAttribute(property.name)
            if (propAttr != null && propAttr.hasAnnotation('Composition')) {
                compositeAttrs = compositeAttrs + property.name + ', '
            }
    }
    if (compositeAttrs.length() > 0){
        compositeAttrs = compositeAttrs.substring(0, compositeAttrs.length() - 2);
        print """/* 
                    * ${entity.className} has next @Composition attributes: $compositeAttrs.
                    * Changes for corresponding entities have to be saved here.
                    * Please make sure that these attributes have cascade saving enabled or save them manually.
                    * Entities with unsaved changes are provided in 'saveContext' method parameter.
                    */"""}%>
        return Set.of(repository.save(getEditedEntity()));
    }

    @Install(to = "${dlId}", target = Target.DATA_LOADER)
    private ${entity.className} loadDelegate(LoadContext<${entity.className}> context){
        return repository.getById(extractId(context), context.getFetchPlan());
    }<%}%>
}