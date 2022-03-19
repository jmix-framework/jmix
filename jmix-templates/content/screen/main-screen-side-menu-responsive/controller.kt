package ${packageName}

import com.haulmont.cuba.gui.screen.UiController
import com.haulmont.cuba.gui.screen.UiDescriptor
import com.haulmont.cuba.web.app.main.MainScreen
<%if (classComment) {%>
${classComment}<%}%>

@UiController("${api.escapeKotlinDollar(id)}")
@UiDescriptor("${descriptorName}.xml")
class ${controllerName} : MainScreen()