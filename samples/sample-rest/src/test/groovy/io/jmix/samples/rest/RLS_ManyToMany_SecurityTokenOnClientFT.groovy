/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest


//import com.haulmont.cuba.security.entity.ConstraintCheckType
import com.haulmont.masquerade.Connectors
//import com.haulmont.rest.demo.http.rest.jmx.SampleJmxService
//import com.haulmont.rest.demo.http.rest.jmx.WebConfigStorageJmxService
import groovy.sql.Sql
import io.jmix.samples.rest.api.DataSet
import org.apache.http.HttpStatus
import org.junit.Ignore
import spock.lang.Specification

import static org.hamcrest.CoreMatchers.notNullValue
import static io.jmix.samples.rest.DataUtils.*
import static io.jmix.samples.rest.DbUtils.getSql
import static io.jmix.samples.rest.RestSpecsUtils.createRequest
import static io.jmix.samples.rest.RestSpecsUtils.getAuthToken

//todo security
@Ignore
class RLS_ManyToMany_SecurityTokenOnClientFT extends Specification {

    private Sql sql
    private DataSet dirtyData = new DataSet()

    private UUID plantId
    private UUID model1Id, model2Id,
                 model3Id, model4Id,
                 model5Id

    private String userPassword = "password"
    private String userLogin = "user1"
    private String userToken

    void setup() {
        Connectors.jmx(SampleJmxService.class)
                .setRestRequiresSecurityToken(true)
        Connectors.jmx(WebConfigStorageJmxService.class)
                .setAppProperty("cuba.rest.requiresSecurityToken", "true")
        sql = getSql() as Sql

        def groupId = createGroup(dirtyData, sql, 'Group')

        createConstraint(dirtyData, sql,
                ConstraintCheckType.MEMORY, 'ref$Model',
                "!{E}.name.startsWith('Model#2_') && !{E}.name.startsWith('Model#5_')",
                groupId)

        UUID userId = createUser(dirtyData, sql,
                userLogin, userPassword, groupId)
        createUserRole(dirtyData, sql, userId, 'rest-full-access')

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

        userToken = getAuthToken(userLogin, userPassword)
    }

    void cleanup() {
        Connectors.jmx(SampleJmxService.class)
                .setRestRequiresSecurityToken(false)
        Connectors.jmx(WebConfigStorageJmxService.class)
                .setAppProperty("cuba.rest.requiresSecurityToken", "false")
        dirtyData.cleanup(sql.connection)
        if (sql != null) {
            sql.close()
        }
    }

    def """Store entity with same collection as in the database, hidden elements should not be deleted"""() {
        when:

        def request = createRequest(userToken)
                .param('view', 'plantWithModels')
        def response = request.with().get("/entities/ref\$Plant/$plantId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        def body = [
                'id'             : plantId.toString(),
                'models'         : [
                        ['id': model1Id.toString()],
                        ['id': model3Id.toString()]
                ],
                '__securityToken': securityToken
        ]

        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref\$Plant/$plantId")

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
        when:

        def request = createRequest(userToken)
                .param('view', 'plantWithModels')
        def response = request.with().get("/entities/ref\$Plant/$plantId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        def body = [
                'id'             : plantId.toString(),
                'models'         : [
                        ['id': model1Id.toString()],
                        ['id': model3Id.toString()],
                        ['id': model4Id.toString(), 'name': 'Model#4_4']
                ],
                '__securityToken': securityToken
        ]

        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref\$Plant/$plantId")

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
        when:

        def request = createRequest(userToken)
                .param('view', 'plantWithModels')
        def response = request.with().get("/entities/ref\$Plant/$plantId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        def body = [
                'id'             : plantId.toString(),
                'models'         : [
                        ['id': model1Id.toString()],
                        ['id': model3Id.toString()],
                        ['id': model2Id.toString(), 'name': 'Model#2_2']
                ],
                '__securityToken': securityToken
        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinkExists(plantId, model1Id)
        assertModelLinkExists(plantId, model2Id)
        assertModelLinkExists(plantId, model3Id)
        assertModelLinkExists(plantId, model5Id)
        assertModelNames_notChanged()
    }

    def """Store entity with updated element in the collection, element should not be updated"""() {
        when:

        def request = createRequest(userToken)
                .param('view', 'plantWithModels')
        def response = request.with().get("/entities/ref\$Plant/$plantId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        def body = [
                'id'             : plantId.toString(),
                'models'         : [
                        ['id': model1Id.toString()],
                        ['id': model3Id.toString(), 'name': 'Model#2_2'],
                ],
                '__securityToken': securityToken
        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinksCount(4)
        assertModelNames_notChanged()
    }

    def """Store entity with deleted element in the collection, element should be deleted because it isn't hidden"""() {
        when:

        def request = createRequest(userToken)
                .param('view', 'plantWithModels')
        def response = request.with().get("/entities/ref\$Plant/$plantId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        def body = [
                'id'             : plantId.toString(),
                'models'         : [
                        ['id': model1Id.toString()]
                ],
                '__securityToken': securityToken
        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinksCount(3)
        assertModelLinkExists(plantId, model1Id)
        assertModelLinkExists(plantId, model2Id)
        assertModelLinkExists(plantId, model5Id)
        assertModelNames_notChanged()
    }

    def """Store entity with deleted element in the collection, element should not be deleted because it was hidden when entity is loaded from REST"""() {
        when:

        updateModel(sql, model3Id, 'Model#2_3')

        then:

        assertNotDeletedModel(model3Id, "Model#2_3")

        when:

        def request = createRequest(userToken)
                .param('view', 'plantWithModels')
        def response = request.with().get("/entities/ref\$Plant/$plantId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        updateModel(sql, model3Id, 'Model#3_')

        then:

        assertNotDeletedModel(model3Id, "Model#3_")

        when:

        def body = [
                'id'             : plantId.toString(),
                'models'         : [
                        ['id': model1Id.toString()]
                ],
                '__securityToken': securityToken
        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinksCount(4)
        assertModelLinkExists(plantId, model1Id)
        assertModelLinkExists(plantId, model2Id)
        assertModelLinkExists(plantId, model3Id)
        assertModelLinkExists(plantId, model5Id)
        assertModelNames_notChanged()
    }

    def """Store entity with null element in the collection, element should not be deleted because it was hidden when entity is loaded from REST"""() {
        when:

        updateModel(sql, model3Id, 'Model#2_3')
        updateModel(sql, model1Id, 'Model#2_1')

        then:

        assertNotDeletedModel(model3Id, "Model#2_3")
        assertNotDeletedModel(model1Id, "Model#2_1")

        when:

        def request = createRequest(userToken)
                .param('view', 'plantWithModels')
        def response = request.with().get("/entities/ref\$Plant/$plantId")

        then:

        def securityToken = response.then().statusCode(HttpStatus.SC_OK)
                .body('__securityToken', notNullValue())
                .extract().path('__securityToken')

        when:

        updateModel(sql, model3Id, 'Model#3_')
        updateModel(sql, model1Id, 'Model#1_')

        then:

        assertNotDeletedModel(model3Id, "Model#3_")
        assertNotDeletedModel(model1Id, "Model#1_")

        when:

        def body = [
                'id'             : plantId.toString(),
                'models'         : null,
                '__securityToken': securityToken
        ]
        request = createRequest(userToken).body(body)
        response = request.with().put("/entities/ref\$Plant/$plantId")

        then:

        response.then().statusCode(HttpStatus.SC_OK)

        assertModelLinksCount(4)
        assertModelLinkExists(plantId, model1Id)
        assertModelLinkExists(plantId, model2Id)
        assertModelLinkExists(plantId, model3Id)
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
