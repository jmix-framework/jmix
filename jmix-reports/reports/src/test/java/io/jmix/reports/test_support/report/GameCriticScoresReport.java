package io.jmix.reports.test_support.report;

import io.jmix.reports.annotation.*;
import io.jmix.reports.delegate.JsonInputProvider;
import io.jmix.reports.delegate.ParameterTransformer;
import io.jmix.reports.entity.*;
import io.jmix.reports.yarg.structure.DefaultValueProvider;

@ReportDef(
        name = "Average game critic scores from external source",
        code = GameCriticScoresReport.CODE,
        description = """
                - default value provider
                - input parameter transformer
                - JSON data set with custom input provider
                - linked data sets
                """
)
@InputParameterDef(
        alias = GameCriticScoresReport.PARAM_PUBLISHER_NAME,
        name = "Publisher name contains",
        type = ParameterType.TEXT,
        required = true
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL
)
@BandDef(
        name = "Data",
        parent = "Root",
        orientation = Orientation.HORIZONTAL,
        dataSets = {
                @DataSetDef(
                        name = "Entity",
                        type = DataSetType.JPQL,
                        query = """
                                select
                                g.name as "name",
                                g.price as "price"
                                from GameTitle g
                                where lower(g.publisher.name) like ${publisherName}
                                """
                ),
                @DataSetDef(
                        name = "External",
                        type = DataSetType.JSON,
                        linkParameterName = "name",
                        json = @JsonDataSetParameters(
                                source = JsonSourceType.DELEGATE,
                                jsonPathQuery = "$.critics[*]"
                        )
                )
        }
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.CSV,
        filePath = "io/jmix/reports/test_support/report/GameCriticScores.csv",
        isDefault = true
)
@ValueFormatDef(
        band = "Data",
        field = "price",
        format = "0.#" // no digits after zero
)
public class GameCriticScoresReport {
    public static final String CODE = "GAME_CRITICS_SCORES";

    public static final String PARAM_PUBLISHER_NAME = "publisherName";

    @InputParameterDelegate(alias = GameCriticScoresReport.PARAM_PUBLISHER_NAME)
    public DefaultValueProvider<String> publisherNameDefaultValue() {
        return (parameter) -> {
            return "acti"; // activision
        };
    }

    @InputParameterDelegate(alias = GameCriticScoresReport.PARAM_PUBLISHER_NAME)
    public ParameterTransformer<String> publisherNameTransformer() {
        return (value, params) -> {
            return "%" + value.toLowerCase() + "%"; // for jpql contains
        };
    }

    @DataSetDelegate(name = "External")
    public JsonInputProvider externalJsonProvider() {
        return (reportQuery, parentBand, reportParameters) -> {
            return """
                    {
                    "critics": [
                        {
                            "name": "Destiny",
                            "averageScore": 7.6
                        },
                        {
                            "name": "Assassin's Creed",
                            "averageScore": 8.1
                        },
                        {
                            "name": "Tetris",
                            "averageScore": 9.9
                        },
                        {
                            "name": "Mario Kart DS",
                            "averageScore": 9.1
                        },
                    ]
                    }
                    """;
        };
    }
}
