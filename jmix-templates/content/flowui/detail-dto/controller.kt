package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication()) {%>
import io.jmix.flowui.view.DefaultMainViewParent<%} else {%>
import ${module_basePackage}.view.main.MainView
<%}%>import com.vaadin.flow.router.Route
import io.jmix.core.LoadContext
import io.jmix.core.SaveContext
import io.jmix.flowui.view.*
import io.jmix.flowui.view.Target

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${detailRoute}/:id", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent::class <%} else {%>MainView::class<%}%>)
@ViewController("${detailId}")
@ViewDescriptor("${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
class ${detailControllerName} : StandardDetailView<${entity.className}>() {

    @Install(to = "${dlId}", target = Target.DATA_LOADER)
    private fun ${dlId}LoadDelegate(loadContext: LoadContext<${entity.className}>): ${entity.className}? {
        val id = loadContext.id
        // Here you can load the entity by id from an external storage.
        // Set the loaded entity to the not-new state using EntityStates.setNew(entity, false).
        return null
    }

    @Install(target = Target.DATA_CONTEXT)
    private fun saveDelegate(saveContext: SaveContext): MutableSet<Any> {
        // Here you can save the entity to an external storage and return the saved instance.
        // Set the returned entity to the not-new state using EntityStates.setNew(entity, false).
        // If the new entity ID is assigned by the storage, set the ID to the original instance too 
        // to let the framework match the saved instance with the original one.
        val savedEntity = editedEntity
        return mutableSetOf(savedEntity)
    }
}
