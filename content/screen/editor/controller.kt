package ${packageName}

import io.jmix.ui.screen.*
import ${entity.fqn}

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${api.escapeKotlinDollar(editorId)}")
@UiDescriptor("${editorDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
class ${editorControllerName} : StandardEditor<${entity.className}>()