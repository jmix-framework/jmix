<%
        def isDataGridTable = tableType.getXmlName().equals("dataGrid")
        def pluralForm = api.pluralForm(entity.uncapitalizedClassName)
        def tableDl = entity.uncapitalizedClassName.equals(pluralForm) ? pluralForm + "CollectionDl" : pluralForm + "Dl"
        %>
package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent
<%} else {%>
import ${routeLayout.getControllerFqn()}
<%}%>import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.HasValueAndElement
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import io.jmix.flowui.component.validation.ValidationErrors
import io.jmix.core.validation.group.UiCrossFieldChecks
import io.jmix.flowui.action.SecuredBaseAction
import io.jmix.flowui.component.UiComponentUtils
import <%if (isDataGridTable) {%> io.jmix.flowui.component.grid.DataGrid <%} else {%> io.jmix.flowui.component.grid.TreeDataGrid <%}%>
import io.jmix.flowui.kit.action.Action
import io.jmix.flowui.kit.action.ActionPerformedEvent
import io.jmix.flowui.kit.component.button.JmixButton
import io.jmix.flowui.model.*
import io.jmix.flowui.view.*
import io.jmix.flowui.view.Target
<%if (useDataRepositories){%>import io.jmix.core.LoadContext
import io.jmix.core.SaveContext
import ${repository.getQualifiedName()}
import io.jmix.core.repository.JmixDataRepositoryUtils.*<%}%>
<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController(id = "${viewId}")
@ViewDescriptor(path = "${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "64em")
class ${viewControllerName}<%if (useDataRepositories){%>(private val repository: ${repository.getName()})<%}%> : StandardListView<${entity.className}>() {

    @ViewComponent
    private lateinit var dataContext: DataContext

    @ViewComponent
    private lateinit var ${tableDc}: CollectionContainer<${entity.className}>

    @ViewComponent
    private lateinit var ${detailDc}: InstanceContainer<${entity.className}>

    @ViewComponent
    private lateinit var ${detailDl}: InstanceLoader<${entity.className}>

    @ViewComponent
    private lateinit var listLayout: VerticalLayout

    @ViewComponent
    private lateinit var ${tableId}: <%if (isDataGridTable) {%> DataGrid<%} else {%> TreeDataGrid<%}%><${entity.className}>

    @ViewComponent
    private lateinit var form: FormLayout

    @ViewComponent
    private lateinit var detailActions: HorizontalLayout

    @Subscribe
    fun onInit(event: InitEvent) {
        ${tableId}.getActions().forEach { action ->
            if (action is SecuredBaseAction) {
                action.addEnabledRule { listLayout.isEnabled }
            }
        }
    }

    @Subscribe
    fun onBeforeShow(event: BeforeShowEvent) {
        updateControls(false)
    }<%if (tableActions.contains("create")) {%>

    @Subscribe("${tableId}.createAction")
    fun on${tableId.capitalize()}CreateAction(event: ActionPerformedEvent) {
        dataContext.clear()
        val entity: ${entity.className} = dataContext.create(${entity.className}::class.java)
        ${detailDc}.item = entity
        updateControls(true)
    }<%}%><%if (tableActions.contains("edit")) {%>

    @Subscribe("${tableId}.editAction")
    fun on${tableId.capitalize()}EditAction(event: ActionPerformedEvent) {
        updateControls(true)
    }<%}%>

    @Subscribe("saveButton")
    fun onSaveButtonClick(event: ClickEvent<JmixButton>) {
        val item = ${detailDc}.item
        val validationErrors = validateView(item)
        if (!validationErrors.isEmpty) {
            val viewValidation = getViewValidation()
            viewValidation.showValidationErrors(validationErrors)
            viewValidation.focusProblemComponent(validationErrors)
            return
        }
        dataContext.save()
        ${tableDc}.replaceItem(item)
        updateControls(false)
    }

    @Subscribe("cancelButton")
    fun onCancelButtonClick(event: ClickEvent<JmixButton>) {
        dataContext.clear()
        ${detailDc}.setItem(null)
        ${detailDl}.load()
        updateControls(false)
    }

    @Subscribe(id = "${tableDc}", target = Target.DATA_CONTAINER)
    fun on${tableDc.capitalize()}ItemChange(event: InstanceContainer.ItemChangeEvent<${entity.className}>) {
        val entity: ${entity.className}? = event.item
        dataContext.clear()
        if (entity != null) {
            ${detailDl}.entityId = entity.id
            ${detailDl}.load()
        } else {
            ${detailDl}.entityId = null
            ${detailDc}.setItem(null)
        }
        updateControls(false)
    }

    private fun validateView(entity: ${entity.className}): ValidationErrors {
        val viewValidation = getViewValidation()
        val validationErrors = viewValidation.validateUiComponents(form)
        if (!validationErrors.isEmpty) {
            return validationErrors
        }
        validationErrors.addAll(viewValidation.validateBeanGroup(UiCrossFieldChecks::class.java, entity))
        return validationErrors
    }

    private fun updateControls(editing: Boolean) {
        UiComponentUtils.getComponents(form).forEach { component ->
            if (component is HasValueAndElement<*, *>) {
                component.isReadOnly = !editing
            }
        }
        detailActions.isVisible = editing
        listLayout.isEnabled = !editing
        ${tableId}.getActions().forEach(Action::refreshState);
    }

    private fun getViewValidation(): ViewValidation {
        return applicationContext.getBean(ViewValidation::class.java)
    }<%if (useDataRepositories){%>

    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    private fun listLoadDelegate(context: LoadContext<${entity.className}>): List<${entity.className}> {
        return repository.findAll(buildPageRequest(context), buildRepositoryContext(context)).content
    }<%if (tableActions.contains("remove")) {%>

    @Install(to = "${tableId}.remove", subject = "delegate")
    private fun ${tableId}RemoveDelegate(collection: Collection<${entity.className}>) {
        repository.deleteAll(collection)
    }<%}%>

    @Install(target = Target.DATA_CONTEXT)
    private fun saveDelegate(saveContext: SaveContext): Set<Any> {
        <%def compositeAttrs = ''
          detailFetchPlan.orderedRootProperties.each {property ->
              def propAttr = detailFetchPlan.entity.getAttribute(property.name)
              if (propAttr != null && propAttr.hasAnnotation('Composition')) {
                  compositeAttrs = compositeAttrs + property.name + ', '
              }
          }
          if (compositeAttrs.length() > 0){
            compositeAttrs = compositeAttrs.substring(0, compositeAttrs.length() - 2);
            println """// ${entity.className} has the following @Composition attributes: $compositeAttrs.
             // To save them, either add cascade in JPA annotation or pass to appropriate repository manually."""}
        %>return mutableSetOf(repository.save(${detailDc}.item))
    }

    @Install(to = "${detailDl}", target = Target.DATA_LOADER)
    private fun detailLoadDelegate(context: LoadContext<${entity.className}>): ${entity.className} {
        return repository.getById(extractEntityId(context), context.fetchPlan)
    }<%}%>
}