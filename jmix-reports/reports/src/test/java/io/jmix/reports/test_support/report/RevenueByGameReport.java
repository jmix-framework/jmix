package io.jmix.reports.test_support.report;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.test_support.entity.GameTitle;
import io.jmix.reports.test_support.entity.PurchasedGame;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.CustomValueFormatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ReportDef(
        name = "Revenue by game for selected period",
        code = RevenueByGameReport.CODE,
        description = """
                Uses the following features:
                - default values for input parameters
                - DELEGATE data sets
                - CSV output
                - output name pattern
                - custom value formatter
                """
)
@InputParameterDef(
        alias = RevenueByGameReport.PARAM_START_DATE,
        name = "Start date",
        type = ParameterType.DATE,
        required = true,
        defaultValue = "2025-04-01T00:00:00"
)
@InputParameterDef(
        alias = RevenueByGameReport.PARAM_END_DATE,
        name = "End date",
        type = ParameterType.DATE,
        required = true,
        defaultValue = "2025-06-01T00:00:00"
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL,
        dataSets = @DataSetDef(
                name = "Root",
                type = DataSetType.DELEGATE
        )
)
@BandDef(
        name = "Data",
        parent = "Root",
        orientation = Orientation.HORIZONTAL,
        dataSets = @DataSetDef(
                name = "Data",
                type = DataSetType.DELEGATE
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.CSV,
        filePath = "io/jmix/reports/test_support/report/RevenueByGame.csv",
        outputNamePattern = "Revenue ${Root.dateRange}.csv",
        isDefault = true
)
@ValueFormatDef(
        band = "Data",
        field = "revenue"
)
public class RevenueByGameReport {
    public static final String CODE = "REVENUE_BY_GAME";

    public static final String PARAM_START_DATE = "startDate";
    public static final String PARAM_END_DATE = "endDate";

    private final UnconstrainedDataManager unconstrainedDataManager;

    public RevenueByGameReport(UnconstrainedDataManager unconstrainedDataManager) {
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    @DataSetDelegate(name = "Root")
    public ReportDataLoader rootDataLoader() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM");
        return (reportQuery, parentBand, parameters) -> {
            String dateRange = String.format("%s - %s",
                    sdf.format(parameters.get(PARAM_START_DATE)),
                    sdf.format(parameters.get(PARAM_END_DATE))
            );

            return List.of(
                    new HashMap<>(Map.of(
                            "dateRange", dateRange
                    ))
            );
        };
    }

    // just to demonstrate java delegate
    // the same can be obtained much easily with JPQL
    @DataSetDelegate(name = "Data")
    public ReportDataLoader dataDataLoader() {
        return (reportQuery, parentBand, parameters) -> {
            java.util.Date startDate = (java.util.Date) parameters.get(PARAM_START_DATE);
            java.util.Date endDate = (java.util.Date) parameters.get(PARAM_END_DATE);

            List<PurchasedGame> purchases = unconstrainedDataManager.load(PurchasedGame.class)
                    .query("select pg from PurchasedGame pg join fetch pg.game" +
                           " where pg.purchaseDate >= :startDate and pg.purchaseDate <= :endDate")
                    .parameter("startDate", startDate)
                    .parameter("endDate", endDate)
                    .list();

            Map<GameTitle, Integer> purchaseCounts = purchases.stream()
                    .collect(Collectors.toMap(
                            PurchasedGame::getGame,
                            pg -> 1,
                            Integer::sum
                    ));

            return purchaseCounts.entrySet().stream()
                    .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                    .map(entry -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("game", entry.getKey().getName());
                        row.put("count", entry.getValue());

                        BigDecimal price = entry.getKey().getPrice();
                        BigDecimal purchaseCount = BigDecimal.valueOf(entry.getValue());
                        BigDecimal revenue = price.multiply(purchaseCount);
                        row.put("revenue", revenue);
                        return row;
                    })
                    .toList();
        };
    }

    @ValueFormatDelegate(band = "Data", field = "revenue")
    public CustomValueFormatter<BigDecimal> dataRevenueValueFormat() {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');

        DecimalFormat df = new DecimalFormat("#.00");
        df.setDecimalFormatSymbols(dfs);
        df.setGroupingUsed(false);

        return value -> {
            return "$" + df.format(value);
        };
    }
}
