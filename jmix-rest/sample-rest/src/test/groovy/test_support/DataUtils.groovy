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

package test_support

import groovy.sql.Sql
import io.jmix.samples.rest.entity.driver.DriverStatus

class DataUtils {

    //todo encryption
    //private static EncryptionModule encryption = new BCryptEncryptionModule()

    static UUID createGroup(DataSet dataSet, Sql sql, String groupName) {
        def groupId = dataSet.createGroupUuid()
        sql.dataSet('sample_rest_sec_group').add(
                id: groupId,
                version: 1,
                name: groupName
        )
        return groupId
    }

    static UUID createConstraint(DataSet dataSet, Sql sql,
//                                 ConstraintCheckType checkType,
                                 String metaClass,
                                 String expression,
                                 UUID groupId) {
        def constraintId = dataSet.createConstraintUuid()
        sql.dataSet('sec_constraint').add(
                id: constraintId,
                version: 1,
//                check_type: checkType.getId(),
                entity_name: metaClass,
                groovy_script: expression,
                group_id: groupId
        )
        return constraintId
    }

    static UUID createUser(DataSet dataSet, Sql sql,
                           String login,
//                           String password,
                           UUID groupId) {
        def userId = dataSet.createUserUuid()
        sql.dataSet('sample_rest_sec_user').add(
                id: userId,
                version: 1,
                login: login,
//                password: encryption.getPasswordHash(userId, password),
//                password_encryption: encryption.hashMethod,
                login_lc: login.toLowerCase(),
                group_id: groupId
        )
        return userId
    }

    static UUID createUser(DataSet dataSet, Sql sql,
                           String login,
                           String firstName,
                           UUID groupId) {
        def userId = dataSet.createUserUuid()
        sql.dataSet('sample_rest_sec_user').add(
                id: userId,
                version: 1,
                login: login,
                login_lc: login.toLowerCase(),
                first_name: firstName,
                group_id: groupId
        )
        return userId
    }

    static UUID createRole(DataSet dataSet, Sql sql, String name, String securityScope) {
        def roleId = dataSet.createRoleUuid()
        sql.dataSet('sample_rest_sec_role').add(
                id: roleId,
                version: 1,
                name: name,
                security_scope: securityScope
        )
        return roleId
    }

    static UUID createRole(DataSet dataSet, Sql sql, String name) {
        def roleId = dataSet.createRoleUuid()
        sql.dataSet('sample_rest_sec_role').add(
                id: roleId,
                version: 1,
                name: name
        )
        return roleId
    }

    static UUID createCar(DataSet dataSet, Sql sql, String vin) {
        def carId = dataSet.createCarUuid()
        sql.dataSet('ref_car').add(
                id: carId,
                version: 1,
                vin: vin
        )
        return carId
    }

    static UUID createDriver(DataSet dataSet, Sql sql, String name, DriverStatus status) {
        def driverId = dataSet.createDriverUuid()
        sql.dataSet('ref_Driver').add(
                id: driverId,
                version: 1,
                dtype: 'ref$ExtDriver',
                status: status.id,
                name: name
        )
        return driverId
    }

    static UUID createCarWithColour(DataSet dataSet, Sql sql, String vin, UUID colourId) {
        def carId = dataSet.createCarUuid()
        sql.dataSet('ref_car').add(
                id: carId,
                version: 1,
                vin: vin,
                colour_id: colourId
        )
        return carId
    }

    static UUID createInsuranceCase(DataSet dataSet, Sql sql, String description, UUID carId) {
        def caseId = dataSet.createInsuranceCaseUuid()
        sql.dataSet('ref_insurance_case').add(
                id: caseId,
                version: 1,
                description: description,
                car_id: carId
        )
        return caseId
    }

    static void updateInsuranceCase(Sql sql, UUID caseId, String description) {
        sql.executeUpdate('update ref_insurance_case set description = ? where id = ?',
                [description, caseId])
    }

    static UUID createPlant(DataSet dataSet, Sql sql, String name) {
        def plantId = dataSet.createPlantUuid()
        sql.dataSet('ref_plant').add(
                id: plantId,
                version: 1,
                name: name,
        )
        return plantId
    }

    static UUID createModel(DataSet dataSet, Sql sql, String name) {
        def modelId = dataSet.createModelUuid()
        sql.dataSet('ref_model').add(
                id: modelId,
                version: 1,
                name: name,
                dtype: 'ref$ExtModel'
        )
        return modelId
    }

    static UUID createColor(DataSet dataSet, Sql sql, String name) {
        def colorId = dataSet.createColourUuid()
        sql.dataSet('ref_colour').add(
                id: colorId,
                version: 1,
                name: name
        )
        return colorId
    }

    static UUID createValidatedEntity(DataSet dataSet, Sql sql, String name) {
        def validatedEntityId = dataSet.createValidatedEntityId()
        sql.dataSet('rest_validated_entity').add(
                id: validatedEntityId,
                name: name
        )
        return validatedEntityId
    }

    static void createPlantModelLink(Sql sql, UUID plantId, UUID modelId) {
        sql.dataSet('ref_plant_model_link').add(
                plant_id: plantId,
                model_id: modelId
        )
    }

    static void updateModel(Sql sql, UUID modelId, String name) {
        sql.executeUpdate('update ref_model set name = ? where id = ?',
                [name, modelId])
    }
}
