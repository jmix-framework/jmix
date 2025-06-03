/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplates.impl;

import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.MapModel;
import freemarker.template.*;
import io.jmix.core.DataManager;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.messagetemplates.MessageTemplateProperties;
import io.jmix.messagetemplates.MessageTemplatesGenerator;
import io.jmix.messagetemplates.entity.MessageTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("msgtmp_MessageTemplatesImpl")
public class MessageTemplatesGeneratorImpl implements MessageTemplatesGenerator, InitializingBean {

    protected DataManager dataManager;
    protected Version version;
    protected ObjectWrapper wrapper;

    protected Configuration defaultConfiguration;

    public MessageTemplatesGeneratorImpl(DataManager dataManager,
                                         MessageTemplateProperties messageTemplateProperties,
                                         Configuration configuration) {
        this.dataManager = dataManager;
        this.version = messageTemplateProperties.getFreemarkerVersion();
        this.defaultConfiguration = configuration;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeansWrapper beansWrapper = new BeansWrapper(version);
        beansWrapper.wrap(TemplateScalarModel.EMPTY_STRING);

        wrapper = new DefaultObjectWrapper(version) {
            @Override
            public TemplateModel wrap(Object obj) throws TemplateModelException {
                if (obj instanceof Map<?, ?> map) {
                    return new MapModel(map, beansWrapper);
                }

                return super.wrap(obj);
            }
        };
    }

    @Override
    public SingleTemplateGenerator generateSingleTemplate() {
        return new SingleTemplateGeneratorImpl();
    }

    @Override
    public MultiTemplateGenerator generateMultiTemplate() {
        return new MultiTemplateGeneratorImpl();
    }

    @Override
    public MultiParamTemplateGenerator generateMultiParamTemplate() {
        return new MultiParamTemplateGeneratorImpl();
    }

    @Override
    public String generateMessage(MessageTemplate template, Map<String, Object> parameters) {
        return generateMessageInternal(template, parameters, defaultConfiguration);
    }

    protected String generateMessageInternal(MessageTemplate template, Map<String, Object> parameters,
                                             Configuration configuration) {
        checkNotNullArgument(template);
        checkNotNullArgument(parameters);
        com.google.common.base.Preconditions.checkState(
                version.equals(configuration.getIncompatibleImprovements()),
                "Incompatible version found for %s".formatted(configuration.getClass().getSimpleName())
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Template htmlTemplate = getHtmlTemplate(template, configuration);

        try (Writer htmlWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            htmlTemplate.process(parameters, htmlWriter);
        } catch (TemplateException e) {
            throw new IllegalArgumentException(
                    "Unable to generate %s with '%s' code".formatted(
                            MessageTemplate.class.getSimpleName(), template.getCode()), e
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to write message template with '%s'".formatted(template.getCode()), e
            );
        }

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    @Override
    public String generateMessage(String templateCode, Map<String, Object> parameters) {
        MessageTemplate template = getMessageTemplateByCode(templateCode);
        return generateMessage(template, parameters);
    }

    protected List<String> generateMessagesByTemplates(Collection<MessageTemplate> templates,
                                                       Map<String, Object> parameters, Configuration configuration) {
        return templates.stream()
                .map(template -> generateMessageInternal(template, parameters, configuration))
                .toList();
    }

    protected MessageTemplate getMessageTemplateByCode(String templateCode) {
        return dataManager.load(MessageTemplate.class)
                .condition(PropertyCondition.equal("code", templateCode))
                .one();
    }

    protected Template getHtmlTemplate(MessageTemplate template, Configuration configuration) {
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        String templateCode = template.getCode();
        stringTemplateLoader.putTemplate(templateCode, template.getContent());

        configuration.setTemplateLoader(stringTemplateLoader);

        Template htmlTemplate;
        try {
            htmlTemplate = configuration.getTemplate(templateCode);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load message template with code '%s'".formatted(templateCode));
        }

        htmlTemplate.setObjectWrapper(wrapper);
        return htmlTemplate;
    }

    public class SingleTemplateGeneratorImpl implements SingleTemplateGenerator {

        protected MessageTemplate template;
        protected Map<String, Object> params = new HashMap<>();
        protected Configuration configuration = defaultConfiguration;

        @Override
        public SingleTemplateGenerator withTemplate(MessageTemplate template) {
            this.template = template;
            return this;
        }

        @Override
        public SingleTemplateGenerator withTemplateCode(String templateCode) {
            template = getMessageTemplateByCode(templateCode);
            return this;
        }

        @Override
        public SingleTemplateGenerator withConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        @Override
        public SingleTemplateGenerator withParams(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        @Override
        public SingleTemplateGenerator addParam(String alias, Object value) {
            this.params.put(alias, value);
            return this;
        }

        @Override
        public String generate() {
            Preconditions.checkNotNullArgument(template);
            return generateMessageInternal(template, params, configuration);
        }
    }

    public class MultiTemplateGeneratorImpl implements MultiTemplateGenerator {

        protected Collection<MessageTemplate> templates;
        protected Map<String, Object> params = new HashMap<>();
        protected Configuration configuration = defaultConfiguration;

        @Override
        public MultiTemplateGenerator withTemplates(MessageTemplate... templates) {
            this.templates = List.of(templates);
            return this;
        }

        @Override
        public MultiTemplateGenerator withTemplateCodes(String... templateCodes) {
            this.templates = Arrays.stream(templateCodes)
                    .map(MessageTemplatesGeneratorImpl.this::getMessageTemplateByCode)
                    .toList();

            return this;
        }

        @Override
        public MultiTemplateGenerator withConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        @Override
        public MultiTemplateGenerator withParams(Map<String, Object> params) {
            this.params.putAll(params);
            return this;
        }

        @Override
        public MultiTemplateGenerator addParam(String alias, Object value) {
            this.params.put(alias, value);
            return this;
        }

        @Override
        public List<String> generate() {
            Preconditions.checkNotNullArgument(templates);
            if (!templates.isEmpty()) {
                return generateMessagesByTemplates(templates, params, configuration);
            }

            return Collections.emptyList();
        }
    }

    public class MultiParamTemplateGeneratorImpl implements MultiParamTemplateGenerator {

        protected MessageTemplate template;
        protected Collection<Map<String, Object>> params = new ArrayList<>();
        protected Configuration configuration = defaultConfiguration;

        @Override
        public MultiParamTemplateGenerator withTemplate(MessageTemplate template) {
            this.template = template;
            return this;
        }

        @Override
        public MultiParamTemplateGenerator withTemplateCode(String templateCode) {
            this.template = getMessageTemplateByCode(templateCode);
            return this;
        }

        @Override
        public MultiParamTemplateGenerator withConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        @Override
        public MultiParamTemplateGenerator addParams(Map<String, Object> params) {
            this.params.add(params);
            return this;
        }

        @Override
        public List<String> generate() {
            Preconditions.checkNotNullArgument(template);
            return params.stream()
                    .map(params -> generateMessageInternal(template, params, configuration))
                    .toList();
        }
    }
}
