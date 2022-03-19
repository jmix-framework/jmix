package ${packageName}

import io.jmix.ui.screen.*
import ${entity.fqn}

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${lookupId}")
@UiDescriptor("${lookupDescriptorName}.xml")
@LookupComponent("${tableId}")
class ${lookupControllerName} : StandardLookup<${entity.className}>()