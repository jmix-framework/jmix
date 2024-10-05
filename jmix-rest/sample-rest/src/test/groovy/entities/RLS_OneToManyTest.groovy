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

package entities

import io.jmix.samples.rest.security.FullAccessRole
import io.jmix.samples.rest.security.InMemoryOneToManyRowLevelRole
import org.apache.http.HttpStatus
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.RestSpec

import static test_support.DataUtils.createCar
import static test_support.DataUtils.createInsuranceCase
import static test_support.DbUtils.getSql
import static test_support.RestSpecsUtils.createRequest
import static test_support.RestSpecsUtils.getAuthToken

class RLS_OneToManyTest extends RestSpec {
    private UUID carId
    private UUID case1Id, case2Id,
                 case3Id, case4Id,
                 case5Id

    private String userPassword = "password"
    private String userLogin = "user1"
    private String userToken
    private UserDetails user

    void setup() {
        user = User.builder()
                .username(userLogin)
                .password("{noop}" + userPassword)
                .authorities(Arrays.asList(
                        roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(FullAccessRole.NAME),
                        roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(InMemoryOneToManyRowLevelRole.NAME)))
                .build()
        userRepository.addUser(user)
        carId = createCar(dirtyData, sql, '001')

        case1Id = createInsuranceCase(dirtyData, sql, 'InsuranceCase#1_', carId)
        case2Id = createInsuranceCase(dirtyData, sql, 'InsuranceCase#2_', carId)
        case3Id = createInsuranceCase(dirtyData, sql, 'InsuranceCase#3_', carId)
        case5Id = createInsuranceCase(dirtyData, sql, 'InsuranceCase#5_', carId)

        case4Id = dirtyData.createInsuranceCaseUuid()

        userToken = getAuthToken(oauthUrl, userLogin, userPassword)
    }

    void cleanup() {
        userRepository.removeUser(user)
    }

    def """Store entity with same composition as in the database, hidden elements should not be deleted"""() {
        setup:

        def body = [
                'id'            : carId.toString(),
                'insuranceCases': [
                        ['id': case1Id.toString()],
                        ['id': case3Id.toString()]
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(4)
        testNotDeletedCase(carId, case2Id, "InsuranceCase#2_")
        testNotDeletedCase(carId, case5Id, "InsuranceCase#5_")
    }

    def """Store entity with new element in the composition, new element should be added"""() {
        setup:

        def body = [
                'id'            : carId.toString(),
                'insuranceCases': [
                        ['id': case1Id.toString()],
                        ['id': case3Id.toString()],
                        ['id': case4Id.toString(), 'description': 'InsuranceCase#4_']
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(5)
        testNotDeletedCase(carId, case4Id, "InsuranceCase#4_")
    }

    def """Store entity with element that is hidden in the composition, new element should not be created/updated in the collection"""() {
        setup:

        def body = [
                'id'            : carId.toString(),
                'insuranceCases': [
                        ['id': case1Id.toString()],
                        ['id': case3Id.toString()],
                        ['id': case2Id.toString(), 'description': 'InsuranceCase#2_2']
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(4)
        testNotDeletedCase(carId, case2Id, "InsuranceCase#2_")
    }

    def """Store entity with updated element in the composition, element should be updated because it isn't hidden"""() {
        setup:

        def body = [
                'id'            : carId.toString(),
                'insuranceCases': [
                        ['id': case1Id.toString()],
                        ['id': case3Id.toString(), 'description': 'InsuranceCase#3_3'],
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(4)
        testNotDeletedCase(carId, case3Id, "InsuranceCase#3_3")
    }

    def """Store entity with deleted element in the composition, element should be be deleted because it isn't hidden"""() {
        setup:

        def body = [
                'id'            : carId.toString(),
                'insuranceCases': [
                        ['id': case1Id.toString()]
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(3)
        testDeletedCase(carId, case3Id, "InsuranceCase#3_")
    }

    def """Store entity with empty array in the composition, elements should be deleted"""() {
        when:

        def body = [
                'id'            : carId.toString(),
                'insuranceCases': [],
        ]
        def request = createRequest(userToken).body(body)
        def response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(2)
        testDeletedCase(carId, case3Id, 'InsuranceCase#3_')
        testDeletedCase(carId, case1Id, 'InsuranceCase#1_')
    }


    void testCaseCount(int expectedCount) {
        def cntRow = sql.firstRow('select count(*) as cnt from REF_INSURANCE_CASE where car_id = ? and delete_ts is null',
                carId)
        assert cntRow.cnt == expectedCount
    }

    void testNotDeletedCase(UUID carId, UUID caseId, String expectedDescription) {
        def descRow = sql.firstRow('select DESCRIPTION as desc from REF_INSURANCE_CASE where car_id = ? and id = ? and delete_ts is null',
                carId, caseId)
        assert descRow.desc == expectedDescription
    }

    void testDeletedCase(UUID carId, UUID caseId, String expectedDescription) {
        def descRow = sql.firstRow('select DESCRIPTION as desc from REF_INSURANCE_CASE where car_id = ? and id = ? and delete_ts is not null',
                carId, caseId)
        assert descRow.desc == expectedDescription
    }
}
