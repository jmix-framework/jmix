package io.jmix.reportsui.screen.report.edit.tabs;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportValueFormat;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("report_ReportEditFormats.fragment")
@UiDescriptor("formats.xml")
public class FormatsFragment extends ScreenFragment {

    @Autowired
    protected InstanceContainer<Report> reportDc;

    @Install(to = "valuesFormatsTable.create", subject = "initializer")
    protected void valuesFormatsTableCreateInitializer(ReportValueFormat reportValueFormat) {
        reportValueFormat.setReport(reportDc.getItem());
    }
}
