package io.jmix.reports.test_support.report;

import io.jmix.reports.annotation.ReportGroupDef;

@ReportGroupDef(
        title = "msg://DemoReportGroup.title",
        code = DemoReportGroup.CODE,
        uuid = "15c81a52-09fc-4de7-e08a-b8a9a9155f15",
        beanName = "sample_demoReportGroup"
)
public class DemoReportGroup {
    public static final String CODE = "TEST_DEMOS";
}
