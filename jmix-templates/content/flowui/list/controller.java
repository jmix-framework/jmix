package ${packageName};

import com.vaadin.flow.router.Route;
import io.jmix.flowui.screen.LookupComponent;
import io.jmix.flowui.screen.StandardLookup;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;
import ${module_basePackage}.view.main.MainView;
import ${entity.fqn};

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${route}", layout = MainView.class)
@UiController("${viewId}")
@UiDescriptor("${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
public class ${viewControllerName} extends StandardLookup<${entity.className}> {
}