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
import io.jmix.core.repository.JmixDataRepositoryContext;

import ${repository.getQualifiedName()};

import java.util.Optional;
import ${getRepositoryIdFqn()};

import org.springframework.beans.factory.annotation.Autowired;
<%}%>
<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${readRoute}/:${readRouteParam}/read", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController(id = "${readId}")
@ViewDescriptor(path = "${readDescriptorName}.xml")
@ReadEntityContainer("${dcId}")
public class ${readControllerName} extends StandardReadView<${entity.className}> {<%if (useDataRepositories){%>

    @Autowired
    private ${repository.getQualifiedName()} repository;

    @Install(to = "${dlId}", target = Target.DATA_LOADER, subject = "loadFromRepositoryDelegate")
    private Optional<${entity.className}> loadDelegate(${getRepositoryIdClassName()} id, JmixDataRepositoryContext context){
        return repository.findById(id, context);
    }<%}%>
}
