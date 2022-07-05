/*
 * Copyright 2021 Haulmont.
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
import io.jmix.core.CoreProperties;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.reports.libintegration.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = DataConfiguration.class)
@PropertySource(name = "io.jmix.reports", value = "classpath:/io/jmix/reports/module.properties")
public class ReportsConfiguration {

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected ReportsProperties reportsProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Bean("report_Scripting")
    public Scripting scripting() {
        return new JmixReportingScripting();
    }

    @Bean("report_Reporting")
    public JmixReporting reporting(ReportLoaderFactory loaderFactory,
                                   JmixFormatterFactory formatterFactory,
                                   JmixDataExtractor dataExtractor,
                                   JmixObjectToStringConverter converter,
                                   Scripting scripting) {
        JmixReporting jmixReporting = new JmixReporting();
        jmixReporting.setLoaderFactory(loaderFactory);
        jmixReporting.setFormatterFactory(formatterFactory);
        jmixReporting.setDataExtractor(dataExtractor);
        jmixReporting.setObjectToStringConverter(converter);
        jmixReporting.setScripting(scripting);
        return jmixReporting;
    }

    @Bean("report_LoaderFactory")
    public ReportLoaderFactory loaderFactory(JmixSqlDataLoader sqlDataLoader,
                                             JmixGroovyDataLoader groovyDataLoader,
                                             JpqlDataLoader jpqlDataLoader,
                                             JmixJsonDataLoader jsonDataLoader,
                                             SingleEntityDataLoader singleEntityDataLoader,
                                             MultiEntityDataLoader multiEntityDataLoader) {
        DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory();
        Map<String, ReportDataLoader> dataLoaders = new HashMap<>();
        dataLoaders.put("sql", sqlDataLoader);
        dataLoaders.put("groovy", groovyDataLoader);
        dataLoaders.put("jpql", jpqlDataLoader);
        dataLoaders.put("json", jsonDataLoader);
        dataLoaders.put("single", singleEntityDataLoader);
        dataLoaders.put("multi", multiEntityDataLoader);
        loaderFactory.setDataLoaders(dataLoaders);
        return loaderFactory;
    }

    @Bean("report_SqlDataLoader")
    public JmixSqlDataLoader sqlDataLoader(SqlParametersConverter converter) {
        JmixSqlDataLoader sqlDataLoader = new JmixSqlDataLoader(dataSource);
        sqlDataLoader.setParametersConverter(converter);
        return sqlDataLoader;
    }

    @Bean("report_SqlParametersConverter")
    public SqlParametersConverter sqlParametersConverter() {
        return new SqlParametersConverter();
    }

    @Bean("report_GroovyDataLoader")
    public JmixGroovyDataLoader groovyDataLoader(Scripting scripting) {
        return new JmixGroovyDataLoader(scripting);
    }

    @Bean("report_JpqlDataLoader")
    public JpqlDataLoader jpqlDataLoader() {
        return new JpqlDataLoader();
    }
    @Bean("report_JsonDataLoader")
    public JmixJsonDataLoader jsonDataLoader(Scripting scripting) {
        return new JmixJsonDataLoader(scripting);
    }

    @Bean("report_SingleEntityDataLoader")
    public SingleEntityDataLoader singleEntityDataLoader() {
        return new SingleEntityDataLoader();
    }

    @Bean("report_MultiEntityDataLoader")
    public MultiEntityDataLoader multiEntityDataLoader() {
        return new MultiEntityDataLoader();
    }

    @Bean("report_FormatterFactory")
    public JmixFormatterFactory formatterFactory(JmixInlinersProvider inlinersProvider,
                                                 JmixFieldFormatProvider fieldFormatProvider,
                                                 Scripting scripting,
                                                 JmixOfficeIntegration officeIntegration) {
        JmixFormatterFactory formatterFactory = new JmixFormatterFactory();
        formatterFactory.setUseOfficeForDocumentConversion(reportsProperties.isUseOfficeForDocumentConversion());
        formatterFactory.setInlinersProvider(inlinersProvider);
        formatterFactory.setDefaultFormatProvider(fieldFormatProvider);
        formatterFactory.setOfficeIntegration(officeIntegration);
        formatterFactory.setScripting(scripting);
        return formatterFactory;
    }

    @Bean("report_InlinersProvider")
    public JmixInlinersProvider inlinersProvider(FileStorageContentInliner fileStorageContentInliner) {
        return new JmixInlinersProvider(fileStorageContentInliner);
    }

    @Bean("report_FieldFormatProvider")
    public JmixFieldFormatProvider fieldFormatProvider() {
        return new JmixFieldFormatProvider();
    }

    @Bean("report_OfficeIntegration")
    public JmixOfficeIntegration officeIntegration() {
        JmixOfficeIntegration officeIntegration = new JmixOfficeIntegration(reportsProperties.getOfficePath(), reportsProperties.getOfficePorts());
        officeIntegration.setDisplayDeviceAvailable(reportsProperties.getDisplayDeviceAvailable());
        officeIntegration.setTimeoutInSeconds(reportsProperties.getDocFormatterTimeout());
        officeIntegration.setTemporaryDirPath(Paths.get(coreProperties.getTempDir(), "reporting").toString());
        officeIntegration.setCountOfRetry(reportsProperties.getCountOfRetry());
        return officeIntegration;
    }

    @Bean("report_DataExtractor")
    public JmixDataExtractor dataExtractor(ReportLoaderFactory loaderFactory,
                                           DefaultExtractionControllerFactory extractionControllerFactory) {
        JmixDataExtractor jmixDataExtractor = new JmixDataExtractor(loaderFactory);
        jmixDataExtractor.setExtractionControllerFactory(extractionControllerFactory);
        return jmixDataExtractor;
    }

    @Bean("report_ExtractionControllerFactory")
    public DefaultExtractionControllerFactory extractionControllerFactory(ReportLoaderFactory loaderFactory,
                                                                          ApplicationContext applicationContext,
                                                                          DefaultPreprocessorFactory defaultPreprocessorFactory) {
        DefaultExtractionControllerFactory extractionControllerFactory = new DefaultExtractionControllerFactory(loaderFactory);
        Map<BandOrientation, ExtractionController> extractionControllers = new HashMap<>();
        extractionControllers.put(BandOrientation.CROSS, applicationContext.getBean(CrossTabExtractionController.class, extractionControllerFactory,
                loaderFactory, defaultPreprocessorFactory));
        extractionControllerFactory.setExtractionControllers(extractionControllers);
        return extractionControllerFactory;
    }

    @Bean("report_CrossTabExtractionController")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CrossTabExtractionController crossTabExtractionController(DefaultExtractionControllerFactory extractionControllerFactory,
                                                                     ReportLoaderFactory loaderFactory,
                                                                     DefaultPreprocessorFactory defaultPreprocessorFactory) {
        CrossTabExtractionController extractionController = new CrossTabExtractionController(extractionControllerFactory, loaderFactory);
        extractionController.setPreprocessorFactory(defaultPreprocessorFactory);
        return extractionController;
    }

    @Bean("report_PreprocessorFactory")
    public DefaultPreprocessorFactory preprocessorFactory(SqlCrosstabPreprocessor sqlCrosstabPreprocessor,
                                                          SqlCrosstabPreprocessor jpqlCrosstabPreprocessor) {
        DefaultPreprocessorFactory preprocessorFactory = new DefaultPreprocessorFactory();
        Map<String, QueryLoaderPreprocessor> preprocessors = new HashMap<>();
        preprocessors.put("sql", sqlCrosstabPreprocessor);
        preprocessors.put("jpql", jpqlCrosstabPreprocessor);
        preprocessorFactory.setPreprocessors(preprocessors);
        return preprocessorFactory;
    }

    @Bean("report_SqlQueryLoaderPreprocessor")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SqlCrosstabPreprocessor sqlCrosstabPreprocessor() {
        return new SqlCrosstabPreprocessor();
    }


    @Bean("report_StringConverter")
    public JmixObjectToStringConverter objectToStringConverter() {
        return new JmixObjectToStringConverter();
    }
}
