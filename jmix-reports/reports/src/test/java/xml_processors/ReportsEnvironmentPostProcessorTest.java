/*
 * Copyright 2026 Haulmont.
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

package xml_processors;

import io.jmix.reports.libintegration.ReportsEnvironmentPostProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Reports dependencies must not replace the XML processors that other libraries of the application get from
 * JAXP. Hazelcast, for example, cannot read its XML configuration with a processor that does not support the
 * JAXP 1.5 external access properties.
 */
public class ReportsEnvironmentPostProcessorTest {

    private static final String TRANSFORMER_FACTORY_PROPERTY = TransformerFactory.class.getName();
    private static final String SCHEMA_FACTORY_PROPERTY =
            SchemaFactory.class.getName() + ":" + XMLConstants.W3C_XML_SCHEMA_NS_URI;
    private static final String DOCX4J_TRANSFORMER_FACTORY = "org.docx4j.org.apache.xalan.processor.TransformerFactoryImpl";
    private static final String XERCES_SCHEMA_FACTORY = "org.apache.xerces.jaxp.validation.XMLSchemaFactory";

    private final ReportsEnvironmentPostProcessor postProcessor = new ReportsEnvironmentPostProcessor(new DeferredLogs());
    // Created here because SpringApplication uses the thread context class loader, which some tests replace
    private final MockEnvironment environment = new MockEnvironment();
    private final SpringApplication application = new SpringApplication();

    private final Map<String, String> savedProperties = new HashMap<>();

    @BeforeEach
    void setUp() {
        for (String property : List.of(TRANSFORMER_FACTORY_PROPERTY, SCHEMA_FACTORY_PROPERTY)) {
            String value = System.getProperty(property);
            if (value != null) {
                savedProperties.put(property, value);
            }
            System.clearProperty(property);
        }
    }

    @AfterEach
    void tearDown() {
        for (String property : List.of(TRANSFORMER_FACTORY_PROPERTY, SCHEMA_FACTORY_PROPERTY)) {
            String value = savedProperties.get(property);
            if (value == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, value);
            }
        }
    }

    @Test
    void testDefaultFactoriesSupportExternalAccessPropertiesInApplication() throws Exception {
        try (ConfigurableApplicationContext ignored = new SpringApplicationBuilder(EmptyConfiguration.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run()) {
            assertThat(System.getProperty(TRANSFORMER_FACTORY_PROPERTY))
                    .isEqualTo(TransformerFactory.newDefaultInstance().getClass().getName());
            assertThat(System.getProperty(SCHEMA_FACTORY_PROPERTY))
                    .isEqualTo(SchemaFactory.newDefaultInstance().getClass().getName());

            // Hazelcast enables XXE protection this way when it reads its XML configuration
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        }
    }

    @Test
    void testConfiguredTransformerFactoryIsKept() {
        System.setProperty(TRANSFORMER_FACTORY_PROPERTY, DOCX4J_TRANSFORMER_FACTORY);

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(System.getProperty(TRANSFORMER_FACTORY_PROPERTY)).isEqualTo(DOCX4J_TRANSFORMER_FACTORY);
    }

    @Test
    void testConfiguredSchemaFactoryIsKept() {
        System.setProperty(SCHEMA_FACTORY_PROPERTY, XERCES_SCHEMA_FACTORY);

        postProcessor.postProcessEnvironment(environment, application);

        assertThat(System.getProperty(SCHEMA_FACTORY_PROPERTY)).isEqualTo(XERCES_SCHEMA_FACTORY);
    }

    @Test
    void testDocx4jTransformerFactoryIsReplaced() throws Exception {
        URL docx4jXalan = Class.forName(DOCX4J_TRANSFORMER_FACTORY).getProtectionDomain().getCodeSource().getLocation();

        // As in an application without Apache Xalan: the docx4j copy is the only registered provider
        withContextClassLoader(() -> {
            postProcessor.postProcessEnvironment(environment, application);

            assertThat(System.getProperty(TRANSFORMER_FACTORY_PROPERTY))
                    .isEqualTo(TransformerFactory.newDefaultInstance().getClass().getName());
        }, docx4jXalan);
    }

    @Test
    void testTransformerFactoryOfAnotherLibraryIsKept(@TempDir Path servicesDirectory) throws Exception {
        URL registration = registerProvider(servicesDirectory, TransformerFactory.class,
                OtherLibraryTransformerFactory.class.getName());
        URL testClasses = OtherLibraryTransformerFactory.class.getProtectionDomain().getCodeSource().getLocation();

        withContextClassLoader(() -> {
            postProcessor.postProcessEnvironment(environment, application);

            assertThat(System.getProperty(TRANSFORMER_FACTORY_PROPERTY)).isNull();
            assertThat(TransformerFactory.newInstance().getClass().getName())
                    .isEqualTo(OtherLibraryTransformerFactory.class.getName());
        }, registration, testClasses);
    }

    @Test
    void testBrokenFactoryRegistrationsDoNotFailStartup(@TempDir Path servicesDirectory) throws Exception {
        URL registrations = registerProvider(servicesDirectory, TransformerFactory.class,
                "com.company.MissingTransformerFactory");
        registerProvider(servicesDirectory, SchemaFactory.class, "com.company.MissingSchemaFactory");

        withContextClassLoader(() -> {
            postProcessor.postProcessEnvironment(environment, application);

            assertThat(System.getProperty(TRANSFORMER_FACTORY_PROPERTY)).isNull();
            assertThat(System.getProperty(SCHEMA_FACTORY_PROPERTY)).isNull();
        }, registrations);
    }

    @Test
    void testFactoryWithMissingDependencyDoesNotFailStartup(@TempDir Path servicesDirectory) throws Exception {
        URL registration = registerProvider(servicesDirectory, TransformerFactory.class,
                XalanBasedTransformerFactory.class.getName());
        URL testClasses = XalanBasedTransformerFactory.class.getProtectionDomain().getCodeSource().getLocation();

        // Apache Xalan, which the factory extends, is missing, so the factory class cannot be linked
        withContextClassLoader(() -> {
            postProcessor.postProcessEnvironment(environment, application);

            assertThat(System.getProperty(TRANSFORMER_FACTORY_PROPERTY)).isNull();
        }, registration, testClasses);
    }

    private URL registerProvider(Path servicesDirectory, Class<?> service, String provider) throws IOException {
        Path servicesFile = servicesDirectory.resolve("META-INF/services/" + service.getName());
        Files.createDirectories(servicesFile.getParent());
        Files.writeString(servicesFile, provider);
        return servicesDirectory.toUri().toURL();
    }

    /**
     * Runs the action while the JAXP service lookup sees only the given classpath.
     */
    private void withContextClassLoader(Runnable action, URL... classpath) throws IOException {
        Thread thread = Thread.currentThread();
        ClassLoader contextClassLoader = thread.getContextClassLoader();
        try (URLClassLoader classLoader = new URLClassLoader(classpath, ClassLoader.getPlatformClassLoader())) {
            thread.setContextClassLoader(classLoader);
            action.run();
        } finally {
            thread.setContextClassLoader(contextClassLoader);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class EmptyConfiguration {
    }
}
