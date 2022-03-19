package ${packageName}

import com.haulmont.cuba.gui.screen.UiController
import com.haulmont.cuba.gui.screen.UiDescriptor
import com.haulmont.cuba.web.app.login.LoginScreen
<%if (classComment) {%>
${classComment}<%}%>

@UiController("${api.escapeKotlinDollar(screenId)}")
@UiDescriptor("${descriptorName}.xml")
class ${controllerName} : LoginScreen()