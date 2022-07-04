package ${packageName};

import ${entity.fqn};
import ${module_basePackage}.view.main.MainView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import io.jmix.flowui.view.*;

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = ":id", layout = MainView.class)
@RoutePrefix("${routePrefix}")
@UiController("${detailId}")
@UiDescriptor("${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
public class ${detailControllerName} extends StandardDetailView<${entity.className}> {
}