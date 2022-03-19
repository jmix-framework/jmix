package ${packageName}

import io.jmix.ui.screen.*
import ${entity.fqn}

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${screenId}")
@UiDescriptor("${descriptorName}.xml")
@LookupComponent("${tableId}")
class ${controllerName} extends MasterDetailScreen<${entity.className}> {
}