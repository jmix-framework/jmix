package ${packageName};

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import io.jmix.flowui.screen.EditedEntityContainer;
import io.jmix.flowui.screen.StandardEditor;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;
import ${module_basePackage}.view.main.MainView;
import ${entity.fqn};

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = ":id", layout = MainView.class)
@RoutePrefix("${route}")
@UiController("${detailId}")
@UiDescriptor("${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
public class ${detailControllerName} extends StandardEditor<${entity.className}> {
}