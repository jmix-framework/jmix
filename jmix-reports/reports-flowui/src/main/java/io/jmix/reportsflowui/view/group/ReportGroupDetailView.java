package io.jmix.reportsflowui.view.group;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.ReportGroup;

@Route(value = "reports/groups/:id", layout = DefaultMainViewParent.class)
@ViewController("report_ReportGroup.detail")
@ViewDescriptor("report-group-detail-view.xml")
@EditedEntityContainer("groupDc")
@DialogMode(width = "50em")
public class ReportGroupDetailView extends StandardDetailView<ReportGroup> {
}
