package io.jmix.reportsflowui.view.group;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.*;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportSource;
import io.jmix.reportsflowui.helper.GridSortHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Route(value = "reports/groups", layout = DefaultMainViewParent.class)
@ViewController("report_ReportGroup.list")
@ViewDescriptor("report-group-list-view.xml")
@LookupComponent("reportGroupsDataGrid")
public class ReportGroupListView extends StandardListView<ReportGroup> {

    @ViewComponent
    protected DataGrid<ReportGroup> reportGroupsDataGrid;
    @ViewComponent("reportGroupsDataGrid.remove")
    protected RemoveAction<ReportGroup> removeAction;

    @ViewComponent("reportGroupsDataGrid.edit")
    protected EditAction<ReportGroup> editAction;
    @ViewComponent
    protected TypedTextField<String> titleFilter;
    @ViewComponent
    protected TypedTextField<String> codeFilter;
    @ViewComponent
    protected CollectionLoader<ReportGroup> groupsDl;

    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;
    @Autowired
    protected ReportGroupRepository reportGroupRepository;
    @Autowired
    protected Messages messages;
    @Autowired
    protected ReportRepository reportRepository;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected GridSortHelper gridSortHelper;

    @Subscribe
    public void onInit(final InitEvent event) {
        codeFilter.addTypedValueChangeListener(ev -> refreshGrid());
        titleFilter.addTypedValueChangeListener(ev -> refreshGrid());
    }

    @Install(to = "reportGroupsDataGrid.remove", subject = "enabledRule")
    protected boolean reportsDataGridEditEnabledRule() {
        return reportGroupsDataGrid.getSingleSelectedItem() != null
               && reportGroupsDataGrid.getSingleSelectedItem().getSource() == ReportSource.DATABASE;
    }

    @Subscribe("reportGroupsDataGrid")
    public void onReportGroupsDataGridSelection(final SelectionEvent<DataGrid<ReportGroup>, ReportGroup> event) {
        ReportGroup group = reportGroupsDataGrid.getSingleSelectedItem();
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

        ReportGroup group = reportGroupsDataGrid.getSingleSelectedItem();
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

    @Install(to = "reportGroupsDataGrid.edit", subject = "viewConfigurer")
    private void reportGroupsDataGridEditViewConfigurer(final ReportGroupDetailView reportGroupDetailView) {
        ReportGroup selectedItem = reportGroupsDataGrid.getSingleSelectedItem();
        if (selectedItem == null) {
            return;
        }
        boolean readOnly = selectedItem.getSource() == ReportSource.ANNOTATED_CLASS;
        reportGroupDetailView.setReadOnly(readOnly);
    }

    @Install(to = "groupsDl", target = Target.DATA_LOADER)
    public List<ReportGroup> groupsDlLoadDelegate(final LoadContext<ReportGroup> loadContext) {
        ReportGroupFilter filter = createFilter();
        Sort sort = gridSortHelper.convertSortOrders(
                reportGroupsDataGrid.getSortOrder(),
                Map.of("title", ReportGroupLoadContext.LOCALIZED_TITLE_SORT_KEY) // custom cell renderer
        );
        ReportGroupLoadContext context = new ReportGroupLoadContext(
                filter,
                sort,
                loadContext.getQuery().getFirstResult(),
                loadContext.getQuery().getMaxResults()
        );

        return reportGroupRepository.loadList(context);
    }

    protected ReportGroupFilter createFilter() {
        ReportGroupFilter filter = new ReportGroupFilter();
        filter.setTitleContains(titleFilter.getValue());
        filter.setCodeContains(codeFilter.getValue());
        return filter;
    }

    @Install(to = "pagination", subject = "totalCountDelegate")
    public Integer paginationTotalCountDelegate(final DataLoadContext ignored) {
        ReportGroupFilter filter = createFilter();
        return reportGroupRepository.getTotalCount(filter);
    }

    protected void refreshGrid() {
        groupsDl.load();
    }

    @Supply(to = "reportGroupsDataGrid.title", subject = "renderer")
    protected Renderer<ReportGroup> reportGroupsDataGridTitleRenderer() {
        return new TextRenderer<>(metadataTools::getInstanceName);
    }
}
