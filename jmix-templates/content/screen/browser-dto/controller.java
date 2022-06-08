package ${packageName};

import io.jmix.core.LoadContext;
import io.jmix.ui.screen.*;
import ${entity.fqn};

import java.util.Collections;
import java.util.List;

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${lookupId}")
@UiDescriptor("${lookupDescriptorName}.xml")
@LookupComponent("${tableId}")
public class ${lookupControllerName} extends StandardLookup<${entity.className}> {

    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    private List<${entity.className}> ${tableDl}LoadDelegate(LoadContext<${entity.className}> loadContext) {
        // Here you can load entities from an external store
        return Collections.emptyList();
    }
}