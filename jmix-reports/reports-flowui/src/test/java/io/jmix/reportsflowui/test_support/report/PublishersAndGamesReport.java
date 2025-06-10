/*
 * Copyright 2025 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.reportsflowui.test_support.report;

import io.jmix.core.Messages;
import io.jmix.reports.annotation.*;
import io.jmix.reports.delegate.ParameterValidator;
import io.jmix.reports.delegate.ParametersCrossValidator;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.exception.ReportParametersValidationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@ReportDef(
        name = "Publishers and games info",
        code = PublishersAndGamesReport.CODE,
        description = """
                Uses the following features:
                - parameter validator
                - cross-validation
                - TABLE output
                """
)
@InputParameterDef(
        alias = PublishersAndGamesReport.PARAM_START_DATE,
        name = "Start date",
        type = ParameterType.DATE,
        required = true
)
@InputParameterDef(
        alias = PublishersAndGamesReport.PARAM_END_DATE,
        name = "End date",
        type = ParameterType.DATE,
        required = true
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL
)
@BandDef(
        name = "Publishers",
        parent = "Root",
        orientation = Orientation.HORIZONTAL,
        dataSets = @DataSetDef(
                type = DataSetType.JPQL,
                query = """
                        select
                        p.name as "name",
                        count(g) as "gameCount"
                        from Publisher p, GameTitle g
                        where g.publisher = p
                        group by p.name
                        order by p.name asc
                        """
        )
)
@BandDef(
        name = "Games",
        parent = "Root",
        orientation = Orientation.HORIZONTAL,
        dataSets = @DataSetDef(
                type = DataSetType.JPQL,
                query = """
                        select
                        g.name as "name",
                        count(pg) as "purchaseCount"
                        from GameTitle g, PurchasedGame pg
                        where pg.game = g
                        and pg.purchaseDate >= ${startDate}
                        and pg.purchaseDate <= ${endDate}
                        group by g.name
                        order by g.name asc
                        """
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.TABLE,
        isDefault = true,
        table = @TemplateTableDef(
                bands = {
                        @TableBandDef(
                                bandName = "Publishers",
                                columns = {
                                        @TableColumnDef(
                                                key = "name",
                                                caption = "msg:///reports.columns.name"
                                        ),
                                        @TableColumnDef(
                                                key = "gameCount",
                                                caption = "msg://report.publishersGames.columns.gameCount"
                                        )
                                }
                        ),
                        @TableBandDef(
                                bandName = "Games",
                                columns = {
                                        @TableColumnDef(
                                                key = "name",
                                                caption = "msg:///reports.columns.name"
                                        ),
                                        @TableColumnDef(key = "purchaseCount", caption = "Purchase count")
                                }
                        )
                }
        )
)
public class PublishersAndGamesReport {
    public static final String CODE = "PUBLISHERS_AND_GAMES";
    public static final String PARAM_START_DATE = "startDate";
    public static final String PARAM_END_DATE = "endDate";

    private final Messages messages;

    public PublishersAndGamesReport(Messages messages) {
        this.messages = messages;
    }

    @InputParameterDelegate(alias = "startDate")
    public ParameterValidator<Date> startDateValidator() throws ParseException {
        Date minDate = new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01");
        return (value) -> {
            if (value.before(minDate)) {
                throw new ReportParametersValidationException(messages.getMessage(getClass(), "report.publishersGames.startDate.early"));
            }
        };
    }

    @ReportDelegate
    public ParametersCrossValidator crossValidator() {
        return (parameterValues) -> {
            Date startDate = (Date) parameterValues.get(PARAM_START_DATE);
            Date endDate = (Date) parameterValues.get(PARAM_END_DATE);
            if (startDate != null && endDate != null // may be invoked even if required parameters aren't filled
                && endDate.before(startDate)) {
                throw new ReportParametersValidationException(messages.getMessage(getClass(), "report.publishersGames.badRange"));
            }
        };
    }
}
