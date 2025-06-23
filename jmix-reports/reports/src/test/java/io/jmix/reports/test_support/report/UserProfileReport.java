package io.jmix.reports.test_support.report;

import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.reports.annotation.*;
import io.jmix.reports.delegate.FetchPlanProvider;
import io.jmix.reports.entity.*;
import io.jmix.reports.test_support.entity.UserRegistration;
import io.jmix.reports.yarg.formatters.CustomReport;
import io.jmix.reports.yarg.structure.BandData;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ReportDef(
        name = "User profile",
        code = UserProfileReport.CODE,
        description = """
                Uses the following features:
                - required input parameter
                - MULTI data set with nested collection attribute and fetch plan provider
                - custom template
                - html template with freemarker engine
                - html template with groovy engine
                """
)
@InputParameterDef(
        alias = UserProfileReport.PARAM_USER,
        name = "User",
        type = ParameterType.ENTITY,
        required = true,
        entity = @EntityParameterDef(entityClass = UserRegistration.class)
)
@BandDef(
        name = "Root",
        root = true
)
@BandDef(
        name = "UserData",
        parent = "Root",
        dataSets = @DataSetDef(
                type = DataSetType.SINGLE,
                entity = @EntityDataSetDef(
                        parameterAlias = UserProfileReport.PARAM_USER,
                        fetchPlanName = FetchPlan.BASE
                )
        )
)
@BandDef(
        name = "GameData",
        parent = "UserData",
        dataSets = @DataSetDef(
                name = "GameData",
                type = DataSetType.MULTI,
                entity = @EntityDataSetDef(
                        parameterAlias = UserProfileReport.PARAM_USER,
                        nestedCollectionAttribute = "games"
                )
        )
)
@TemplateDef(
        code = "xml",
        outputType = ReportOutputType.CUSTOM,
        isDefault = true,
        outputNamePattern = "User profile.xml",
        custom = @CustomTemplateParameters(
                enabled = true,
                definedBy = CustomTemplateDefinedBy.DELEGATE
        )
)
@TemplateDef(
        code = UserProfileReport.TEMPLATE_HTML_FREEMARKER,
        outputType = ReportOutputType.HTML,
        templateEngine = TemplateMarkupEngine.FREEMARKER,
        filePath = "io/jmix/reports/test_support/report/UserProfileFreeMarker.html"
)
@TemplateDef(
        code = UserProfileReport.TEMPLATE_HTML_GROOVY,
        outputType = ReportOutputType.HTML,
        templateEngine = TemplateMarkupEngine.GROOVY,
        filePath = "io/jmix/reports/test_support/report/UserProfileGroovy.html"
)
public class UserProfileReport {
    public static final String CODE = "USER_PROFILE";
    public static final String PARAM_USER = "user";

    public static final String TEMPLATE_HTML_FREEMARKER = "html-freemarker";
    public static final String TEMPLATE_HTML_GROOVY = "html-groovy";

    private final FetchPlans fetchPlans;
    private final MetadataTools metadataTools;
    private final DatatypeRegistry datatypeRegistry;

    public UserProfileReport(FetchPlans fetchPlans, MetadataTools metadataTools, DatatypeRegistry datatypeRegistry) {
        this.fetchPlans = fetchPlans;
        this.metadataTools = metadataTools;
        this.datatypeRegistry = datatypeRegistry;
    }

    @DataSetDelegate(name = "GameData")
    public FetchPlanProvider gameDataFetchPlan() {
        return () -> fetchPlans.builder(UserRegistration.class)
                .add("games", purchasedGame -> {
                    purchasedGame
                            .add("game", FetchPlan.INSTANCE_NAME)
                            .add("purchaseDate")
                            .add("userRating");
                })
                .build();
    }

    @TemplateDelegate(code = "xml")
    public CustomReport customTemplate() {
        return (report, rootBand, params) -> {
            return renderXml(rootBand);
        };
    }

    private byte[] renderXml(BandData rootBand) {
        Document document = DocumentHelper.createDocument();
        document.addElement("Profile");

        for (BandData bandData : rootBand.getChildrenList()) {
            renderUserData(document.getRootElement(), bandData);
        }

        return convertToByteArray(document);
    }

    private void renderUserData(Element rootElement, BandData bandData) {
        Map<String, Object> data = bandData.getData();
        Element userElement = rootElement.addElement("User");

        for (String attr: List.of("username", "firstName", "lastName", "registrationDate")) {
            Object obj = data.get(attr);
            String formattedValue;
            if (obj instanceof LocalDateTime) {
                formattedValue = datatypeRegistry.get(LocalDateTime.class).format(obj);
            } else {
                formattedValue = String.valueOf(obj);
            }
            userElement.addAttribute(attr, formattedValue);
        }

        for (BandData childBandData : bandData.getChildrenList()) {
            renderGame(userElement, childBandData);
        }
    }

    private void renderGame(Element rootElement, BandData bandData) {
        if (bandData.getData().isEmpty() && bandData.getChildrenBands().isEmpty()) {
            return;
        }
        Element bandElement = rootElement.addElement("Purchase");

        Map<String, Object> data = bandData.getData();
        for (String attr: List.of("game", "purchaseDate", "userRating")) {
            Object obj = data.get(attr);
            if (obj != null) {
                String formattedValue;
                if (obj instanceof LocalDateTime) {
                    formattedValue = datatypeRegistry.get(LocalDateTime.class).format(obj);
                } else if (obj instanceof Integer) {
                    formattedValue = datatypeRegistry.get(Integer.class).format(obj);
                } else if (obj instanceof Entity) {
                    formattedValue = metadataTools.getInstanceName(obj);
                } else {
                    formattedValue = String.valueOf(obj);
                }
                bandElement.addAttribute(attr, formattedValue);
            }
        }
    }

    private byte[] convertToByteArray(Document document) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndentSize(2);
        format.setSuppressDeclaration(true);
        XMLWriter writer;
        try {
            writer = new XMLWriter(baos, format);
            writer.write(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
}
