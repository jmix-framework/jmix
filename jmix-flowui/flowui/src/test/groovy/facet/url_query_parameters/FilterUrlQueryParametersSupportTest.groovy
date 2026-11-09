/*
 * Copyright 2026 Haulmont.
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

package facet.url_query_parameters

import io.jmix.core.Metadata
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.urlqueryparameters.FilterUrlQueryParametersSupport
import io.jmix.flowui.view.navigation.UrlParamSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.viewtemplate.ComponentXmlDatatypesEntity
import test_support.spec.FlowuiTestSpecification

import java.sql.Date as SqlDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Pins how {@link FilterUrlQueryParametersSupport} reads back the value of a filter condition that the
 * URL query parameters binders have written.
 */
@SpringBootTest
class FilterUrlQueryParametersSupportTest extends FlowuiTestSpecification {

    @Autowired
    FilterUrlQueryParametersSupport filterUrlQueryParametersSupport

    @Autowired
    UrlParamSerializer urlParamSerializer

    @Autowired
    Metadata metadata

    def "DATE_EQUALS value written to the URL is read back as the value of the #property attribute"() {
        given: "the value written the way the binders write it"
        def valueString = urlParamSerializer.serialize(filterUrlQueryParametersSupport.getSerializableValue(value))

        when:
        def parsed = filterUrlQueryParametersSupport.parseValue(metadata.getClass(ComponentXmlDatatypesEntity),
                property, PropertyFilter.Operation.DATE_EQUALS.type, valueString)

        then:
        parsed == value
        parsed.class == value.class

        where:
        property              | value
        "dateValue"           | new GregorianCalendar(2026, Calendar.SEPTEMBER, 15).time
        "sqlDateValue"        | SqlDate.valueOf(LocalDate.of(2026, 9, 15))
        "localDateValue"      | LocalDate.of(2026, 9, 15)
        "localDateTimeValue"  | LocalDateTime.of(2026, 9, 15, 0, 0)
        "offsetDateTimeValue" | OffsetDateTime.of(2026, 9, 15, 0, 0, 0, 0, ZoneOffset.ofHours(4))
    }
}
