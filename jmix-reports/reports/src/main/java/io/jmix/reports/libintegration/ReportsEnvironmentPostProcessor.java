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

package io.jmix.reports.libintegration;

import org.apache.commons.logging.Log;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.log.LogMessage;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.SchemaFactoryConfigurationError;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Keeps the JDK {@link TransformerFactory} and {@link SchemaFactory} as the default ones of the application.
 * <p>
 * The Reports dependencies register old XML processors as JAXP service providers: Xalan, including the copy
 * repackaged by docx4j, as the {@code TransformerFactory} and Xerces as the {@code SchemaFactory}. The registrations
 * make them the default factories of the whole application, although docx4j creates its Xalan factory by class name.
 * These processors do not support the JAXP 1.5 external access properties, so libraries that set them fail, for
 * example Hazelcast when it reads its XML configuration.
 * <p>
 * When such a processor is the default factory and the corresponding system property is not set, the property is set
 * to the JDK factory. This also replaces a processor set in a JAXP configuration file, such as {@code jaxp.properties},
 * because the system property takes precedence over the file. The post-processor runs before the application context
 * is created, so the property is in place before any bean uses JAXP. The JDK factory is used rather than another
 * registered provider because the property applies to the whole JVM, and only the JDK factory is available to every
 * application in it. To keep another factory, set the system property explicitly.
 */
public class ReportsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String TRANSFORMER_FACTORY_PROPERTY = TransformerFactory.class.getName();
    private static final String SCHEMA_FACTORY_PROPERTY =
            SchemaFactory.class.getName() + ":" + XMLConstants.W3C_XML_SCHEMA_NS_URI;

    private static final Set<String> LEGACY_TRANSFORMER_FACTORIES = Set.of(
            "org.apache.xalan.processor.TransformerFactoryImpl",
            "org.docx4j.org.apache.xalan.processor.TransformerFactoryImpl");
    private static final Set<String> LEGACY_SCHEMA_FACTORIES = Set.of(
            "org.apache.xerces.jaxp.validation.XMLSchemaFactory");

    private final Log log;

    public ReportsEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(ReportsEnvironmentPostProcessor.class);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        replaceLegacyFactory(TRANSFORMER_FACTORY_PROPERTY, LEGACY_TRANSFORMER_FACTORIES,
                TransformerFactory::newInstance, TransformerFactory::newDefaultInstance);
        replaceLegacyFactory(SCHEMA_FACTORY_PROPERTY, LEGACY_SCHEMA_FACTORIES,
                () -> SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI), SchemaFactory::newDefaultInstance);
    }

    private void replaceLegacyFactory(String property, Set<String> legacyFactories,
                                      Supplier<?> defaultFactory, Supplier<?> jdkFactory) {
        if (System.getProperty(property) != null) {
            return;
        }

        String factory;
        try {
            factory = defaultFactory.get().getClass().getName();
        } catch (TransformerFactoryConfigurationError | SchemaFactoryConfigurationError | LinkageError e) {
            // JAXP does not wrap the error when a registered provider class cannot be linked
            log.debug(LogMessage.format("Cannot get the default factory for '%s': %s", property, e));
            return;
        }
        if (!legacyFactories.contains(factory)) {
            return;
        }

        String jdkFactoryName = jdkFactory.get().getClass().getName();
        log.debug(LogMessage.format("Setting system property '%s' to '%s' instead of '%s'",
                property, jdkFactoryName, factory));
        System.setProperty(property, jdkFactoryName);
    }
}
