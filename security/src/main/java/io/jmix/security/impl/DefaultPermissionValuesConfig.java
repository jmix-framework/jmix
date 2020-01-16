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

package io.jmix.security.impl;

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.core.Resources;
import io.jmix.core.commons.util.Dom4j;
import io.jmix.core.security.PermissionType;
import io.jmix.security.entity.Permission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used for working with default permissions.
 * Default permission values are used when no role defines an explicit value for permission target.
 * Default permissions are loaded from the set of files defined by the {@code cuba.defaultPermissionValuesConfig} app property.
 */
@Component("cuba_DefaultPermissionValuesConfig")
public class DefaultPermissionValuesConfig {

    private final Logger log = LoggerFactory.getLogger(DefaultPermissionValuesConfig.class);

    protected Map<String, Permission> permissionValues = new HashMap<>();

    @Inject
    protected Resources resources;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Environment environment;

    @EventListener
    protected void init(ContextRefreshedEvent event) {
        log.info("Initializing default permission values");
        permissionValues.clear();

        String configName = environment.getProperty("jmix.defaultPermissionValuesConfig");
        if (!StringUtils.isBlank(configName)) {
            StringTokenizer tokenizer = new StringTokenizer(configName);
            for (String fileName : tokenizer.getTokenArray()) {
                parseConfigFile(fileName);
            }
        }
    }

    protected void parseConfigFile(String fileName) {
        String fileContent = resources.getResourceAsString(fileName);
        if (!Strings.isNullOrEmpty(fileContent)) {
            Document document = Dom4j.readDocument(fileContent);
            List<Element> permissionElements = document.getRootElement().elements("permission");

            for (Element element : permissionElements) {
                String target = element.attributeValue("target");
                Integer value = Integer.valueOf(element.attributeValue("value"));
                Integer type = Integer.valueOf(element.attributeValue("type"));
                Permission permission = metadata.create(Permission.class);
                permission.setTarget(target);
                permission.setType(PermissionType.fromId(type));
                permission.setValue(value);
                permissionValues.put(target, permission);
            }
        } else {
            log.error("File {} not found", fileName);
        }
    }

    public Map<String, Permission> getDefaultPermissionValues() {
        return new HashMap<>(permissionValues);
    }
}