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

package templates

import io.jmix.core.Metadata
import io.jmix.core.security.SystemAuthenticator
import io.jmix.email.EmailInfo
import io.jmix.emailtemplates.EmailTemplates
import io.jmix.emailtemplates.TemplateConverter
import io.jmix.emailtemplates.dto.ReportWithParams
import io.jmix.emailtemplates.entity.EmailTemplate
import io.jmix.emailtemplates.entity.JsonEmailTemplate
import io.jmix.emailtemplates.entity.ParameterValue
import io.jmix.emailtemplates.entity.TemplateReport
import io.jmix.emailtemplates.exception.ReportParameterTypeChangedException
import io.jmix.emailtemplates.exception.TemplateNotFoundException
import io.jmix.reports.entity.ParameterType
import io.jmix.reports.entity.Report
import io.jmix.reports.entity.ReportInputParameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification
import test_support.EmailTemplatesTestConfiguration

@ContextConfiguration(classes = [EmailTemplatesTestConfiguration])
class EmailTemplatesTest extends Specification {
    @Shared
    Map<String, Object> paramsMap
    @Shared
    List<ReportWithParams> paramsList
    @Shared
    ReportInputParameter parameter
    @Shared
    ParameterValue value

    @Autowired
    EmailTemplates delegate

    @Autowired
    TemplateConverter templateConverter

    @Autowired
    SystemAuthenticator authenticator


    @Autowired
    Metadata metadata;

    void setupSpec() {
        paramsMap = new HashMap<>()
        paramsList = new ArrayList<>()

        parameter = new ReportInputParameter()
        parameter.setType(ParameterType.ENTITY)
        parameter.setReport(new Report())
        value = new ParameterValue()
        value.setParameterType(ParameterType.TEXT)
    }

    void setup() {
        authenticator.begin()
    }

    void cleanup() {
        authenticator.end()
    }

    def "check that method generateEmail with list params throw exception with empty template"() {
        when:
        delegate.generateEmail(template as EmailTemplate, params as List<ReportWithParams>)

        then:
        thrown(TemplateNotFoundException)

        where:
        template                | params
        null                    | paramsList
        null                    | null
    }

    def "check that method checkParameterTypeChanged throw exception if parameter type was changed"() {
        when:
        delegate.checkParameterTypeChanged(inputParameter, parameterValue)

        then:
        thrown(ReportParameterTypeChangedException)

        where:
        inputParameter             | parameterValue
        parameter                  | new ParameterValue()
        parameter                  | value
    }

    def "check builder methods"() {
        when:
        EmailTemplate template = metadata.create(JsonEmailTemplate)
        template.setName("Test")
        template.setCode("Test")
        template.setHtml("\${paramValue}")
        template.setTo("address1")
        template.setCc("addressCC")
        template.setReport(initReport(template))
        template.setEmailBodyReport(initTemplateReport(template))

        EmailInfo emailInfo = delegate.buildFromTemplate(template)
                .setSubject("Subject")
                .setBodyParameter("paramValue", "New value")
                .addTo("address2")
                .setCc("newAddressCC")
                .generateEmail()


        then:
        emailInfo.subject == "Subject"
        emailInfo.addresses == "address1, address2"
        emailInfo.cc == "newAddressCC"
        emailInfo.body == "New value"

    }

    protected Report initReport(JsonEmailTemplate template) {
        return templateConverter.convertToReport(template)
    }

    protected TemplateReport initTemplateReport(JsonEmailTemplate template) {
        TemplateReport templateReport = metadata.create(TemplateReport)
        templateReport.setEmailTemplate(template)
        templateReport.setReport(template.report)
        templateReport.setParameterValues(new ArrayList<>())
        return templateReport
    }
}
