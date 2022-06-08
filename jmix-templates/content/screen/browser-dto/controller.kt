package ${packageName}

import io.jmix.core.LoadContext
import io.jmix.ui.screen.*
import io.jmix.ui.screen.Target
import ${entity.fqn}

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${lookupId}")
@UiDescriptor("${lookupDescriptorName}.xml")
@LookupComponent("${tableId}")
class ${lookupControllerName} : StandardLookup<${entity.className}>() {

    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    private fun ${tableDl}LoadDelegate(loadContext: LoadContext<${entity.className}>?): MutableList<${entity.className}> {
        // Here you can load entities from an external store
        return mutableListOf()
    }
}