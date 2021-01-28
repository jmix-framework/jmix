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
import io.jmix.samples.rest.security.InMemoryManyToManyRowLevelRole
import io.jmix.security.authentication.RoleGrantedAuthority
import org.apache.http.HttpStatus
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.context.TestPropertySource
import test_support.RestSpec

import static org.hamcrest.CoreMatchers.notNullValue
import static test_support.DataUtils.*
import static test_support.DbUtils.getSql
import static test_support.RestSpecsUtils.createRequest
import static test_support.RestSpecsUtils.getAuthToken

@TestPropertySource(properties =
        ["jmix.core.entitySerializationTokenRequired = true"])
class RLS_ManyToMany_SecurityTokenOnClientTest extends RestSpec {

    private UUID plantId
    private UUID model1Id, model2Id,
                 model3Id, model4Id,
                 model5Id

    private String userPassword = "password"
    private String userLogin = "user1"
    private String userToken
    private UserDetails user

    void setup() {
        user = User.builder()
                .username(userLogin)
                .password("{noop}" + userPassword)
                .authorities(RoleGrantedAuthority.withResourceRoleProvider({ resourceRoleRepository.getRoleByCode(it) })
                        .withRowLevelRoleProvider({ rowLevelRoleRepository.getRoleByCode(it) })
                        .withResourceRoles(FullAccessRole.NAME)
                        .withRowLevelRoles(InMemoryManyToManyRowLevelRole.NAME)
                        .build())
                .build()

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
