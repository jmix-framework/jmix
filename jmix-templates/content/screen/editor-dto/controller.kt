package ${packageName}

import io.jmix.core.LoadContext
import io.jmix.core.SaveContext
import io.jmix.ui.screen.*
import io.jmix.ui.screen.Target
import ${entity.fqn}

<%if (classComment) {%>
${classComment}
<%}%>@UiController("${editorId}")
@UiDescriptor("${editorDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
class ${editorControllerName} : StandardEditor<${entity.className}>() {

    @Install(to = "${dlId}", target = Target.DATA_LOADER)
    private fun ${dlId}LoadDelegate(loadContext: LoadContext<${entity.className}>?): ${entity.className} {
        // Here you can load entity from an external store by ID passed in LoadContext
        return editedEntity
    }

    @Install(target = Target.DATA_CONTEXT)
    private fun commitDelegate(saveContext: SaveContext?): MutableSet<Any> {
        // Here you can save the edited entity or the whole SaveContext in an external store.
        // Return the set of saved instances.
        return mutableSetOf(editedEntity)
    }
}