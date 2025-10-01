package io.jmix.reports.test_support.report;

import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@ReportDef(
        name = "Revenue by publisher for selected period",
        code = RevenueByPublisherReport.CODE,
        description = """
                Uses the following features:
                - CROSSTAB band
                """
)
@InputParameterDef(
        alias = RevenueByPublisherReport.PARAM_START_DATE,
        name = "Start date",
        type = ParameterType.DATE,
        required = true,
        defaultValue = "2025-01-01T00:00:00"
)
@InputParameterDef(
        alias = RevenueByPublisherReport.PARAM_END_DATE,
        name = "End date",
        type = ParameterType.DATE,
        required = true,
        defaultValue = "2025-06-01T00:00:00"
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL
)
@BandDef(
        name = "revenue",
        parent = "Root",
        orientation = Orientation.CROSS,
        dataSets = {
                @DataSetDef(
                        name = "revenue_dynamic_header",
                        type = DataSetType.DELEGATE
                ),
                @DataSetDef(
                        name = "revenue_master_data",
                        type = DataSetType.JPQL,
                        query = """
                                select
                                p.name as "publisher_name",
                                p.id as "publisher_id"
                                from Publisher p
                                order by p.name asc
                                """
                ),
                @DataSetDef(
                        name = "revenue",
                        type = DataSetType.JPQL,
                        query = """
                                select
                                p.id as "revenue_master_data@publisher_id",
                                extract(year from pg.purchaseDate) as "revenue_dynamic_header@year",
                                extract(month from pg.purchaseDate) as "revenue_dynamic_header@month",
                                sum(g.price) as "amount"
                                from PurchasedGame pg
                                join pg.game g
                                join g.publisher p
                                where
                                pg.purchaseDate >= ${startDate} and pg.purchaseDate <= ${endDate}
                                and extract(year from pg.purchaseDate) in ${revenue_dynamic_header@year}
                                and extract(month from pg.purchaseDate) in ${revenue_dynamic_header@month}
                                and p.id in ${revenue_master_data@publisher_id}
                                group by p.id, extract(year from pg.purchaseDate), extract(month from pg.purchaseDate)
                                """
                )
        }
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.XLSX,
        filePath = "io/jmix/reports/test_support/report/RevenueByPublisher.xlsx",
        isDefault = true
)
public class RevenueByPublisherReport {
    public static final String CODE = "REVENUE_BY_PUBLISHER";

    public static final String PARAM_START_DATE = "startDate";
    public static final String PARAM_END_DATE = "endDate";

    @DataSetDelegate(name = "revenue_dynamic_header")
    public ReportDataLoader revenueDynamicHeaderDataLoader() {
        return (reportQuery, parentBand, parameters) -> {
            Date startDate = (Date) parameters.get(PARAM_START_DATE);
            Date endDate = (Date) parameters.get(PARAM_END_DATE);
            if (startDate.after(endDate)) {
                return Collections.emptyList();
            }

            DateFormatSymbols dateFormatSymbols = DateFormatSymbols.getInstance(Locale.ENGLISH);
            LocalDate startLDate = LocalDate.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
            LocalDate endLDate = LocalDate.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

            List<Map<String, Object>> entries = new ArrayList<>();
            LocalDate monthStart = startLDate.withDayOfMonth(1);
            do {
                Map<String, Object> entry = new HashMap<>();
                entry.put("year", monthStart.getYear());
                entry.put("month", monthStart.getMonthValue());
                entry.put("month_caption", dateFormatSymbols.getMonths()[(monthStart.getMonthValue() - 1)]);
                entries.add(entry);

                monthStart = monthStart.plusMonths(1);
            } while (monthStart.isBefore(endLDate));

            return entries;
        };
    }
}
