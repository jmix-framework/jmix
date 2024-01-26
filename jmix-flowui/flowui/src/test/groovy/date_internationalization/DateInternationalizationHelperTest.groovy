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

package date_internationalization

import io.jmix.flowui.component.DateInternationalizationHelper
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Unroll
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DateInternationalizationHelperTest extends FlowuiTestSpecification {

    def "should return correct month names"() {
        given:
        def locale = Locale.US
        def expectedMonthNames = [
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ]

        when:
        def actualMonthNames = DateInternationalizationHelper.getMonthNames(locale)

        then:
        actualMonthNames == expectedMonthNames
    }

    def "should return correct weekday names"() {
        given:
        def locale = Locale.US
        def expectedWeekdayNames = [
                "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        ]

        when:
        def actualWeekdayNames = DateInternationalizationHelper.getWeekdayNames(locale)

        then:
        actualWeekdayNames == expectedWeekdayNames
    }

    def "should return correct short weekday names"() {
        given:
        def locale = Locale.US
        def expectedShortWeekdayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]

        when:
        def actualShortWeekdayNames = DateInternationalizationHelper.getShortWeekdayNames(locale)

        then:
        actualShortWeekdayNames == expectedShortWeekdayNames
    }

    def "should return correct first day of the week"() {
        given:
        def locale = Locale.US

        when:
        def actualFirstDayOfWeek = DateInternationalizationHelper.getFirstDayOfWeek(locale)

        then:
        actualFirstDayOfWeek == Calendar.SUNDAY
    }

    def "should return correct month names for Russian locale"() {
        given:
        def locale = new Locale("ru", "RU")
        def expectedMonthNames = [
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        ]

        when:
        def actualMonthNames = DateInternationalizationHelper.getMonthNames(locale)

        then:
        actualMonthNames == expectedMonthNames
    }

    def "should return correct weekday names for Russian locale"() {
        given:
        def locale = new Locale("ru", "RU")
        def expectedWeekdayNames = [
                "воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота"
        ]

        when:
        def actualWeekdayNames = DateInternationalizationHelper.getWeekdayNames(locale)

        then:
        actualWeekdayNames == expectedWeekdayNames
    }

    def "should return correct short weekday names for Russian locale"() {
        given:
        def locale = new Locale("ru", "RU")
        def expectedShortWeekdayNames = ["вс", "пн", "вт", "ср", "чт", "пт", "сб"]

        when:
        def actualShortWeekdayNames = DateInternationalizationHelper.getShortWeekdayNames(locale)

        then:
        actualShortWeekdayNames == expectedShortWeekdayNames
    }

    def "should return correct first day of the week for Russian locale"() {
        given:
        def locale = new Locale("ru", "RU")

        when:
        def actualFirstDayOfWeek = DateInternationalizationHelper.getFirstDayOfWeek(locale)

        then:
        actualFirstDayOfWeek == Calendar.MONDAY
    }

    def "should return correct first day of the week for English locale"() {
        given:
        def locale = Locale.ENGLISH

        when:
        def actualFirstDayOfWeek = DateInternationalizationHelper.getFirstDayOfWeek(locale)

        then:
        actualFirstDayOfWeek == Calendar.SUNDAY
    }

    @Unroll
    def "should return #vaadinDayOfWeek as vaadinDayOfWeek for #calenderDayOfWeek as calenderDayOfWeek"() {
        expect:
        DateInternationalizationHelper.calenderDayOfWeekToVaadinDayOfWeek(calenderDayOfWeek) == vaadinDayOfWeek

        where:
        calenderDayOfWeek  || vaadinDayOfWeek
        Calendar.SUNDAY    || 0
        Calendar.MONDAY    || 1
        Calendar.WEDNESDAY || 3
        Calendar.SATURDAY  || 6
    }
}
