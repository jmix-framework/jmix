package io.jmix.reportsflowui.view.group;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Route(value = "reports/groups", layout = DefaultMainViewParent.class)
@ViewController("report_ReportGroup.list")
@ViewDescriptor("report-group-list-view.xml")
@LookupComponent("reportGroupsDataGrid")
public class ReportGroupListView extends StandardListView<ReportGroup> {

    @ViewComponent
    protected DataGrid<ReportGroup> reportGroupsDataGrid;
    @ViewComponent("reportGroupsDataGrid.remove")
    protected RemoveAction<ReportGroup> removeAction;

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected MetadataTools metadataTools;

    @Subscribe
    public void onInit(InitEvent event) {
        reportGroupsDataGrid.addColumn(reportGroup -> metadataTools.getInstanceName(reportGroup))
                .setHeader(messageBundle.getMessage("title"))
                .setKey("title")
                .setResizable(true)
                .setSortable(true);

        List<Grid.Column<ReportGroup>> columnsOrder = new ArrayList<>(Arrays.asList(
                reportGroupsDataGrid.getColumnByKey("title"),
                reportGroupsDataGrid.getColumnByKey("systemFlag")
        ));
        reportGroupsDataGrid.setColumnOrder(columnsOrder);
    }

    @Subscribe("reportGroupsDataGrid.remove")
    public void onReportGroupsDataGridRemove(final ActionPerformedEvent event) {
        if (!event.getSource().isEnabled()) {
            return;
        }

        ReportGroup group = reportGroupsDataGrid.getSingleSelectedItem();
        if (group != null) {
            if (group.getSystemFlag()) {
                notifications.create(messageBundle.getMessage("unableToDeleteSystemReportGroup"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            } else {
                Optional<Report> report = dataManager.load(Report.class)
                        .query("select r from report_Report r where r.group.id = :groupId")
                        .parameter("groupId", group.getId())
                        .fetchPlan("report.view")
                        .optional();

                if (report.isPresent()) {
                    notifications.create(messageBundle.getMessage("unableToDeleteNotEmptyReportGroup"))
                            .withType(Notifications.Type.WARNING)
                            .show();
                } else {
                    removeAction.execute();
                }
            }
        }
    }
}
