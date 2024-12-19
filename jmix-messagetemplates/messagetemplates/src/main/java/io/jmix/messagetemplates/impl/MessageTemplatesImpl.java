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
import freemarker.core.ParseException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.MapModel;
import freemarker.template.*;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.messagetemplates.MessageTemplateProperties;
import io.jmix.messagetemplates.MessageTemplates;
import io.jmix.messagetemplates.entity.MessageTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("msgtmp_MessageTemplatesImpl")
public class MessageTemplatesImpl implements MessageTemplates, InitializingBean {

    protected DataManager dataManager;
    protected Version version;
    protected ObjectWrapper wrapper;

    public MessageTemplatesImpl(DataManager dataManager, MessageTemplateProperties messageTemplateProperties) {
        this.dataManager = dataManager;
        this.version = messageTemplateProperties.getFreemarkerVersion();
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
    public String generateMessage(MessageTemplate template, Map<String, Object> parameters) {
        checkNotNullArgument(template);
        checkNotNullArgument(parameters);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Template htmlTemplate = getHtmlTemplate(template);

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

    protected MessageTemplate getMessageTemplateByCode(String templateCode) {
        return dataManager.load(MessageTemplate.class)
                .condition(PropertyCondition.equal("code", templateCode))
                .one();
    }

    protected Template getHtmlTemplate(MessageTemplate template) {
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        String templateName = template.getName();
        stringTemplateLoader.putTemplate(templateName, template.getContent());

        Configuration configuration = new Configuration(version);
        configuration.setTemplateLoader(stringTemplateLoader);
        // TODO: kd, add default formatters from app props
        configuration.setDefaultEncoding("UTF-8");

        Template htmlTemplate;
        // TODO: kd, handle exceptions
        try {
            htmlTemplate = configuration.getTemplate(templateName);
        } catch (TemplateNotFoundException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (MalformedTemplateNameException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        htmlTemplate.setObjectWrapper(wrapper);
        return htmlTemplate;
    }
}
