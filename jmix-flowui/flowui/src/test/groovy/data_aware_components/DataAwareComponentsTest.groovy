/*
 * Copyright 2023 Haulmont.
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

package data_aware_components

import data_aware_components.view.DataAwareComponentsView
import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.core.security.CurrentAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.dataaware.TestDateTimeRangeEntity
import test_support.entity.dataaware.TestLengthEntity
import test_support.entity.dataaware.TestTimeZoneIdEntity
import test_support.spec.FlowuiTestSpecification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@SpringBootTest
class DataAwareComponentsTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    CurrentAuthentication currentAuthentication

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("data_aware_components.view")

        def saveContext = new SaveContext()

        def lengthEntity = dataManager.create(TestLengthEntity)
        def timeZoneIdEntity = dataManager.create(TestTimeZoneIdEntity)
        def dateTimeRangeEntity = dataManager.create(TestDateTimeRangeEntity)
        saveContext.saving(lengthEntity, timeZoneIdEntity, dateTimeRangeEntity)

        dataManager.save(saveContext)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_LENGTH")
        jdbcTemplate.execute("delete from TEST_TIME_ZONE_ID")
        jdbcTemplate.execute("delete from TEST_DATE_TIME_RANGE")
    }

    def "Load length range properties from entity metadata"() {
        when: "A DataAwareComponentsView will open with an entity that has data length restrictions"
        def dataAwareView = navigateToView(DataAwareComponentsView)

        then: "HasLengthLimited components must have appropriate restrictions"
        dataAwareView.sizeTextField.minLength == 3
        dataAwareView.sizeTextArea.minLength == 3
        dataAwareView.sizeEmailField.minLength == 3

        dataAwareView.sizeTextField.maxLength == 10
        dataAwareView.sizeTextArea.maxLength == 10
        dataAwareView.sizeEmailField.maxLength == 10

        dataAwareView.lengthTextField.minLength == 5
        dataAwareView.lengthTextArea.minLength == 5
        dataAwareView.lengthEmailField.minLength == 5

        dataAwareView.lengthTextField.maxLength == 15
        dataAwareView.lengthTextArea.maxLength == 15
        dataAwareView.lengthEmailField.maxLength == 15
    }

    def "Load zoneId property from entity metadata"() {
        when: "A DataAwareComponentsView will open with an entity that has property date type supports time zone conversation"
        def dataAwareView = navigateToView(DataAwareComponentsView)

        then: "HasZoneId components must have appropriate property"
        dataAwareView.zoneDateTimePicker.zoneId == currentAuthentication.timeZone.toZoneId()
        dataAwareView.zoneTimePicker.zoneId == currentAuthentication.timeZone.toZoneId()
    }

    def "Load date/time range from entity metadata"() {
        when: "A DataAwareComponentsView will open with an entity that has attributes with date/time range"
        def dataAwareView = navigateToView(DataAwareComponentsView)

        then: "Date/Time picker components must have appropriate range"
        dataAwareView.rangeDatePicker.getMin().isAfter(LocalDate.now())             // @Future
        dataAwareView.rangeTimePicker.getMin().isBefore(LocalTime.now())            // @FutureOrPresent

        // end of the current day
        dataAwareView.rangeDateTimePicker.getMax().isAfter(LocalDateTime.now())     // @Past
    }
}
