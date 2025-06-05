package io.jmix.reports.test_support.report;

import io.jmix.core.FetchPlan;
import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.test_support.entity.UserRegistration;

@ReportDef(
        name = "Users, their purchases and achievements",
        code = UsersAndAchievementsReport.CODE,
        description = """
                Uses the following features:
                - 'list of entities' input parameter
                - MULTI data set
                - SQL data set
                - JPQL data set
                - 2 levels of nested bands
                - static value format
                - XLSX output
                """
)
@InputParameterDef(
        alias = UsersAndAchievementsReport.PARAM_USERS,
        name = "User registrations",
        type = ParameterType.ENTITY_LIST,
        required = true,
        entity = @EntityParameterDef(entityClass = UserRegistration.class)
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL
)
@BandDef(
        name = "TableHeader",
        parent = "Root",
        orientation = Orientation.HORIZONTAL
)
@BandDef(
        name = "Users",
        parent = "Root",
        orientation = Orientation.HORIZONTAL,
        dataSets = @DataSetDef(
                type = DataSetType.MULTI,
                entity = @EntityDataSetDef(
                        parameterAlias = "users",
                        fetchPlanName = FetchPlan.BASE
                )
        )
)
@BandDef(
        name = "Games",
        parent = "Users",
        orientation = Orientation.HORIZONTAL,
        dataSets = @DataSetDef(
                type = DataSetType.JPQL,
                query = """
                    select
                    pg.id as "purchaseId",
                    g.name as "game"
                    from PurchasedGame pg join pg.game g
                    where pg.user.id = ${Users.id}
                    order by g.name asc
                    """
        )
)
@BandDef(
        name = "Achievements",
        parent = "Games",
        orientation = Orientation.HORIZONTAL,
        dataSets = @DataSetDef(
                type = DataSetType.SQL,
                query = """
                    select
                    a.NAME as "achievement",
                    ua.DATE_ as "earnedDate"
                    from USER_ACHIEVEMENT ua
                    join ACHIEVEMENT a on ua.ACHIEVEMENT_ID = a.ID
                    where ua.PURCHASED_GAME_ID = ${Games.purchaseId}
                    order by ua.DATE_ asc
                    """
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.XLSX,
        filePath = "io/jmix/reports/test_support/report/UsersAndAchievements.xlsx",
        isDefault = true
)
@ValueFormatDef(
        band = "Achievements",
        field = "earnedDate",
        format = "dd.MM.yyyy HH:mm"
)
public class UsersAndAchievementsReport {
        public static final String CODE = "USERS_AND_ACHIEVEMENTS";
        public static final String PARAM_USERS = "users";
}
