/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql.datafetcher

import io.jmix.graphql.AbstractGraphQLTest
import org.springframework.http.HttpHeaders
import spock.lang.Ignore

class MutationValidationTest extends AbstractGraphQLTest {

    def "should show correct validation message on submit not allowed null value"() {
        when:
        // todo shortcut .graphql path
        def response = query("datafetcher/upsert-car-with-null-car-type.graphql")
        def error = getErrors(response)[0].getAsJsonObject()
        def errorMsg = getMessage(error)
        def extensionErrMsg = getExtensions(error).get("persistenceError").getAsString()
//        println "response = $response.rawResponse"

        then:
        errorMsg == "Exception while fetching data (/upsert_scr_Car) : Can't save entity to database"
        extensionErrMsg == "Can't save entity to database"
    }

    def "should throw exception while creating new DatatypesTestEntity with read-only attributes"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypes-test-entity.graphql",
                asObjectNode('{"entity":{' +
                        '"readOnlyStringAttr":"read-only",' +
                        '"id":"6a538099-9dfd-8761-fa32-b496c236dbe8"}}'))
        def error = getErrors(response)[0].getAsJsonObject()

        then:
        getMessage(error) == "Exception while fetching data (/upsert_scr_DatatypesTestEntity) : " +
                "Modifying read-only attributes is forbidden [readOnlyStringAttr]"
    }

    def "should show bean validation message without parent attribute"() {
        when:
        def response = query(
                "datafetcher/upsert-car-with-bean-validation-errors.graphql")
        def error = getErrors(response)[0].getAsJsonObject()
        def extensions = getExtensions(error).getAsJsonArray("constraintViolations")
        List messages = new ArrayList()
        extensions.forEach(extension -> {
            if (getPath(extension.getAsJsonObject()) == "regNumber") {
                messages.add(getMessage(extension.getAsJsonObject()))
                messages.add(getPath(extension.getAsJsonObject()))
            }
        })
        messages.sort()

        then:
        getMessage(error) == "Exception while fetching data (/upsert_scr_Car) : Entity validation failed"
        messages.get(0) == "must match \"[a-zA-Z]{2}\\d{3}\""
        messages.get(1) == "regNumber"
    }

    def "should show bean validation message with parent attribute"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypesTestEntity-bean-validation.gql")
        def error = getErrors(response)[0].getAsJsonObject()
        def extensions = getExtensions(error).getAsJsonArray("constraintViolations")
        List messages = new ArrayList()
        messages.add(getMessage(extensions[0].getAsJsonObject()))
        messages.add(getPath(extensions[0].getAsJsonObject()))
        messages.sort()

        then:
        getMessage(error) == "Exception while fetching data (/upsert_scr_DatatypesTestEntity) : Entity validation failed"
        messages.get(0) == "compositionO2Oattr.quantity"
        messages.get(1) == "must be greater than or equal to 0"
    }

    def "should show correct message for one-to-many composition validation"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypesTestEntity-O2M-bean-validation.gql")
        def error = getErrors(response)[0].getAsJsonObject()
        def extensions = getExtensions(error).getAsJsonArray("constraintViolations")
        List messages = new ArrayList()
        messages.add(getMessage(extensions[0].getAsJsonObject()))
        messages.add(getPath(extensions[0].getAsJsonObject()))
        messages.sort()

        then:
        getMessage(error) == "Exception while fetching data (/upsert_scr_DatatypesTestEntity) : Entity validation failed"
        messages.get(0) == "compositionO2Mattr.quantity"
        messages.get(1) == "must be greater than or equal to 0"
    }

    def "should show bean validation message with parent attribute and nested composition"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypesTestEntity-bean-validation-more-dept.gql")
        def error = getErrors(response)[0].getAsJsonObject()
        def extensions = getExtensions(error).getAsJsonArray("constraintViolations")
        List messages = new ArrayList()
        messages.add(getMessage(extensions[0].getAsJsonObject()))
        messages.add(getPath(extensions[0].getAsJsonObject()))
        messages.sort()

        then:
        getMessage(error) == "Exception while fetching data (/upsert_scr_DatatypesTestEntity) : Entity validation failed"
        messages.get(0) == "Length can't be less than 6 symbols"
        messages.get(1) == "compositionO2Oattr.nestedComposition.name"
    }

    def "should show bean validation messages with 'en' locale"() {
        when:
        def headers = new HttpHeaders()
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, "en")
        def response = query("datafetcher/upsert-car-with-bean-validation-errors.graphql",
                headers)
        def error = getErrors(response)[0].getAsJsonObject()
        def extensions = getExtensions(error).getAsJsonArray("constraintViolations")

        List messages = new ArrayList()
        messages.add(getMessage(extensions[0].getAsJsonObject()))
        messages.add(getMessage(extensions[1].getAsJsonObject()))
        messages.sort()

        then:
        getMessage(error) == "Exception while fetching data (/upsert_scr_Car) : Entity validation failed"
        messages.get(0) == "Manufacturer empty"
        messages.get(1) == "must match \"[a-zA-Z]{2}\\d{3}\""
    }

    @Ignore // todo rework with https://github.com/Haulmont/jmix-graphql/issues/209
    def "should show bean validation messages with 'ru' locale"() {
        when:
        def headers = new HttpHeaders()
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, "ru")
        def response = query("datafetcher/upsert-car-with-bean-validation-errors.graphql",
                headers)
        def error = getErrors(response)[0].getAsJsonObject()
        def extensions = getExtensions(error).getAsJsonArray("constraintViolations")

        List messages = new ArrayList()
        messages.add(getMessage(extensions[0].getAsJsonObject()))
        messages.add(getMessage(extensions[1].getAsJsonObject()))
        messages.sort()

        then:
        getMessage(error) == "Exception while fetching data (/upsert_scr_Car) : Entity validation failed"
        messages.get(1) == "Производитель не указан"
        messages.get(0) == "must match \"[a-zA-Z]{2}\\d{3}\""
    }

    def "should throw the exception when a reverse attribute does not contain a correct parent entity"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypesTestEntity-composition.gql",
                asObjectNode('{"parent": {"id": "db9faa31-dfa3-4b97-943c-ba268888cdc3"}}')
        )
        def errorMessage = getMessage(getErrors(response)[0].getAsJsonObject())

        then:
        errorMessage == "Exception while fetching data (/upsert_scr_DatatypesTestEntity) : " +
                "Composition attribute 'compositionO2Mattr' in class 'scr_DatatypesTestEntity' " +
                "doesn't contain the correct link to parent entity. " +
                "Please set correct parent ID 'f17652de-59f6-f2a5-9fd8-1ec69ffaa761' in composition relation."
    }

    def "should throw the exception when a deeply nested reverse attribute does not contain a correct parent entity"() {
        when:
        def response = query(
                "datafetcher/upsert-datatypesTestEntity-deeply-nested-composition.gql",
                asObjectNode('{"parent": {"id": "db9faa31-dfa3-4b97-943c-ba268888cdc3"}}')
        )
        def errorMessage = getMessage(getErrors(response)[0].getAsJsonObject())

        then:
        errorMessage == "Exception while fetching data (/upsert_scr_DatatypesTestEntity) : " +
                "Composition attribute 'nestedComposition' in class 'scr_CompositionO2OTestEntity' " +
                "doesn't contain the correct link to parent entity. " +
                "Please set correct parent ID 'f17652de-59f6-f2a5-9fd8-1ec69ffaa733' in composition relation."

    }
}
