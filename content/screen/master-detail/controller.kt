package ${packageName}

import io.jmix.ui.screen.*
import ${entity.fqn}

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${api.escapeKotlinDollar(screenId)}")
@UiDescriptor("${descriptorName}.xml")
@LookupComponent("${tableId}")
class ${controllerName} : MasterDetailScreen<${entity.className}>()