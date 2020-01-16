/*
 * Copyright 2019 Haulmont.
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

package io.jmix.data.impl;

import com.google.common.base.Strings;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.data.persistence.PersistenceXmlPostProcessor;
import io.jmix.core.EnvironmentUtils;
import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.core.commons.util.Dom4j;
import io.jmix.core.commons.util.ReflectionHelper;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import javax.persistence.Entity;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a working persistence.xml file combining classes and properties from a set of given persistence.xml files,
 * defined in <code>cuba.persistenceConfig</code> app property.
 */
public class PersistenceConfigProcessor {

    private static final Logger log = LoggerFactory.getLogger(PersistenceConfigProcessor.class);

    protected String fileName;
    private DbmsSpecifics dbmsSpecifics;
    protected String storeName;

    protected Environment environment;
    protected Metadata metadata;

    public PersistenceConfigProcessor(Environment environment, Metadata metadata,
                                      DbmsSpecifics dbmsSpecifics, String storeName, String fileName) {
        this.environment = environment;
        this.metadata = metadata;
        this.dbmsSpecifics = dbmsSpecifics;

        this.storeName = storeName;
        this.fileName = fileName;
    }

    public void setOutputFile(String file) {
        fileName = file;
    }

    public void setStorageName(String storeName) {
        this.storeName = storeName;
    }

    public void create() {
        if (StringUtils.isBlank(fileName))
            throw new IllegalStateException("Output file not set");

        List<String> classes = metadata.getClasses().stream()
                .filter(metaClass -> Boolean.TRUE.equals(metaClass.getAnnotations().get("jmix.orm")))
                .map(metaClass -> metaClass.getJavaClass().getName())
                .collect(Collectors.toList());

        Map<String, String> properties = new HashMap<>();

        properties.putAll(dbmsSpecifics.getDbmsFeatures(storeName).getJpaParameters());

//        for (String fileName : sourceFileNames) {
//            Document doc = getDocument(fileName);
//            Element puElem = findPersistenceUnitElement(doc.getRootElement());
//            if (puElem == null)
//                throw new IllegalStateException("No persistence unit named 'cuba' found among multiple units inside " + fileName);
//            addClasses(puElem, classes);
//            addProperties(puElem, properties);
//        }

        for (String name : EnvironmentUtils.getPropertyNames(environment)) {
            if (name.startsWith("eclipselink.")) {
                properties.put(name, environment.getProperty(name));
            }
        }

        if (!Stores.isMain(storeName))
            properties.put(PersistenceSupport.PROP_NAME, storeName);

        File outFile;
        try {
            outFile = new File(fileName).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        outFile.getParentFile().mkdirs();

        boolean ormXmlCreated = true;
        String disableOrmGenProp = environment.getProperty("cuba.disableOrmXmlGeneration");
        if (!Boolean.parseBoolean(disableOrmGenProp)) {
            MappingFileCreator mappingFileCreator =
                    new MappingFileCreator(environment, classes, properties, outFile.getParentFile());
            ormXmlCreated = mappingFileCreator.create();
        }

//        String fileName = sourceFileNames.get(sourceFileNames.size() - 1);
//        Document doc = getDocument(fileName);
//        Element rootElem = doc.getRootElement();
//
//        Element puElem = findPersistenceUnitElement(rootElem);
//        if (puElem == null)
//            throw new IllegalStateException("No persistence unit named 'cuba' found among multiple units inside " + fileName);

        Document doc = DocumentFactory.getInstance().createDocument();
        Element rootElem = doc.addElement("persistence", "http://java.sun.com/xml/ns/persistence");
        rootElem.addAttribute("version", "2.0");
        rootElem.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootElem.addAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd");

        Element puElem = rootElem.addElement("persistence-unit");
        puElem.addAttribute("name", "jmix");

        puElem.addElement("provider").setText("org.eclipse.persistence.jpa.PersistenceProvider");

        if (ormXmlCreated) {
            puElem.addElement("mapping-file").setText("orm.xml");
        }

        for (String className : classes) {
            puElem.addElement("class").setText(className);
        }

        puElem.addElement("exclude-unlisted-classes");

        Element propertiesEl = puElem.element("properties");
        if (propertiesEl != null)
            puElem.remove(propertiesEl);

        propertiesEl = puElem.addElement("properties");
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            Element element = propertiesEl.addElement("property");
            element.addAttribute("name", entry.getKey());
            element.addAttribute("value", entry.getValue());
        }

        postProcess(doc);

        log.info("Creating file " + outFile);
        try (OutputStream os = new FileOutputStream(fileName);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            Dom4j.writeDocument(doc, true, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void postProcess(Document document) {
        String postProcessorClassName = environment.getProperty("cuba.persistenceXmlPostProcessor");

        if (!Strings.isNullOrEmpty(postProcessorClassName)) {
            log.debug("Running persistence.xml post-processor: " + postProcessorClassName);
            try {
                Class processorClass = ReflectionHelper.loadClass(postProcessorClassName);
                PersistenceXmlPostProcessor processor = (PersistenceXmlPostProcessor) processorClass.newInstance();
                processor.process(document);
            } catch (Exception e) {
                throw new RuntimeException("Error post-processing persistence.xml", e);
            }
        }
    }

    private void addClasses(Element puElem, Map<String, String> classes) {
        for (Element element : puElem.elements("class")) {
            String className = element.getText();
            Class<Object> cls = ReflectionHelper.getClass(className);
            Entity annotation = cls.getAnnotation(Entity.class);
            if (annotation != null) {
                classes.put(annotation.name(), className);
            } else {
                classes.put(className, className);
            }
        }
    }

    private void addProperties(Element puElem, Map<String, String> properties) {
        Element propertiesEl = puElem.element("properties");
        if (propertiesEl != null) {
            for (Element element : propertiesEl.elements("property")) {
                properties.put(element.attributeValue("name"), element.attributeValue("value"));
            }
        }
    }

    private Element findPersistenceUnitElement(Element rootElem) {
        List<Element> puList = rootElem.elements("persistence-unit");
        if (puList.size() == 1) {
            return puList.get(0);
        } else {
            for (Element element : puList) {
                if ("cuba".equals(element.attributeValue("name"))) {
                    return element;
                }
            }
        }
        return null;
    }
}
