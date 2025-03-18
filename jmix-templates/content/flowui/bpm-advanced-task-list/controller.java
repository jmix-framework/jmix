package ${packageName};

<%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%>
import io.jmix.flowui.view.DefaultMainViewParent;
<%} else {%>
import ${routeLayout.getControllerFqn()};
<%}%>
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.bpm.entity.ProcessDefinitionData;
import io.jmix.bpm.entity.TaskData;
import io.jmix.bpm.entity.UserGroup;
import io.jmix.bpm.multitenancy.BpmTenantProvider;
import io.jmix.bpm.service.UserGroupService;
import io.jmix.bpm.util.FlowableEntitiesConverter;
import io.jmix.bpmflowui.event.TaskCompletedUiEvent;
import io.jmix.bpmflowui.processform.ProcessFormViews;
import io.jmix.core.DataLoadContext;
import io.jmix.core.LoadContext;
import io.jmix.core.Sort;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.query.Query;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.service.impl.TaskQueryProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

<%if (classComment) {%>
${classComment}
<%}%>@Route(value = "${route}", layout = <%if (!api.jmixProjectModule.isApplication() || routeLayout == null) {%> DefaultMainViewParent.class <%} else {%>${routeLayout.getControllerClassName()}.class<%}%>)
@ViewController(id = "${viewId}")
@ViewDescriptor(path = "${descriptorName}.xml")
public class ${controllerName} extends StandardListView<TaskData> {
    public static final String CREATE_TIME_PROPERTY = "createTime";
    public static final String NAME_PROPERTY = "name";
    public static final String DUE_DATE_PROPERTY = "dueDate";

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private CollectionLoader<TaskData> tasksDl;
    @ViewComponent
    private CollectionContainer<TaskData> tasksDc;
    @ViewComponent
    private CollectionContainer<ProcessDefinitionData> processDefinitionsDc;

    @ViewComponent
    private JmixButton filterBtn;
    @ViewComponent
    private Span appliedFiltersCount;
    @ViewComponent
    private JmixFormLayout filterFormLayout;
    @ViewComponent
    private VerticalLayout filterContainer;

    @ViewComponent
    private JmixDetails generalFilters;
    @ViewComponent
    private JmixDetails assignmentFilters;
    @ViewComponent
    private TypedTextField<String> taskNameField;
    @ViewComponent
    private TypedTextField<String> processNameField;
    @ViewComponent
    private JmixRadioButtonGroup<MyTaskAssignmentType> assignmentTypeField;
    @ViewComponent
    private DataGrid<TaskData> tasksDataGrid;

    // Flowable API
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;

    //Jmix BPM API
    @Autowired
    private UserGroupService userGroupService;
    @Autowired
    private ProcessFormViews processFormViews;
    @Autowired
    private FlowableEntitiesConverter entitiesConverter;
    @Autowired(required = false)
    private BpmTenantProvider bpmTenantProvider;

    @Autowired
    private CurrentUserSubstitution currentUserSubstitution;

    private String currentUserName;
    private List<String> userGroupCodes;

    @Subscribe
    public void onInit(final InitEvent event) {
        assignmentTypeField.setValue(MyTaskAssignmentType.ALL);
        generalFilters.setSummary(new H5(messageBundle.getMessage("taskFilter.generalGroup.summaryText")));
        assignmentFilters.setSummary(new H5(messageBundle.getMessage("taskFilter.assignmentGroup.summaryText")));
        tasksDataGrid.sort(GridSortOrder.desc(tasksDataGrid.getColumnByKey(CREATE_TIME_PROPERTY)).build());
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        currentUserName = currentUserSubstitution.getEffectiveUser().getUsername();
        userGroupCodes = userGroupService.getUserGroups(currentUserName).stream().map(UserGroup::getCode).toList();

        tasksDl.load();
    }

    @Install(to = "tasksDl", target = Target.DATA_LOADER)
    private List<TaskData> tasksDlLoadDelegate(final LoadContext<TaskData> loadContext) {
        TaskQuery taskQuery = createTaskQuery();

        Sort sort = Optional.ofNullable(loadContext.getQuery()).map(LoadContext.Query::getSort).orElse(null);
        addSort(taskQuery, sort);

        List<Task> tasks = taskQuery.listPage(loadContext.getQuery().getFirstResult(), loadContext.getQuery().getMaxResults());

        return tasks.stream().map(entitiesConverter::createTaskData).toList();
    }

    @Subscribe("applyFilter")
    private void onApplyFilterActionPerformed(ActionPerformedEvent event) {
        updateAppliedFilterCount();
        tasksDl.load();
    }

    @Subscribe("resetFilter")
    private void onResetFilter(final ActionPerformedEvent event) {
        taskNameField.clear();
        processNameField.clear();
        assignmentTypeField.setValue(MyTaskAssignmentType.ALL);

        updateAppliedFilterCount();
        tasksDl.load();
    }

    @Subscribe("tasksDataGrid.openTaskForm")
    private void onTasksDataGridOpenTaskForm(ActionPerformedEvent event) {
        Task task = taskService.createTaskQuery().taskId(tasksDc.getItem().getId()).singleResult();

        processFormViews.openTaskProcessForm(task, this, processFormDialog -> {
            processFormDialog.addAfterCloseListener(afterCloseEvent -> tasksDl.load());
        });
    }

    @Install(to = "tasksPagination", subject = "totalCountDelegate")
    private Integer tasksPaginationTotalCountDelegate(final DataLoadContext loadContext) {
        TaskQuery taskQuery = createTaskQuery();

        return (int) taskQuery.count();
    }

    @Subscribe(id = "filterBtn", subject = "clickListener")
    public void onFilterBtnClick(final ClickEvent<JmixButton> event) {
        filterContainer.setVisible(!filterContainer.isVisible());
        if (filterContainer.isVisible()) {
            filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            filterBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
    }

    @Subscribe(id = "tasksDl", target = Target.DATA_LOADER)
    public void onTasksDlPostLoad(final CollectionLoader.PostLoadEvent<TaskData> event) {
        loadProcessDefinitions();
    }

    /**
     * Shows the number of applied filters to the tasks list on the filter button. Zero is not displayed on the button.
     */
    private void updateAppliedFilterCount() {
        long filterCount = calcAppliedFiltersCount();

        if (filterCount > 0) {
            appliedFiltersCount.setVisible(true);
            appliedFiltersCount.setText(String.valueOf(filterCount));
        } else {
            appliedFiltersCount.setVisible(false);
        }
    }

    /**
     * Calculates the number of fields that have a non-empty, non-default value in the task filter form.
     *
     * @return the number of fields containing a non-empty and non-default value
     */
    private long calcAppliedFiltersCount() {
        return filterFormLayout.getComponents()
                .stream()
                .filter(component -> {
                    if (component instanceof HasValue<?, ?> hasValue) {
                        Object value = UiComponentUtils.getValue(hasValue);
                        if (value instanceof MyTaskAssignmentType assignmentType) {
                            return assignmentType != MyTaskAssignmentType.ALL; // exclude a default filter value
                        }
                        return value != null;
                    }
                    return false;
                })
                .count();
    }

    @Supply(to = "tasksDataGrid.process", subject = "renderer")
    private Renderer<TaskData> tasksDataGridProcessRenderer() {
        return new TextRenderer<>(taskData -> {
            if (taskData.getProcessDefinitionId() == null) {
                return null;
            }
            ProcessDefinitionData processDefinitionData = processDefinitionsDc.getItemOrNull(taskData.getProcessDefinitionId());
            return processDefinitionData != null ? processDefinitionData.getName() : null;
        });
    }

    @EventListener(TaskCompletedUiEvent.class)
    public void onTaskCompletedEvent() {
        tasksDl.load();
    }

    /**
     * Loads process definitions for the loaded user tasks to show data in the "Process" column.
     */
    private void loadProcessDefinitions() {
        Set<String> processDefinitionIds = tasksDc.getItems()
                .stream()
                .map(TaskData::getProcessDefinitionId)
                .filter(processDefinitionId -> processDefinitionId != null && !processDefinitionsDc.containsItem(processDefinitionId))
                .collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(processDefinitionIds)) {
            List<ProcessDefinitionData> processDefinitions = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionIds(processDefinitionIds)
                    .list()
                    .stream()
                    .map(entitiesConverter::createProcessDefinitionData)
                    .toList();

            processDefinitionsDc.getMutableItems().addAll(processDefinitions);
        }
    }

    /**
     * Creates a task query to get active user tasks by filter conditions.
     *
     * @return created query with filter conditions
     */
    private TaskQuery createTaskQuery() {
        TaskQuery taskQuery = taskService.createTaskQuery().active();

        if (StringUtils.isNotEmpty(taskNameField.getTypedValue())) {
            taskQuery.taskNameLikeIgnoreCase("%" + taskNameField.getTypedValue() + "%");
        }

        if (StringUtils.isNotEmpty(processNameField.getTypedValue())) {
            taskQuery.processDefinitionNameLike("%" + processNameField.getTypedValue() + "%");
        }

        addAssignmentCondition(taskQuery);

        return taskQuery;
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
    private void addAssignmentCondition(TaskQuery taskQuery) {
        MyTaskAssignmentType assignmentType = assignmentTypeField.getValue();
        if (assignmentType == MyTaskAssignmentType.ASSIGNED_TO_ME) {
            taskQuery.taskAssignee(currentUserName);
        } else {
            taskQuery.or();
            if (assignmentType == MyTaskAssignmentType.GROUP) {
                taskQuery.taskCandidateUser(currentUserName);
            } else {
                taskQuery.taskCandidateOrAssigned(currentUserName);
            }

            if (CollectionUtils.isNotEmpty(userGroupCodes)) {
                taskQuery.taskCandidateGroupIn(userGroupCodes);
            }

            taskQuery.endOr();
        }
        if (bpmTenantProvider != null && bpmTenantProvider.isMultitenancyActive()) {
            taskQuery.taskTenantId(bpmTenantProvider.getCurrentUserTenantId());
        }
    }

    /**
     * Adds sort options to the specified query based on the specified {@link Sort} instance.
     *
     * @param taskQuery a query to load user tasks
     * @param sort      options to sort user tasks
     */
    private void addSort(TaskQuery taskQuery, @Nullable Sort sort) {
        if (sort != null && CollectionUtils.isNotEmpty(sort.getOrders())) {
            List<Sort.Order> orders = sort.getOrders();
            orders.forEach(order -> {
                TaskQueryProperty sortProperty = switch (order.getProperty()) {
                    case CREATE_TIME_PROPERTY -> TaskQueryProperty.CREATE_TIME;
                    case NAME_PROPERTY -> TaskQueryProperty.NAME;
                    case DUE_DATE_PROPERTY -> TaskQueryProperty.DUE_DATE;
                    default -> null;
                };

                if (sortProperty != null) {
                    taskQuery.orderBy(sortProperty, Query.NullHandlingOnOrder.NULLS_LAST);
                    if (order.getDirection() == Sort.Direction.ASC) {
                        taskQuery.asc();
                    } else {
                        taskQuery.desc();
                    }
                }
            });
        }
    }

    public enum MyTaskAssignmentType implements EnumClass<String> {
        ALL("All"),
        ASSIGNED_TO_ME("Assigned to me"),
        GROUP("Group");

        private final String id;

        MyTaskAssignmentType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Nullable
        public static MyTaskAssignmentType fromId(String id) {
            for (MyTaskAssignmentType at : MyTaskAssignmentType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }
}