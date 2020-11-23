/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest

import io.jmix.core.security.CoreUser
import io.jmix.samples.rest.security.FullAccessRole
import io.jmix.samples.rest.security.InMemoryManyToManyRowLevelRole
import io.jmix.security.authentication.RoleGrantedAuthority
import io.jmix.security.role.RoleRepository
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired

import static io.jmix.samples.rest.DataUtils.*
import static io.jmix.samples.rest.DbUtils.getSql
import static io.jmix.samples.rest.RestSpecsUtils.createRequest
import static io.jmix.samples.rest.RestSpecsUtils.getAuthToken

class RLS_ManyToManyFT extends RestSpec {
    private UUID plantId
    private UUID model1Id, model2Id,
                 model3Id, model4Id,
                 model5Id

    private String userPassword = "password"
    private String userLogin = "user1"
    private String userToken
    private CoreUser user

    @Autowired
    private RoleRepository roleRepository

    void setup() {
        user = new CoreUser(userLogin, "{noop}" + userPassword,
                Arrays.asList(new RoleGrantedAuthority(roleRepository.getRoleByCode(InMemoryManyToManyRowLevelRole.NAME)),
                        new RoleGrantedAuthority(roleRepository.getRoleByCode(FullAccessRole.NAME))))

        userRepository.addUser(user)

        plantId = createPlant(dirtyData, sql, '001')

        model1Id = createModel(dirtyData, sql, 'Model#1_')
        model2Id = createModel(dirtyData, sql, 'Model#2_')
        model3Id = createModel(dirtyData, sql, 'Model#3_')
        model4Id = createModel(dirtyData, sql, 'Model#4_')
        model5Id = createModel(dirtyData, sql, 'Model#5_')

        createPlantModelLink(sql, plantId, model1Id)
        createPlantModelLink(sql, plantId, model2Id)
        createPlantModelLink(sql, plantId, model3Id)
        createPlantModelLink(sql, plantId, model5Id)

        userToken = getAuthToken(baseUrl, userLogin, userPassword)
    }

    void cleanup() {
        userRepository.removeUser(user)
    }

    def """Store entity with same collection as in the database, hidden elements should not be deleted"""() {
        setup:

        def body = [
                'id'    : plantId.toString(),
                'models': [
                        ['id': model1Id.toString()],
                        ['id': model3Id.toString()]
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinksCount(4)
        assertModelLinkExists(plantId, model1Id)
        assertModelLinkExists(plantId, model2Id)
        assertModelLinkExists(plantId, model3Id)
        assertModelLinkExists(plantId, model5Id)

        assertModelNames_notChanged()
    }

    def "Store entity with new element in the collection, new element should be added"() {
        setup:

        def body = [
                'id'    : plantId.toString(),
                'models': [
                        ['id': model1Id.toString()],
                        ['id': model3Id.toString()],
                        ['id': model4Id.toString(), 'name': 'Model#4_4']
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinksCount(5)
        assertModelLinkExists(plantId, model1Id)
        assertModelLinkExists(plantId, model2Id)
        assertModelLinkExists(plantId, model3Id)
        assertModelLinkExists(plantId, model4Id)
        assertModelLinkExists(plantId, model5Id)
        assertModelNames_notChanged()
    }

    def """Store entity with element that is hidden in the collection, new element should not be created in the collection"""() {
        setup:

        def body = [
                'id'    : plantId.toString(),
                'models': [
                        ['id': model1Id.toString()],
                        ['id': model3Id.toString()],
                        ['id': model2Id.toString(), 'name': 'Model#2_2']
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinkExists(plantId, model1Id)
        assertModelLinkExists(plantId, model2Id)
        assertModelLinkExists(plantId, model3Id)
        assertModelLinkExists(plantId, model5Id)
        assertModelNames_notChanged()
    }

    def """Store entity with updated element in the collection, element should not be updated"""() {
        setup:

        def body = [
                'id'    : plantId.toString(),
                'models': [
                        ['id': model1Id.toString()],
                        ['id': model3Id.toString(), 'name': 'Model#2_2'],
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinksCount(4)
        assertModelNames_notChanged()
    }

    def """Store entity with deleted element in the collection, element should be be deleted because it isn't hidden"""() {
        setup:

        def body = [
                'id'    : plantId.toString(),
                'models': [
                        ['id': model1Id.toString()]
                ]
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinksCount(3)
        assertModelLinkExists(plantId, model1Id)
        assertModelLinkExists(plantId, model2Id)
        assertModelLinkExists(plantId, model5Id)
        assertModelNames_notChanged()
    }

    def """Store entity with null element in the collection, element should be be deleted"""() {
        setup:

        def body = [
                'id'    : plantId.toString(),
                'models': null
        ]
        def request = createRequest(userToken).body(body)

        when:

        def response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinksCount(2)
        assertModelLinkExists(plantId, model2Id)
        assertModelLinkExists(plantId, model5Id)
        assertModelNames_notChanged()
    }


    void assertModelLinksCount(int expectedCount) {
        def cntRow = sql.firstRow('select count(*) as cnt from ref_plant_model_link where plant_id = ?',
                plantId)
        assert cntRow.cnt == expectedCount
    }

    void assertModelLinkExists(UUID plantId, UUID modelId) {
        def cntRow = sql.firstRow('select count(*) as cnt from ref_plant_model_link where plant_id = ? and model_id = ?',
                plantId, modelId)
        assert cntRow.cnt == 1
    }

    void assertNotDeletedModel(UUID modelId, String expectedName) {
        def descRow = sql.firstRow('select name from ref_model where id = ? and delete_ts is null',
                modelId)
        assert descRow.name == expectedName
    }

    void assertModelNames_notChanged() {
        assertNotDeletedModel(model1Id, "Model#1_")
        assertNotDeletedModel(model2Id, "Model#2_")
        assertNotDeletedModel(model3Id, "Model#3_")
        assertNotDeletedModel(model4Id, "Model#4_")
        assertNotDeletedModel(model5Id, "Model#5_")
    }
}
