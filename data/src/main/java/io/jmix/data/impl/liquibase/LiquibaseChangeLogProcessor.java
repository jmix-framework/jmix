/*
 * Copyright 2020 Haulmont.
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

package io.jmix.data.impl.liquibase;

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import io.jmix.core.Stores;
import io.jmix.core.common.util.Dom4j;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component(LiquibaseChangeLogProcessor.NAME)
public class LiquibaseChangeLogProcessor {

    public static final String NAME = "data_LiquibaseChangeLogProcessor";

    private Environment environment;
    private JmixModules jmixModules;

    private static final Logger log = LoggerFactory.getLogger(LiquibaseChangeLogProcessor.class);

    @Autowired
    public LiquibaseChangeLogProcessor(Environment environment, JmixModules jmixModules) {
        this.environment = environment;
        this.jmixModules = jmixModules;
    }

    public String createMasterChangeLog(String storeName) {
        String fileName = getOutputFileName(storeName);

        List<String> moduleFiles = new ArrayList<>();

        for (JmixModuleDescriptor module : jmixModules.getAll()) {
            ClassPathResource resource = new ClassPathResource(getModuleFileName(module, storeName), getClass());
            if (resource.exists()) {
                moduleFiles.add(resource.getPath());
            }
        }

        if (!moduleFiles.isEmpty()) {
            log.debug("Found module changelogs: {}", moduleFiles);
            Document doc = DocumentFactory.getInstance().createDocument();
            Element rootElem = doc.addElement("databaseChangeLog", "http://www.liquibase.org/xml/ns/dbchangelog");
            rootElem.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElem.addAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd");

            for (String moduleFile : moduleFiles) {
                Element includeElem = rootElem.addElement("include");
                includeElem.addAttribute("file", moduleFile);
            }

            log.info("Creating file " + fileName);

            File outFile;
            try {
                outFile = new File(fileName).getCanonicalFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!outFile.getParentFile().exists()) {
                if (!outFile.getParentFile().mkdirs()) {
                    throw new RuntimeException("Cannot create directory " + outFile.getParentFile());
                }
            }
            try (OutputStream os = new FileOutputStream(outFile);
                 Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                Dom4j.writeDocument(doc, true, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.debug("Did not find changelogs in {}", jmixModules.getAll());
        }

        return fileName;
    }

    protected String getOutputFileName(String storeName) {
        String prefix = Stores.isMain(storeName) ? "" : storeName + "-";
        return environment.getProperty("jmix.core.workDir") + "/" + prefix + "liquibase-changelog.xml";
    }

    protected String getModuleFileName(JmixModuleDescriptor module, String storeName) {
        String prefix = Stores.isMain(storeName) ? "" : storeName + "-";
        return "/" + module.getBasePackage().replace('.', '/') + "/liquibase/" + prefix + "changelog.xml";
    }
}
