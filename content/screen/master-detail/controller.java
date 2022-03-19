package ${packageName};

import io.jmix.ui.screen.*;
import ${entity.fqn};

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${screenId}")
@UiDescriptor("${descriptorName}.xml")
@LookupComponent("${tableId}")
public class ${controllerName} extends MasterDetailScreen<${entity.className}> {
}