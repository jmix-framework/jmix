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

package generation

import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.messagetemplates.MessageTemplatesGenerator
import io.jmix.messagetemplates.entity.MessageTemplate
import io.jmix.messagetemplates.entity.TemplateType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import pojo.TestUser
import spock.lang.Specification
import test_support.MessageTemplatesTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration,
        EclipselinkConfiguration, MessageTemplatesTestConfiguration])
class MessageTemplatesGenerationTest extends Specification {

    @Autowired
    MessageTemplatesGenerator messageTemplatesGenerator
    @Autowired
    DataManager dataManager
    @Autowired
    JdbcTemplate jdbcTemplate

    void setup() {
        def firstTemplate = dataManager.create MessageTemplate

        firstTemplate.name = 'First Template'
        firstTemplate.code = 'first-template'
        firstTemplate.type = TemplateType.PLAIN
        firstTemplate.content = 'Hello ${firstName} ${lastName}! Your username is ${username}'

        def secondTemplate = dataManager.create MessageTemplate

        secondTemplate.name = 'Second Template'
        secondTemplate.code = 'second-template'
        secondTemplate.type = TemplateType.HTML
        secondTemplate.content = '<div>Hello <b>${firstName} ${lastName}</b>! Your username is <i>${username}<i></div>'

        dataManager.save firstTemplate, secondTemplate
    }

    void cleanup() {
        jdbcTemplate.execute("delete from MSGTMP_MESSAGE_TEMPLATE")
    }

    def "Generate single template message"() {
        when: "The single template was generated"
        def generatedMessage = messageTemplatesGenerator.generateSingleTemplate()
                .withTemplateCode('first-template')
                .withParams(Map.of(
                        'username', 'admin',
                        'firstName', 'John',
                        'lastName', 'Doe'
                ))
                .generate()

        then: "The template content is filled with parameters"

        'Hello John Doe! Your username is admin' == generatedMessage
    }

    def "Generate multi template message"() {
        when: "The multi template was generated"
        def generatedMessages = messageTemplatesGenerator.generateMultiTemplate()
                .withTemplateCodes('first-template', 'second-template')
                .withParams(Map.of(
                        'username', 'admin',
                        'firstName', 'John',
                        'lastName', 'Doe'
                ))
                .generate()

        then: "The templates content is filled with parameters"

        'Hello John Doe! Your username is admin' == generatedMessages[0]
        '<div>Hello <b>John Doe</b>! Your username is <i>admin<i></div>' == generatedMessages[1]
    }

    def "Generate multi params template message"() {
        when: "The multi params template was generated"
        def generatedMessages = messageTemplatesGenerator.generateMultiParamTemplate()
                .withTemplateCode('first-template')
                .addParams(Map.of(
                        'username', 'admin',
                        'firstName', 'John',
                        'lastName', 'Doe'
                ))
                .addParams(Map.of(
                        'username', 'user',
                        'firstName', 'Mary',
                        'lastName', 'Smith'
                ))
                .addParams(Map.of(
                        'username', 'moderator',
                        'firstName', 'Katherine',
                        'lastName', 'Potter'
                ))
                .generate()

        then: "The templates content is filled with parameters"

        'Hello John Doe! Your username is admin' == generatedMessages[0]
        'Hello Mary Smith! Your username is user' == generatedMessages[1]
        'Hello Katherine Potter! Your username is moderator' == generatedMessages[2]
    }

    def "Generate template message with wrapped properties"() {
        given: "Message template entity"
        def template = dataManager.create MessageTemplate

        template.name = 'Test template'
        template.code = 'test-template'
        template.type = TemplateType.PLAIN
        template.content = 'Hello ${user.firstName} ${user.lastName}! Your username is ${user.username}'

        when: "The message template was generated"
        def generatedMessage = messageTemplatesGenerator.generateSingleTemplate()
                .withTemplate(template)
                .addParam('user', new TestUser('test', 'John', 'Doe'))
                .generate()

        then: "The template content is filled with unwrapped parameters"
        'Hello John Doe! Your username is test' == generatedMessage
    }
}
