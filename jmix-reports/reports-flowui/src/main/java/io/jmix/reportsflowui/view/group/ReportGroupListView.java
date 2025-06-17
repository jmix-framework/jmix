package io.jmix.reportsflowui.view.group;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataLoadContext;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportGroupFilter;
import io.jmix.reports.ReportGroupLoadContext;
import io.jmix.reports.ReportGroupRepository;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportGroupInfo;
import io.jmix.reports.entity.ReportSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

@Route(value = "reports/groups", layout = DefaultMainViewParent.class)
@ViewController("report_ReportGroup.list")
@ViewDescriptor("report-group-list-view.xml")
@LookupComponent("reportGroupsDataGrid")
public class ReportGroupListView extends StandardListView<ReportGroupInfo> {

    @ViewComponent
    protected DataGrid<ReportGroupInfo> reportGroupsDataGrid;
    @ViewComponent("reportGroupsDataGrid.remove")
    protected RemoveAction<ReportGroupInfo> removeAction;

    @ViewComponent("reportGroupsDataGrid.edit")
    protected EditAction<ReportGroupInfo> editAction;
    @ViewComponent
    protected TypedTextField<String> titleFilter;
    @ViewComponent
    protected TypedTextField<String> codeFilter;
    @ViewComponent
    protected CollectionLoader<ReportGroupInfo> groupsDl;

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected ReportGroupRepository reportGroupRepository;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected Messages messages;
    @Autowired
    private ReportRepository reportRepository;

    @Subscribe
    public void onInit(final InitEvent event) {
        SecuredBaseAction.EnabledRule rule = () -> {
            return reportGroupsDataGrid.getSingleSelectedItem() != null
                   && reportGroupsDataGrid.getSingleSelectedItem().getSource() == ReportSource.DATABASE;
        };

        removeAction.addEnabledRule(rule);
    }

    @Subscribe("reportGroupsDataGrid")
    public void onReportGroupsDataGridSelection(final SelectionEvent<DataGrid<ReportGroupInfo>, ReportGroupInfo> event) {
        ReportGroupInfo group = reportGroupsDataGrid.getSingleSelectedItem();
        String text;
        if (group == null || group.getSource() == ReportSource.DATABASE) {
            text = messages.getMessage("actions.Edit");
        } else {
            text = messages.getMessage("actions.Read");
        }
        editAction.setText(text);
    }

    @Subscribe("reportGroupsDataGrid.remove")
    public void onReportGroupsDataGridRemove(final ActionPerformedEvent event) {
        if (!event.getSource().isEnabled()) {
            return;
        }

        ReportGroupInfo group = reportGroupsDataGrid.getSingleSelectedItem();
        if (group == null || group.getSource() != ReportSource.DATABASE) {
            return;
        }

        if (group.getSystemFlag()) {
            notifications.create(messageBundle.getMessage("unableToDeleteSystemReportGroup"))
                    .withType(Notifications.Type.WARNING)
                    .show();
        } else {
            boolean relatedReportExists = reportRepository.existsReportByGroup(group);
            if (relatedReportExists) {
                notifications.create(messageBundle.getMessage("unableToDeleteNotEmptyReportGroup"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            } else {
                removeAction.execute();
            }
        }
    }

    @Install(to = "reportGroupsDataGrid.remove", subject = "delegate")
    private void reportGroupsDataGridRemoveDelegate(final Collection<ReportGroupInfo> collection) {
        for (ReportGroupInfo info : collection) {
            reportGroupRepository.remove(info);
        }
    }

    @Subscribe("reportGroupsDataGrid.create")
    public void onReportGroupsDataGridCreate(final ActionPerformedEvent event) {
        // need custom implementation because detail's entity differs from entity in list component
        dialogWindows.detail(this, ReportGroup.class)
                .newEntity()
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        refreshGrid();
                    }
                })
                .open();
    }

    @Subscribe("reportGroupsDataGrid.edit")
    public void onReportGroupsDataGridEdit(final ActionPerformedEvent event) {
        // need custom implementation because detail's entity differs from entity in list component
        ReportGroupInfo item = reportGroupsDataGrid.getSingleSelectedItem();
        if (item == null) {
            return;
        }

        ReportGroup entity = reportGroupRepository.loadModelObject(item);
        boolean readOnly = item.getSource() == ReportSource.ANNOTATED_CLASS;

        dialogWindows.detail(this, ReportGroup.class)
                .editEntity(entity)
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        refreshGrid();
                    }
                })
                .withViewConfigurer(view -> {
                    ((ReportGroupDetailView) view).setReadOnly(readOnly);
                })
                .open();
    }

    @Install(to = "groupsDl", target = Target.DATA_LOADER)
    public List<ReportGroupInfo> groupsDlLoadDelegate(final LoadContext<ReportGroupInfo> loadContext) {
        ReportGroupFilter filter = createLoadFilter();
        ReportGroupLoadContext context = new ReportGroupLoadContext(
                filter,
                loadContext.getQuery().getSort(),
                loadContext.getQuery().getFirstResult(),
                loadContext.getQuery().getMaxResults()
        );

        return reportGroupRepository.loadList(context);
    }

    protected ReportGroupFilter createLoadFilter() {
        ReportGroupFilter filter = new ReportGroupFilter(
                titleFilter.getValue(),
                codeFilter.getValue()
        );
        return filter;
    }

    @Install(to = "pagination", subject = "totalCountDelegate")
    public Integer paginationTotalCountDelegate(final DataLoadContext dataLoadContext) {
        ReportGroupFilter filter = createLoadFilter();
        return reportGroupRepository.getTotalCount(filter);
    }

    @Subscribe(id = "clearBtn", subject = "clickListener")
    public void onClearBtnClick(final ClickEvent<JmixButton> event) {
        titleFilter.clear();
        codeFilter.clear();

        refreshGrid();
    }

    @Subscribe(id = "searchBtn", subject = "clickListener")
    public void onSearchBtnClick(final ClickEvent<JmixButton> event) {
        refreshGrid();
    }

    protected void refreshGrid() {
        groupsDl.load();
    }
}
