/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest


import io.jmix.core.security.CoreUser
import io.jmix.samples.rest.security.FullAccessRole
import io.jmix.samples.rest.security.InMemoryManyToManyRowLevelRole
import io.jmix.samples.rest.security.InMemoryOneToManyRowLevelRole
import io.jmix.security.authentication.RoleGrantedAuthority
import io.jmix.security.role.RoleRepository
import io.jmix.security.role.assignment.RoleAssignment
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

import static io.jmix.samples.rest.DataUtils.*
import static io.jmix.samples.rest.DbUtils.getSql
import static io.jmix.samples.rest.RestSpecsUtils.createRequest
import static io.jmix.samples.rest.RestSpecsUtils.getAuthToken
import static org.hamcrest.CoreMatchers.notNullValue

@TestPropertySource(properties =
        ["jmix.core.entitySerializationTokenRequired = true"])
class RLS_OneToMany_SecurityTokenOnClientFT extends RestSpec {
    private UUID carId
    private UUID case1Id, case2Id,
                 case3Id, case4Id,
                 case5Id

    private String userPassword = "password"
    private String userLogin = "user1"
    private String userToken
    private CoreUser user

    @Autowired
    private RoleRepository roleRepository

    void setup() {
        user = new CoreUser(userLogin, "{noop}" + userPassword,
                Arrays.asList(new RoleGrantedAuthority(roleRepository.getRoleByCode(InMemoryOneToManyRowLevelRole.NAME)),
                        new RoleGrantedAuthority(roleRepository.getRoleByCode(FullAccessRole.NAME))))
        userRepository.addUser(user)

        carId = createCar(dirtyData, sql, '001')

        case1Id = createInsuranceCase(dirtyData, sql, 'InsuranceCase#1_', carId)
        case2Id = createInsuranceCase(dirtyData, sql, 'InsuranceCase#2_', carId)
        case3Id = createInsuranceCase(dirtyData, sql, 'InsuranceCase#3_', carId)
        case5Id = createInsuranceCase(dirtyData, sql, 'InsuranceCase#5_', carId)

        case4Id = dirtyData.createInsuranceCaseUuid()

        userToken = getAuthToken(baseUrl, userLogin, userPassword)
    }

    void cleanup() {
        userRepository.removeUser(user)
    }

    def """Store entity with same composition as in the database, hidden elements should not be deleted"""() {
        when:

        def request = createRequest(userToken)
                .param('view', 'carWithInsuranceCases')
        def response = request.with().get("/entities/ref_Car/$carId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        def body = [
                'id'             : carId.toString(),
                'insuranceCases' : [
                        ['id': case1Id.toString()],
                        ['id': case3Id.toString()]
                ],
                '__securityToken': securityToken
        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(4)
        testNotDeletedCase(carId, case2Id, "InsuranceCase#2_")
        testNotDeletedCase(carId, case5Id, "InsuranceCase#5_")
    }

    def """Store entity with new element in the composition, new element should be added"""() {
        when:

        def request = createRequest(userToken)
                .param('view', 'carWithInsuranceCases')
        def response = request.with().get("/entities/ref_Car/$carId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')


        when:

        def body = [
                'id'             : carId.toString(),
                'insuranceCases' : [
                        ['id': case1Id.toString()],
                        ['id': case3Id.toString()],
                        ['id': case4Id.toString(), 'description': 'InsuranceCase#4_']
                ],
                '__securityToken': securityToken
        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(5)
        testNotDeletedCase(carId, case4Id, "InsuranceCase#4_")
    }

    def """Store entity with element that is hidden in the composition, new element should not be created/updated in the collection."""() {
        when:

        def request = createRequest(userToken)
                .param('view', 'carWithInsuranceCases')
        def response = request.with().get("/entities/ref_Car/$carId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        def body = [
                'id'             : carId.toString(),
                'insuranceCases' : [
                        ['id': case1Id.toString()],
                        ['id': case3Id.toString()],
                        ['id': case2Id.toString(), 'description': 'InsuranceCase#2_2']
                ],
                '__securityToken': securityToken
        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(4)
        testNotDeletedCase(carId, case2Id, "InsuranceCase#2_")
    }

    def """Store entity with updated element in the composition, element should be updated because it isn't hidden"""() {
        when:

        def request = createRequest(userToken)
                .param('view', 'carWithInsuranceCases')
        def response = request.with().get("/entities/ref_Car/$carId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        def body = [
                'id'             : carId.toString(),
                'insuranceCases' : [
                        ['id': case1Id.toString()],
                        ['id': case3Id.toString(), 'description': 'InsuranceCase#3_3'],
                ],
                '__securityToken': securityToken
        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(4)
        testNotDeletedCase(carId, case3Id, "InsuranceCase#3_3")
    }

    def """Store entity with deleted element in the composition, element should be deleted because it isn't hidden"""() {
        when:

        def request = createRequest(userToken)
                .param('view', 'carWithInsuranceCases')
        def response = request.with().get("/entities/ref_Car/$carId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        def body = [
                'id'             : carId.toString(),
                'insuranceCases' : [
                        ['id': case1Id.toString()]
                ],
                '__securityToken': securityToken

        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(3)
        testDeletedCase(carId, case3Id, "InsuranceCase#3_")
    }

    def """Store entity with deleted element in the composition, element should not be deleted because it was hidden when entity is loaded from REST"""() {
        when:

        updateInsuranceCase(sql, case3Id, 'InsuranceCase#2_3')

        then:

        testNotDeletedCase(carId, case3Id, 'InsuranceCase#2_3')

        when:

        def request = createRequest(userToken)
                .param('view', 'carWithInsuranceCases')
        def response = request.with().get("/entities/ref_Car/$carId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        updateInsuranceCase(sql, case3Id, 'InsuranceCase#3_')

        then:

        testNotDeletedCase(carId, case3Id, 'InsuranceCase#3_')

        when:

        def body = [
                'id'             : carId.toString(),
                'insuranceCases' : [
                        ['id': case1Id.toString()]
                ],
                '__securityToken': securityToken

        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(4)
        testNotDeletedCase(carId, case3Id, 'InsuranceCase#3_')
    }

    def """Store entity with empty array in the composition, elements should not be deleted because it was hidden when entity is loaded from REST"""() {
        when:

        updateInsuranceCase(sql, case3Id, 'InsuranceCase#2_3')
        updateInsuranceCase(sql, case1Id, 'InsuranceCase#2_1')

        then:

        testNotDeletedCase(carId, case3Id, 'InsuranceCase#2_3')
        testNotDeletedCase(carId, case1Id, 'InsuranceCase#2_1')

        when:

        def request = createRequest(userToken)
                .param('view', 'carWithInsuranceCases')
        def response = request.with().get("/entities/ref_Car/$carId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        updateInsuranceCase(sql, case3Id, 'InsuranceCase#3_')
        updateInsuranceCase(sql, case1Id, 'InsuranceCase#1_')

        then:

        testNotDeletedCase(carId, case3Id, 'InsuranceCase#3_')
        testNotDeletedCase(carId, case1Id, 'InsuranceCase#1_')

        when:

        def body = [
                'id'             : carId.toString(),
                'insuranceCases' : [],
                '__securityToken': securityToken

        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref_Car/$carId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)
        testCaseCount(4)
        testNotDeletedCase(carId, case3Id, 'InsuranceCase#3_')
        testNotDeletedCase(carId, case1Id, 'InsuranceCase#1_')
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
