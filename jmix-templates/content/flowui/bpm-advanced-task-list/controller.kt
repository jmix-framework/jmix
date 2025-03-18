package ${packageName}

<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent;
<%} else {%>
import ${routeLayout.getControllerFqn()};
<%}%>
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.html.H5
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.router.Route
import io.jmix.bpm.entity.ProcessDefinitionData
import io.jmix.bpm.entity.TaskData
import io.jmix.bpm.entity.UserGroup
import io.jmix.bpm.multitenancy.BpmTenantProvider
import io.jmix.bpm.service.UserGroupService
import io.jmix.bpm.util.FlowableEntitiesConverter
import io.jmix.bpmflowui.event.TaskCompletedUiEvent
import io.jmix.bpmflowui.processform.ProcessFormViews
import io.jmix.core.DataLoadContext
import io.jmix.core.LoadContext
import io.jmix.core.Sort
import io.jmix.core.metamodel.datatype.EnumClass
import io.jmix.core.usersubstitution.CurrentUserSubstitution
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.details.JmixDetails
import io.jmix.flowui.component.formlayout.JmixFormLayout
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.kit.action.ActionPerformedEvent
import io.jmix.flowui.kit.component.button.JmixButton
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.CollectionLoader
import io.jmix.flowui.view.*
import io.jmix.flowui.view.Target
import org.flowable.engine.RepositoryService
import org.flowable.engine.TaskService
import org.flowable.engine.repository.ProcessDefinition
import org.flowable.task.api.Task
import org.flowable.task.api.TaskQuery
import org.flowable.task.service.impl.TaskQueryProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${route}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent::class <%} else {%>${routeLayout.getControllerClassName()}::class<%}%>)
@ViewController(id = "${viewId}")
@ViewDescriptor(path = "${descriptorName}.xml")
class ${controllerName} : StandardListView<TaskData>() {
    companion object {
        const val CREATE_TIME_PROPERTY: String = "createTime"
        const val NAME_PROPERTY: String = "name"
        const val DUE_DATE_PROPERTY: String = "dueDate"
    }

    @ViewComponent
    private lateinit var messageBundle: MessageBundle

    @ViewComponent
    private lateinit var tasksDl: CollectionLoader<TaskData>

    @ViewComponent
    private lateinit var tasksDc: CollectionContainer<TaskData>

    @ViewComponent
    private lateinit var processDefinitionsDc: CollectionContainer<ProcessDefinitionData>

    @ViewComponent
    private lateinit var filterBtn: JmixButton

    @ViewComponent
    private lateinit var appliedFiltersCount: Span

    @ViewComponent
    private lateinit var filterFormLayout: JmixFormLayout

    @ViewComponent
    private lateinit var filterContainer: VerticalLayout

    @ViewComponent
    private lateinit var generalFilters: JmixDetails

    @ViewComponent
    private lateinit var assignmentFilters: JmixDetails

    @ViewComponent
    private lateinit var taskNameField: TypedTextField<String?>

    @ViewComponent
    private lateinit var processNameField: TypedTextField<String?>

    @ViewComponent
    private lateinit var assignmentTypeField: JmixRadioButtonGroup<MyTaskAssignmentType?>

    @ViewComponent
    private lateinit var tasksDataGrid: DataGrid<TaskData>

    // Flowable API
    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var repositoryService: RepositoryService

    //Jmix BPM API
    @Autowired
    private lateinit var userGroupService: UserGroupService

    @Autowired
    private lateinit var processFormViews: ProcessFormViews

    @Autowired
    private lateinit var entitiesConverter: FlowableEntitiesConverter

    @Autowired(required = false)
    private var bpmTenantProvider: BpmTenantProvider? = null

    @Autowired
    private lateinit var currentUserSubstitution: CurrentUserSubstitution

    private var currentUserName: String? = null
    private var userGroupCodes: List<String>? = null

    @Subscribe
    private fun onInit(event: InitEvent) {
        assignmentTypeField.setValue(MyTaskAssignmentType.ALL)
        generalFilters.setSummary(H5(messageBundle.getMessage("taskFilter.generalGroup.summaryText")))
        assignmentFilters.setSummary(H5(messageBundle.getMessage("taskFilter.assignmentGroup.summaryText")))
        tasksDataGrid.sort(GridSortOrder.desc(tasksDataGrid.getColumnByKey(CREATE_TIME_PROPERTY)).build())
    }

    @Subscribe
    private fun onBeforeShow(event: BeforeShowEvent) {
        currentUserName = currentUserSubstitution.effectiveUser.username
        userGroupCodes = currentUserName?.let { username ->
            userGroupService.getUserGroups(username).stream().map(UserGroup::getCode).toList()
        } ?: listOf()

        tasksDl.load()
    }

    @Install(to = "tasksDl", target = Target.DATA_LOADER)
    private fun tasksDlLoadDelegate(loadContext: LoadContext<TaskData>): List<TaskData> {
        val taskQuery: TaskQuery = createTaskQuery()
        addSort(taskQuery, loadContext.query?.sort)

        val tasks: List<Task> =
            loadContext.query?.let { query -> taskQuery.listPage(query.firstResult, query.maxResults) } ?: listOf()

        return tasks.map { task -> entitiesConverter.createTaskData(task)!! }.toList()
    }

    @Subscribe("applyFilter")
    private fun onApplyFilterActionPerformed(event: ActionPerformedEvent) {
        updateAppliedFilterCount()
        tasksDl.load()
    }

    @Subscribe("resetFilter")
    private fun onResetFilter(event: ActionPerformedEvent) {
        taskNameField.clear()
        processNameField.clear()
        assignmentTypeField.setValue(MyTaskAssignmentType.ALL)

        updateAppliedFilterCount()
        tasksDl.load()
    }

    @Subscribe("tasksDataGrid.openTaskForm")
    private fun onTasksDataGridOpenTaskForm(event: ActionPerformedEvent) {
        val task: Task = taskService.createTaskQuery().taskId(tasksDc.getItem().getId()).singleResult()

        processFormViews.openTaskProcessForm(task, this) { processFormDialog ->
            processFormDialog.addAfterCloseListener { tasksDl.load() }
        }
    }

    @Install(to = "tasksPagination", subject = "totalCountDelegate")
    private fun tasksPaginationTotalCountDelegate(loadContext: DataLoadContext): Int {
        val taskQuery: TaskQuery = createTaskQuery()

        return taskQuery.count().toInt()
    }

    @Subscribe(id = "filterBtn", subject = "clickListener")
    fun onFilterBtnClick(event: ClickEvent<JmixButton?>?) {
        filterContainer.isVisible = !filterContainer.isVisible
        if (filterContainer.isVisible) {
            filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        } else {
            filterBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
        }
    }

    @Subscribe(id = "tasksDl", target = Target.DATA_LOADER)
    fun onTasksDlPostLoad(event: CollectionLoader.PostLoadEvent<TaskData?>?) {
        loadProcessDefinitions()
    }

    /**
     * Shows the number of applied filters to the tasks list on the filter button. Zero is not displayed on the button.
     */
    private fun updateAppliedFilterCount() {
        val filterCount: Long = calcAppliedFiltersCount()

        if (filterCount > 0) {
            appliedFiltersCount.isVisible = true
            appliedFiltersCount.text = filterCount.toString()
        } else {
            appliedFiltersCount.isVisible = false
        }
    }

    /**
     * Calculates the number of fields that have a non-empty, non-default value in the task filter form.
     *
     * @return the number of fields containing a non-empty and non-default value
     */
    private fun calcAppliedFiltersCount(): Long {
        return filterFormLayout.components
            .stream()
            .filter { component: Component? ->
                if (component is HasValue<*, *>) {
                    val value = UiComponentUtils.getValue(component)
                    if (value is MyTaskAssignmentType) {
                        return@filter (value != MyTaskAssignmentType.ALL) // exclude a default filter value
                    }
                    return@filter (value != null)
                }
                false
            }
            .count()
    }

    @Supply(to = "tasksDataGrid.process", subject = "renderer")
    private fun tasksDataGridProcessRenderer(): Renderer<TaskData> {
        return TextRenderer { taskData ->
            val processDefinitionData =
                taskData.processDefinitionId?.let { processDefinitionsDc.getItemOrNull(taskData.processDefinitionId) }
            processDefinitionData?.name
        }
    }

    @EventListener(TaskCompletedUiEvent::class)
    open fun onTaskCompletedEvent() {
        tasksDl.load()
    }

    /**
     * Loads process definitions for the loaded user tasks to show data in the "Process" column.
     */
    private fun loadProcessDefinitions() {
        val processDefinitionIds = tasksDc.items
            .map { obj: TaskData -> obj.processDefinitionId }
            .filter { processDefinitionId: String? ->
                processDefinitionId != null && !processDefinitionsDc.containsItem(processDefinitionId)
            }
            .toSet()

        if (processDefinitionIds.isNotEmpty()) {
            val processDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionIds(processDefinitionIds)
                .list()
                .map { processDefinition: ProcessDefinition? ->
                    entitiesConverter.createProcessDefinitionData(processDefinition)
                }
                .toList()

            processDefinitionsDc.mutableItems.addAll(processDefinitions)
        }
    }

    /**
     * Creates a task query to get active user tasks by filter conditions.
     *
     * @return created query with filter conditions
     */
    private fun createTaskQuery(): TaskQuery {
        val taskQuery = taskService.createTaskQuery().active()

        if (!taskNameField.typedValue.isNullOrEmpty()) {
            taskQuery.taskNameLikeIgnoreCase("%" + taskNameField.typedValue + "%")
        }

        if (!processNameField.typedValue.isNullOrEmpty()) {
            taskQuery.processDefinitionNameLike("%" + processNameField.typedValue + "%")
        }

        addAssignmentCondition(taskQuery)

        return taskQuery
    }

    /**
     * Adds a condition related to the task assignment using the following rules:
     * <ol>
     *     <li>The "All" option: load tasks that the current user is assigned to or is a candidate for</li>
     *     <li>The "Assigned to me" option: load tasks that the current user is assigned to</li>
     *     <li>The "Group" option: load tasks that the current user is a candidate for</li>
     * </ol>
     *
     * @param taskQuery query to load tasks that the current user can perform
     */
    private fun addAssignmentCondition(taskQuery: TaskQuery) {
        val assignmentType: MyTaskAssignmentType? = assignmentTypeField.value
        if (assignmentType == MyTaskAssignmentType.ASSIGNED_TO_ME) {
            taskQuery.taskAssignee(currentUserName)
        } else {
            taskQuery.or()
            if (assignmentType == MyTaskAssignmentType.GROUP) {
                taskQuery.taskCandidateUser(currentUserName)
            } else {
                taskQuery.taskCandidateOrAssigned(currentUserName)
            }

            if (userGroupCodes?.isNotEmpty() == true) {
                taskQuery.taskCandidateGroupIn(userGroupCodes)
            }

            taskQuery.endOr()
        }
        if (bpmTenantProvider != null && bpmTenantProvider?.isMultitenancyActive == true) {
            taskQuery.taskTenantId(bpmTenantProvider!!.currentUserTenantId)
        }
    }

    /**
     * Adds sort options to the specified query based on the specified [Sort] instance.
     *
     * @param taskQuery a query to load user tasks
     * @param sort      options to sort user tasks
     */
    private fun addSort(taskQuery: TaskQuery, sort: Sort?) {
        if (sort != null && sort.orders.isNotEmpty()) {
            val orders = sort.orders
            orders.forEach { order: Sort.Order ->
                val sortProperty: TaskQueryProperty? = when (order.property) {
                    CREATE_TIME_PROPERTY -> TaskQueryProperty.CREATE_TIME
                    NAME_PROPERTY -> TaskQueryProperty.NAME
                    DUE_DATE_PROPERTY -> TaskQueryProperty.DUE_DATE
                    else -> null
                }

                sortProperty?.let {
                    taskQuery.orderBy(
                        sortProperty,
                        org.flowable.common.engine.api.query.Query.NullHandlingOnOrder.NULLS_LAST
                    )
                    if (order.direction == Sort.Direction.ASC) {
                        taskQuery.asc()
                    } else {
                        taskQuery.desc()
                    }
                }
            }
        }
    }

    enum class MyTaskAssignmentType(private val id: String) : EnumClass<String> {
        ALL("All"),
        ASSIGNED_TO_ME("Assigned to me"),
        GROUP("Group");

        override fun getId(): String {
            return id
        }

        companion object {
            fun fromId(id: String): MyTaskAssignmentType? {
                for (at in entries) {
                    if (at.getId() == id) {
                        return at
                    }
                }
                return null
            }
        }
    }
}