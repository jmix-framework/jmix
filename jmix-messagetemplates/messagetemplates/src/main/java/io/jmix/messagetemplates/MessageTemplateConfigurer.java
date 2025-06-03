/*
 * Copyright 2025 Haulmont.
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

package io.jmix.messagetemplates;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerProperties;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerVariablesCustomizer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Factory that configures a FreeMarker {@link Configuration}.
 */
public class MessageTemplateConfigurer extends FreeMarkerConfigurer {

    protected MessageTemplateProperties messageTemplateProperties;
    protected FreeMarkerProperties freeMarkerProperties;
    protected List<FreeMarkerVariablesCustomizer> variablesCustomizers;

    public MessageTemplateConfigurer(MessageTemplateProperties messageTemplateProperties,
                                     FreeMarkerProperties freeMarkerProperties,
                                     ObjectProvider<FreeMarkerVariablesCustomizer> variablesCustomizers) {
        this.messageTemplateProperties = messageTemplateProperties;
        this.freeMarkerProperties = freeMarkerProperties;
        this.variablesCustomizers = variablesCustomizers.orderedStream().toList();
    }

    @Override
    public void afterPropertiesSet() throws IOException, TemplateException {
        applyProperties();

        super.afterPropertiesSet();
    }

    protected void applyProperties() {
        setTemplateLoaderPaths(freeMarkerProperties.getTemplateLoaderPath());
        setPreferFileSystemAccess(freeMarkerProperties.isPreferFileSystemAccess());
        setDefaultEncoding(freeMarkerProperties.getCharsetName());

        setFreemarkerSettings(createFreeMarkerSettings());
        setFreemarkerVariables(createFreeMarkerVariables());
    }

    protected Properties createFreeMarkerSettings() {
        Properties settings = new Properties();
        settings.put("recognize_standard_file_extensions", "true");
        settings.putAll(freeMarkerProperties.getSettings());
        return settings;
    }

    protected Map<String, Object> createFreeMarkerVariables() {
        Map<String, Object> variables = new HashMap<>();
        for (FreeMarkerVariablesCustomizer customizer : variablesCustomizers) {
            customizer.customizeFreeMarkerVariables(variables);
        }
        return variables;
    }

    @Override
    protected Configuration newConfiguration() {
        return new Configuration(messageTemplateProperties.getFreemarkerVersion());
    }
}
