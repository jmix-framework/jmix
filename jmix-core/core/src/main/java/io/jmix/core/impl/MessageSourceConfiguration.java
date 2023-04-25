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

package io.jmix.core.impl;

import io.jmix.core.annotation.MessageSourceBasenames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import jakarta.annotation.PostConstruct;

@Configuration
public class MessageSourceConfiguration implements ImportAware {

    private AnnotationAttributes messageSourceParams;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        messageSourceParams = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(MessageSourceBasenames.class.getName(), false));

    }

    @PostConstruct
    protected void configureMessageSource() {
        if (messageSource instanceof AbstractResourceBasedMessageSource && messageSourceParams != null) {
            String[] baseNames = messageSourceParams.getStringArray("value");
            for (String baseName : baseNames) {
                ((AbstractResourceBasedMessageSource) messageSource).addBasenames(baseName);
            }
        }
    }
}
