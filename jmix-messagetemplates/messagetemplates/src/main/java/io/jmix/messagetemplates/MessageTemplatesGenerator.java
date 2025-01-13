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

package io.jmix.messagetemplates;

import freemarker.template.Configuration;
import io.jmix.messagetemplates.entity.MessageTemplate;

import java.util.List;
import java.util.Map;

/**
 * The interface is used to generate messages using templates.
 */
public interface MessageTemplatesGenerator {

    /**
     * Creates {@link SingleTemplateGenerator}.
     * <p>
     * The generator is used to create messages based on a single template and a single map of parameters.
     * The result of the generation is a template filled with passed parameters.
     * </p>
     * Example of single template generation:
     * <pre>{@code
     *         generateSingleTemplate()
     *                 .withTemplateCode("my-template")
     *                 .addParam("username", "admin")
     *                 .addParam("firstName", "John")
     *                 .addParam("lastName", "Doe")
     *                 .generate();
     *
     * }</pre>
     *
     * @return {@link SingleTemplateGenerator}
     */
    SingleTemplateGenerator generateSingleTemplate();

    /**
     * Creates {@link MultiTemplateGenerator}.
     * <p>
     * The generator is used to create messages based on a multiple set of templates and a single map of parameters.
     * The result of the generation is a list of templates filled with passed parameters.
     * </p>
     * Example of multi template generation:
     * <pre>{@code
     *         generateMultiTemplate()
     *                 .withTemplateCodes("my-first-template", "my-second-template")
     *                 .addParam("username", "admin")
     *                 .addParam("firstName", "John")
     *                 .addParam("lastName", "Doe")
     *                 .generate();
     * }</pre>
     *
     * @return {@link MultiTemplateGenerator}
     */
    MultiTemplateGenerator generateMultiTemplate();

    /**
     * Creates {@link MultiParamTemplateGenerator}.
     * <p>
     * The generator is used to create messages based on a single template and multiple set of parameters.
     * The result of the generation is a list of templates filled with different parameters.
     * </p>
     * Example of multi param template generation:
     * <pre>{@code
     *        generateMultiParamTemplate()
     *                 .withTemplateCode("my-template")
     *                 .addParams(Map.of(
     *                         "username", "admin",
     *                         "firstName", "John",
     *                         "lastName", "Doe"
     *                 ))
     *                 .addParams(Map.of(
     *                         "username", "user",
     *                         "firstName", "Mary",
     *                         "lastName", "Smith"
     *                 ))
     *                 .generate();
     * }</pre>
     *
     * @return {@link MultiTemplateGenerator}
     */
    MultiParamTemplateGenerator generateMultiParamTemplate();

    /**
     * Generates a message based on the passed {@link MessageTemplate} and parameters map.
     *
     * @param template   template for generation
     * @param parameters parameters for generation
     * @return template filled with parameters
     */
    String generateMessage(MessageTemplate template, Map<String, Object> parameters);

    /**
     * Generates a message based on the passed code of {@link MessageTemplate} and parameters map.
     *
     * @param templateCode code of  template for generation
     * @param parameters   parameters for generation
     * @return template filled with parameters
     */
    String generateMessage(String templateCode, Map<String, Object> parameters);

    /**
     * The generator is used to create messages based on a single template and a single map of parameters.
     * The result of the generation is a template filled with passed parameters.
     */
    interface SingleTemplateGenerator {

        /**
         * Sets the template for generating the message.
         *
         * @param template template for generating the message.
         * @return this
         */
        SingleTemplateGenerator withTemplate(MessageTemplate template);

        /**
         * Sets the template code for generating the message.
         *
         * @param templateCode template code for generating the message
         * @return this
         */
        SingleTemplateGenerator withTemplateCode(String templateCode);

        /**
         * Sets the {@link Configuration} for message generation.
         *
         * @param configuration configuration for generating the message
         * @return this
         */
        SingleTemplateGenerator withConfiguration(Configuration configuration);

        /**
         * Sets the parameters for filling the template.
         *
         * @param params parameters map
         * @return this
         */
        SingleTemplateGenerator withParams(Map<String, Object> params);

        /**
         * Adds a parameter to filling the template.
         *
         * @param alias parameter alias
         * @param value parameter value
         * @return this
         */
        SingleTemplateGenerator addParam(String alias, Object value);

        /**
         * Generates a message based on the current state of the generator.
         *
         * @return generated message as a string
         */
        String generate();
    }

    /**
     * The generator is used to create messages based on a multiple set of templates and a single map of parameters.
     * The result of the generation is a list of templates filled with passed parameters.
     */
    interface MultiTemplateGenerator {

        /**
         * Sets the templates for generating the messages.
         *
         * @param templates array of {@link MessageTemplate} for generating the message.
         * @return this
         */
        MultiTemplateGenerator withTemplates(MessageTemplate... templates);

        /**
         * Sets the template codes for generating the messages.
         *
         * @param templateCodes array of {@link MessageTemplate} codes for generating the message.
         * @return this
         */
        MultiTemplateGenerator withTemplateCodes(String... templateCodes);

        /**
         * Sets the {@link Configuration} for message generation.
         *
         * @param configuration configuration for generating the message
         * @return this
         */
        MultiTemplateGenerator withConfiguration(Configuration configuration);

        /**
         * Sets the parameters for filling the templates.
         *
         * @param params parameters map
         * @return this
         */
        MultiTemplateGenerator withParams(Map<String, Object> params);

        /**
         * Adds a parameter to filling the templates.
         *
         * @param alias parameter alias
         * @param value parameter value
         * @return this
         */
        MultiTemplateGenerator addParam(String alias, Object value);

        /**
         * Generates a messages based on the current state of the generator.
         *
         * @return generated messages as a list of strings
         */
        List<String> generate();
    }

    /**
     * The generator is used to create messages based on a single template and multiple set of parameters.
     * The result of the generation is a list of templates filled with different parameters.
     */
    interface MultiParamTemplateGenerator {

        /**
         * Sets the template for generating the messages.
         *
         * @param template template for generating the messages.
         * @return this
         */
        MultiParamTemplateGenerator withTemplate(MessageTemplate template);

        /**
         * Sets the template code for generating the messages.
         *
         * @param templateCode template code for generating the messages
         * @return this
         */
        MultiParamTemplateGenerator withTemplateCode(String templateCode);

        /**
         * Sets the {@link Configuration} for message generation.
         *
         * @param configuration configuration for generating the message
         * @return this
         */
        MultiParamTemplateGenerator withConfiguration(Configuration configuration);

        /**
         * Adds a parameter map to the collection of parameters for generating the messages.
         *
         * @param params parameter map to add
         * @return this
         */
        MultiParamTemplateGenerator addParams(Map<String, Object> params);

        /**
         * Generates a messages based on the current state of the generator.
         *
         * @return generated messages as a list of strings
         */
        List<String> generate();
    }
}
