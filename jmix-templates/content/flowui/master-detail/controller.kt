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
import com.vaadin.flow.component.HasValidation
import com.vaadin.flow.component.HasValueAndElement
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeLeaveEvent
import com.vaadin.flow.router.Route
import io.jmix.core.AccessManager
import io.jmix.core.EntityStates
import io.jmix.flowui.component.validation.ValidationErrors
import io.jmix.core.validation.group.UiCrossFieldChecks
import io.jmix.flowui.UiComponentProperties
import io.jmix.flowui.UiViewProperties
import io.jmix.flowui.accesscontext.UiEntityAttributeContext
import io.jmix.flowui.action.SecuredBaseAction
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.delegate.AbstractFieldDelegate.PROPERTY_INVALID
import <%if (isDataGridTable) {%> io.jmix.flowui.component.grid.DataGrid <%} else {%> io.jmix.flowui.component.grid.TreeDataGrid <%}%>
import io.jmix.flowui.data.EntityValueSource
import io.jmix.flowui.data.SupportsValueSource
import io.jmix.flowui.kit.action.Action
import io.jmix.flowui.kit.action.ActionPerformedEvent
import io.jmix.flowui.kit.component.button.JmixButton
import io.jmix.flowui.model.*
import io.jmix.flowui.util.OperationResult
import io.jmix.flowui.util.UnknownOperationResult
import io.jmix.flowui.view.*
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    private lateinit var accessManager: AccessManager

    @Autowired
    private lateinit var entityStates: EntityStates

    @Autowired
    private lateinit var uiViewProperties: UiViewProperties

    @Autowired
    private lateinit var viewValidation: ViewValidation

    @Autowired
    private lateinit var uiComponentProperties: UiComponentProperties

    private var modifiedAfterEdit: Boolean = false

    @Subscribe
    fun onInit(event: InitEvent) {
        ${tableId}.getActions().forEach { action ->
            if (action is SecuredBaseAction) {
                action.addEnabledRule { listLayout.isEnabled }
            }
        }
    }

    @Subscribe
    fun onReady(event: ReadyEvent) {
        setupModifiedTracking()
    }

    @Subscribe
    fun onBeforeShow(event: BeforeShowEvent) {
        updateControls(false)
    }

    @Subscribe
    fun onBeforeClose(event: BeforeCloseEvent) {
        preventUnsavedChanges(event)
    }<%if (tableActions.contains("create")) {%>

    @Subscribe("${tableId}.createAction")
    fun on${tableId.capitalize()}CreateAction(event: ActionPerformedEvent) {
        prepareFormForValidation()

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
        saveEditedEntity()
    }

    @Subscribe("cancelButton")
    fun onCancelButtonClick(event: ClickEvent<JmixButton>) {
        if (!hasUnsavedChanges()) {
            discardEditedEntity()
            return
        }

        if (uiViewProperties.isUseSaveConfirmation) {
            viewValidation.showSaveConfirmationDialog(this)
                .onSave(this::saveEditedEntity)
                .onDiscard(this::discardEditedEntity)
        } else {
            viewValidation.showUnsavedChangesDialog(this)
                .onDiscard(this::discardEditedEntity)
        }
    }

    @Subscribe(id = "${tableDc}", target = Target.DATA_CONTAINER)
    fun on${tableDc.capitalize()}ItemChange(event: InstanceContainer.ItemChangeEvent<${entity.className}>) {
        prepareFormForValidation()

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

    private fun prepareFormForValidation() {
        // all components shouldn't be readonly due to validation passing correctly
        UiComponentUtils.getComponents(form)
            .forEach {
                if (it is HasValueAndElement<*, *>) {
                    it.isReadOnly = false
                }
            }
    }

    private fun saveEditedEntity(): OperationResult {
        val item = ${detailDc}.item
        val validationErrors = validateView(item)

        if (!validationErrors.isEmpty) {
            viewValidation.showValidationErrors(validationErrors)
            viewValidation.focusProblemComponent(validationErrors)
            return OperationResult.fail()
        }

        dataContext.save()
        ${tableDc}.replaceItem(item)
        updateControls(false)
        return OperationResult.success()
    }

    private fun discardEditedEntity() {
        resetFormInvalidState()

        dataContext.clear()
        ${detailDc}.setItem(null)
        ${detailDl}.load()
        updateControls(false)
    }

    private fun resetFormInvalidState() {
        UiComponentUtils.getComponents(form)
            .filter { it is HasValidation && it.isInvalid }
            .forEach {
                with(it.getElement()) {
                    setProperty(PROPERTY_INVALID, false)
                    executeJs("this.invalid = \$0", false)
                }
            }
    }

    private fun validateView(entity: ${entity.className}): ValidationErrors {
        val validationErrors = viewValidation.validateUiComponents(form)
        if (!validationErrors.isEmpty) {
            return validationErrors
        }
        validationErrors.addAll(viewValidation.validateBeanGroup(UiCrossFieldChecks::class.java, entity))
        return validationErrors
    }

    private fun updateControls(editing: Boolean) {
        UiComponentUtils.getComponents(form).forEach { component ->
            if (component is SupportsValueSource<*>
                && component.valueSource is EntityValueSource<*, *>
                && component is HasValueAndElement<*, *>
            ) {
                component.isReadOnly = !editing || !isUpdatePermitted(component.valueSource as EntityValueSource<*, *>)
            }
        }

        modifiedAfterEdit = false
        detailActions.isVisible = editing
        listLayout.isEnabled = !editing
        ${tableId}.actions.forEach(Action::refreshState)

        if (!uiComponentProperties.isImmediateRequiredValidationEnabled && editing) {
            resetFormInvalidState()
        }
    }

    private fun isUpdatePermitted(valueSource: EntityValueSource<*, *>): Boolean {
        return UiEntityAttributeContext(valueSource.metaPropertyPath)
            .also { accessManager.applyRegisteredConstraints(it) }
            .canModify()
    }

    private fun hasUnsavedChanges(): Boolean {
        for (modified in dataContext.modified) {
            if (!entityStates.isNew(modified)) {
                return true
            }
        }

        return modifiedAfterEdit
    }

    private fun setupModifiedTracking() {
        dataContext.addChangeListener { modifiedAfterEdit = true }
        dataContext.addPostSaveListener { modifiedAfterEdit = false }
    }

    private fun preventUnsavedChanges(event: BeforeCloseEvent) {
        val closeAction = event.closeAction

        if (closeAction is ChangeTrackerCloseAction
            && closeAction.isCheckForUnsavedChanges
            && hasUnsavedChanges()
        ) {
            val result = UnknownOperationResult()

            if (closeAction is NavigateCloseAction) {
                val beforeLeaveEvent = closeAction.beforeLeaveEvent
                val navigationAction = beforeLeaveEvent.postpone()

                if (uiViewProperties.isUseSaveConfirmation) {
                    viewValidation.showSaveConfirmationDialog(this)
                        .onSave { result.resume(navigateWithSave(navigationAction)) }
                        .onDiscard { result.resume(navigateWithDiscard(navigationAction)) }
                        .onCancel {
                            result.otherwise { cancelNavigation(navigationAction) }
                            result.fail()
                        }
                } else {
                    viewValidation.showUnsavedChangesDialog(this)
                        .onDiscard { result.resume(navigateWithDiscard(navigationAction)) }
                        .onCancel {
                            result.otherwise { cancelNavigation(navigationAction) }
                            result.fail()
                        }
                }
            } else {
                if (uiViewProperties.isUseSaveConfirmation) {
                    viewValidation.showSaveConfirmationDialog(this)
                        .onSave { result.resume(closeWithSave()) }
                        .onDiscard { result.resume(closeWithDiscard()) }
                        .onCancel(result::fail)
                } else {
                    viewValidation.showUnsavedChangesDialog(this)
                        .onDiscard { result.resume(closeWithDiscard()) }
                        .onCancel(result::fail)
                }
            }

            event.preventClose(result)
        }
    }

    private fun navigateWithDiscard(navigationAction: BeforeLeaveEvent.ContinueNavigationAction): OperationResult {
        return navigate(navigationAction, StandardOutcome.DISCARD.closeAction)
    }

    private fun navigateWithSave(navigationAction: BeforeLeaveEvent.ContinueNavigationAction): OperationResult {
        return saveEditedEntity()
            .compose { navigate(navigationAction, StandardOutcome.SAVE.closeAction) }
    }

    private fun cancelNavigation(navigationAction: BeforeLeaveEvent.ContinueNavigationAction) {
        // Because of using React Router, we need to call
        // 'BeforeLeaveEvent.ContinueNavigationAction.cancel'
        // explicitly, otherwise navigation process hangs
        navigationAction.cancel()
    }

    private fun navigate(
        navigationAction: BeforeLeaveEvent.ContinueNavigationAction,
        closeAction: CloseAction
    ): OperationResult {
        navigationAction.proceed()

        val afterCloseEvent = AfterCloseEvent(this, closeAction)
        fireEvent(afterCloseEvent)

        return OperationResult.success()
    }

    private fun closeWithSave(): OperationResult {
        return saveEditedEntity()
            .compose { close(StandardOutcome.SAVE) }
    }<%if (useDataRepositories){%>

    @Install(to = "${tableDl}", target = Target.DATA_LOADER)
    private fun listLoadDelegate(context: LoadContext<${entity.className}>): List<${entity.className}> {
        return repository.findAll(buildPageRequest(context), buildRepositoryContext(context)).content
    }<%if (tableActions.contains("remove")) {%>

    @Install(to = "${tableId}.removeAction", subject = "delegate")
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