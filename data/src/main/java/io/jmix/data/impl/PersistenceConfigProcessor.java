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

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.core.impl.scanning.JpaConverterDetector;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.persistence.DbmsSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates orm.xml file.
 */
@Component("data_PersistenceConfigProcessor")
public class PersistenceConfigProcessor {

    private static final Logger log = LoggerFactory.getLogger(PersistenceConfigProcessor.class);

    private ExtendedEntities extendedEntities;
    protected DbmsSpecifics dbmsSpecifics;
    private JmixModulesClasspathScanner classpathScanner;
    protected Environment environment;
    protected Metadata metadata;

    @Autowired
    public PersistenceConfigProcessor(Environment environment, Metadata metadata, ExtendedEntities extendedEntities,
                                      DbmsSpecifics dbmsSpecifics, JmixModulesClasspathScanner classpathScanner) {
        this.environment = environment;
        this.metadata = metadata;
        this.extendedEntities = extendedEntities;
        this.dbmsSpecifics = dbmsSpecifics;
        this.classpathScanner = classpathScanner;
    }

    public void createOrmXml(String storeName, File dir) {
        List<String> classes = new ArrayList<>();

        for (MetaClass metaClass : metadata.getClasses()) {
            if (Boolean.TRUE.equals(metaClass.getAnnotations().get("jmix.orm"))
                    && metaClass.getStore().getName().equals(storeName)) {
                MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
                if (originalMetaClass != null) {
                    // add all intermediate ancestors in case of multi-level extension
                    MetaClass ancestor = metaClass.getAncestor();
                    while (ancestor != null && !ancestor.equals(originalMetaClass)) {
                        classes.add(ancestor.getJavaClass().getName());
                        ancestor = ancestor.getAncestor();
                    }
                    classes.add(originalMetaClass.getJavaClass().getName());
                }
                classes.add(metaClass.getJavaClass().getName());
            }
        }

        classes.addAll(classpathScanner.getClassNames(JpaConverterDetector.class));

        String disableOrmGenProp = environment.getProperty("jmix.disableOrmXmlGeneration");
        if (!Boolean.parseBoolean(disableOrmGenProp)) {
            Map<String, String> properties = new HashMap<>(dbmsSpecifics.getDbmsFeatures(storeName).getJpaParameters());

            MappingFileCreator mappingFileCreator =
                    new MappingFileCreator(environment, classes, properties, dir, storeName);
            mappingFileCreator.create();
        }
    }

}
