package ${packageName};

import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${id}")
@UiDescriptor("${descriptorName}.xml")
public class ${controllerName} extends ScreenFragment {
}