package ${packageName};

import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.web.app.main.MainScreen;
<%if (classComment) {%>
${classComment}<%}%>

@UiController("${id}")
@UiDescriptor("${descriptorName}.xml")
public class ${controllerName} extends MainScreen {
}