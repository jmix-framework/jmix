package ${packageName}

import io.jmix.ui.screen.ScreenFragment
import io.jmix.ui.screen.UiController
import io.jmix.ui.screen.UiDescriptor

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${api.escapeKotlinDollar(id)}")
@UiDescriptor("${descriptorName}.xml")
class ${controllerName} : ScreenFragment()