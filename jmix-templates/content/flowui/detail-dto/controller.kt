package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent<%} else {%>
import ${routeLayout.getControllerFqn()}<%}%>
import com.vaadin.flow.router.Route
import io.jmix.core.Copier
import io.jmix.core.EntityStates
import io.jmix.core.LoadContext
import io.jmix.core.SaveContext
import io.jmix.core.entity.EntityValues
import io.jmix.flowui.view.Target
import io.jmix.flowui.view.*
import org.springframework.beans.factory.annotation.Autowired

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${detailRoute}/:${detailRouteParam}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController(id = "${api.escapeKotlinDollar(detailId)}")
@ViewDescriptor(path = "${detailDescriptorName}.xml")
@EditedEntityContainer("${dcId}")
class ${detailControllerName} : StandardDetailView<${entity.className}>() {
<%if (generateDelegates) {%>
    @Autowired
    private lateinit var copier: Copier
    @Autowired
    private lateinit var entityStates: EntityStates

    @Install(to = "${dlId}", target = Target.DATA_LOADER)
    private fun ${dlId}LoadDelegate(loadContext: LoadContext<${entity.className}>): ${entity.className}? {
        val id = loadContext.id
        // Here you can load the entity by id from an external storage.
        // Set the loaded entity to the not-new state using EntityStates.setNew(entity, false).
        return null
    }

    @Install(target = Target.DATA_CONTEXT)
    private fun saveDelegate(saveContext: SaveContext): MutableSet<Any> {
        val entity = editedEntity
        // Make a copy and save it. Copying isolates the view from possible changes of the saved entity.
        val saved = save(copier.copy(entity))
        // If the new entity ID is assigned by the storage, set the ID to the original entity instance
        // to let the framework match the saved instance with the original one.
        if (EntityValues.getId(entity) == null) {
            EntityValues.setId(entity, EntityValues.getId(saved))
        }
        // Set the returned entity to the not-new state.
        entityStates.setNew(saved, false)
        return mutableSetOf(saved)
    }

    private fun save(entity: ${entity.className}): ${entity.className} {
        // Here you can save the entity to an external storage and return the saved instance.
        return entity
    }<%}%>
}
