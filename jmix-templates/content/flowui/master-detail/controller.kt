package ${packageName}

import ${entity.fqn}<%if (!api.jmixProjectModule.isApplication()) {%>
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
import io.jmix.flowui.component.validation.group.UiCrossFieldChecks
import io.jmix.flowui.kit.action.ActionPerformedEvent
import io.jmix.flowui.kit.component.button.JmixButton
import io.jmix.flowui.model.*
import io.jmix.flowui.view.*
import io.jmix.flowui.view.Target

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${listRoute}", layout = <%if (!api.jmixProjectModule.isApplication()) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController("${viewId}")
@ViewDescriptor("${viewDescriptorName}.xml")
@LookupComponent("${tableId}")
@DialogMode(width = "64em")
class ${viewControllerName} : StandardListView<${entity.className}>() {

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
    private lateinit var form: FormLayout

    @ViewComponent
    private lateinit var detailActions: HorizontalLayout

    @Subscribe
    fun onInit(event: InitEvent) {
        updateControls(false)
    }

    @Subscribe("${tableId}.create")
    fun on${tableId.capitalize()}Create(event: ActionPerformedEvent) {
        dataContext.clear()
        val entity: ${entity.className} = dataContext.create(${entity.className}::class.java)
        ${detailDc}.item = entity
        updateControls(true)
    }

    @Subscribe("${tableId}.edit")
    fun on${tableId.capitalize()}Edit(event: ActionPerformedEvent) {
        updateControls(true)
    }

    @Subscribe("saveBtn")
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

    @Subscribe("cancelBtn")
    fun onCancelButtonClick(event: ClickEvent<JmixButton>) {
        dataContext.clear()
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
        form.children.forEach { component ->
            if (component is HasValueAndElement<*, *>) {
                component.isReadOnly = !editing
            }
        }
        detailActions.isVisible = editing
        listLayout.isEnabled = !editing
    }

    private fun getViewValidation(): ViewValidation {
        return applicationContext.getBean(ViewValidation::class.java)
    }
}