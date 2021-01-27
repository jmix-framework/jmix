package io.jmix.reports;

import com.haulmont.yarg.loaders.QueryLoaderPreprocessor;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.factory.ReportLoaderFactory;
import com.haulmont.yarg.reporting.extraction.DefaultExtractionControllerFactory;
import com.haulmont.yarg.reporting.extraction.DefaultPreprocessorFactory;
import com.haulmont.yarg.reporting.extraction.ExtractionController;
import com.haulmont.yarg.reporting.extraction.controller.CrossTabExtractionController;
import com.haulmont.yarg.reporting.extraction.preprocessor.SqlCrosstabPreprocessor;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.util.groovy.Scripting;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.CoreProperties;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.reports.libintegration.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {CoreConfiguration.class, DataConfiguration.class})
@PropertySource(name = "io.jmix.reports", value = "classpath:/io/jmix/reports/module.properties")
public class ReportsConfiguration {

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected ReportsProperties reportsProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected Reports reports;

    @Bean("reporting_lib_Scripting")
    public Scripting scripting() {
        return new JmixReportingScripting();
    }

    @Bean("reporting_lib_SqlParametersConverter")
    public SqlParametersConverter sqlParametersConverter() {
        return new SqlParametersConverter();
    }

    @Bean("reporting_lib_JpqlParametersConverter")
    public JpqlParametersConverter jpqlParametersConverter() {
        return new JpqlParametersConverter();
    }

    @Bean("reporting_lib_GroovyDataLoader")
    public JmixGroovyDataLoader groovyDataLoader() {
        return new JmixGroovyDataLoader(scripting());
    }

    @Bean("reporting_lib_JsonDataLoader")
    public JmixJsonDataLoader jsonDataLoader() {
        return new JmixJsonDataLoader(scripting());
    }

    @Bean("reporting_lib_SqlDataLoader")
    public ReportDataLoader sqlDataLoader() {
        JmixSqlDataLoader sqlDataLoader = new JmixSqlDataLoader(dataSource);
        sqlDataLoader.setParametersConverter(sqlParametersConverter());
        return sqlDataLoader;
    }

    @Bean("reporting_lib_JpqlDataLoader")
    public JpqlDataDataLoader jpqlDataDataLoader() {
        JpqlDataDataLoader jpqlDataDataLoader = new JpqlDataDataLoader();
        jpqlDataDataLoader.setParametersConverter(jpqlParametersConverter());
        return jpqlDataDataLoader;
    }

    @Bean("reporting_lib_SingleEntityDataLoader")
    public SingleEntityDataLoader singleEntityDataLoader() {
        return new SingleEntityDataLoader();
    }

    @Bean("reporting_lib_MultiEntityDataLoader")
    public MultiEntityDataLoader multiEntityDataLoader() {
        return new MultiEntityDataLoader();
    }

    @Bean("reporting_lib_OfficeIntegration")
    public JmixOfficeIntegration officeIntegration() {
        JmixOfficeIntegration officeIntegration = new JmixOfficeIntegration(reportsProperties.getOfficePath(), reportsProperties.getOfficePorts());
        officeIntegration.setDisplayDeviceAvailable(reportsProperties.getDisplayDeviceAvailable());
        officeIntegration.setTimeoutInSeconds(reportsProperties.getDocFormatterTimeout());
        officeIntegration.setTemporaryDirPath(Paths.get(coreProperties.getTempDir(), "reporting").toString());
        officeIntegration.setCountOfRetry(reportsProperties.getCountOfRetry());
        return officeIntegration;
    }

    @Bean("reporting_lib_JmixFieldFormatProvider")
    public JmixFieldFormatProvider fieldFormatProvider() {
        return new JmixFieldFormatProvider();
    }

    @Bean("reporting_lib_InlinersProvider")
    public JmixInlinersProvider inlinersProvider() {
        return new JmixInlinersProvider();
    }

    @Bean("reporting_lib_FormatterFactory")
    public JmixFormatterFactory formatterFactory() {
        JmixFormatterFactory formatterFactory = new JmixFormatterFactory();
        formatterFactory.setUseOfficeForDocumentConversion(reportsProperties.isUseOfficeForDocumentConversion());
        formatterFactory.setInlinersProvider(inlinersProvider());
        formatterFactory.setDefaultFormatProvider(fieldFormatProvider());
        formatterFactory.setOfficeIntegration(officeIntegration());
        formatterFactory.setScripting(scripting());
        return formatterFactory;
    }

    @Bean("reporting_lib_LoaderFactory")
    public ReportLoaderFactory loaderFactory() {
        DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory();
        Map<String, ReportDataLoader> dataLoaders = new HashMap<>();
        dataLoaders.put("sql", sqlDataLoader());
        dataLoaders.put("groovy", groovyDataLoader());
        dataLoaders.put("jpql", jpqlDataDataLoader());
        dataLoaders.put("json", jsonDataLoader());
        dataLoaders.put("single", singleEntityDataLoader());
        dataLoaders.put("multi", multiEntityDataLoader());
        loaderFactory.setDataLoaders(dataLoaders);
        return loaderFactory;
    }

    @Bean("reporting_lib_SqlQueryLoaderPreprocessor")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SqlCrosstabPreprocessor sqlCrosstabPreprocessor() {
        return new SqlCrosstabPreprocessor();
    }

    @Bean("reporting_lib_PreprocessorFactory")
    public DefaultPreprocessorFactory preprocessorFactory() {
        DefaultPreprocessorFactory preprocessorFactory = new DefaultPreprocessorFactory();
        Map<String, QueryLoaderPreprocessor> preprocessors = new HashMap<>();
        preprocessors.put("sql", sqlCrosstabPreprocessor());
        preprocessors.put("jpql", sqlCrosstabPreprocessor());
        preprocessorFactory.setPreprocessors(preprocessors);
        return preprocessorFactory;
    }

    @Bean("reporting_lib_ExtractionControllerFactory")
    public DefaultExtractionControllerFactory extractionControllerFactory() {
        DefaultExtractionControllerFactory extractionControllerFactory = new DefaultExtractionControllerFactory(loaderFactory());
        Map<BandOrientation, ExtractionController> extractionControllers = new HashMap<>();
        extractionControllers.put(BandOrientation.CROSS, crossTabExtractionController(extractionControllerFactory));
        extractionControllerFactory.setExtractionControllers(extractionControllers);
        return extractionControllerFactory;
    }

    @Bean("reporting_lib_CrossTabExtractionController")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CrossTabExtractionController crossTabExtractionController(DefaultExtractionControllerFactory extractionControllerFactory) {
        CrossTabExtractionController extractionController = new CrossTabExtractionController(extractionControllerFactory, loaderFactory());
        extractionController.setPreprocessorFactory(preprocessorFactory());
        return extractionController;
    }

    @Bean("reporting_lib_DataExtractor")
    public JmixDataExtractor dataExtractor() {
        JmixDataExtractor jmixDataExtractor = new JmixDataExtractor(loaderFactory());
        jmixDataExtractor.setExtractionControllerFactory(extractionControllerFactory());
        return jmixDataExtractor;
    }

    @Bean("reporting_lib_StringConverter")
    public JmixObjectToStringConverter objectToStringConverter() {
        return new JmixObjectToStringConverter();
    }

    @Bean("reporting_lib_Reporting")
    public JmixReporting reporting() {
        JmixReporting jmixReporting = new JmixReporting();
        jmixReporting.setLoaderFactory(loaderFactory());
        jmixReporting.setFormatterFactory(formatterFactory());
        jmixReporting.setDataExtractor(dataExtractor());
        jmixReporting.setObjectToStringConverter(objectToStringConverter());
        jmixReporting.setScripting(scripting());
        jmixReporting.setReports(reports);
        return jmixReporting;
    }


    //TODO ReportExceptionHandler

    //TODO create JMX beans
//    <!-- MBeans registration -->
//    <bean id="reports_MBeanExporter" class="com.haulmont.cuba.core.sys.jmx.MBeanExporter" lazy-init="false">
//        <property name="beans">
//            <map>
//                <entry key="${cuba.webContextName}.reports:type=CubaOfficeIntegration"
//    value-ref="reporting_lib_OfficeIntegration"/>
//                <entry key="${cuba.webContextName}.reports:type=ReportImportExport"
//    value="reporting_ReportImportExport"/>
//                <entry key="${cuba.webContextName}.reports:type=ReportingMigrator"
//    value="reporting_ReportingMigrator"/>
//            </map>
//        </property>
//    </bean>

}
