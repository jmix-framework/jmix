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

package test_support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DataSet {

    /**
     * Sets for ids of all the entities that has been created
     */
    private Set<UUID> carIds = new HashSet<>();
    private Set<UUID> carDocumentationsIds = new HashSet<>();
    private Set<UUID> carTokenIds = new HashSet<>();
    private Set<UUID> insuranceCaseIds = new HashSet<>();
    private Set<UUID> colourIds = new HashSet<>();
    private Set<UUID> driverIds = new HashSet<>();
    private Set<UUID> driverAllocIds = new HashSet<>();
    private Set<UUID> userIds = new HashSet<>();
    private Set<UUID> roleIds = new HashSet<>();
    private Set<UUID> groupIds = new HashSet<>();
    private Set<UUID> permissionIds = new HashSet<>();
    private Set<UUID> modelIds = new HashSet<>();
    private Set<UUID> repairIds = new HashSet<>();
    private Set<UUID> driverGroupIds = new HashSet<>();
    private Set<UUID> caseIds = new HashSet<>();
    private Set<UUID> debtorIds = new HashSet<>();
    private Set<Long> sellerIds = new HashSet<>();
    private Set<UUID> categoryIds = new HashSet<>();
    private Set<UUID> categoryAttributeIds = new HashSet<>();
    private Set<UUID> categoryAttributeValueIds = new HashSet<>();
    private Set<UUID> constraintIds = new HashSet<>();
    private Set<UUID> plantIds = new HashSet<>();
    private Set<UUID> validatedEntityIds = new HashSet<>();
    private Set<UUID> secretEntityIds = new HashSet<>();
    private Set<String> currencyIds = new HashSet<>();
    private Set<Long> compositeKeyEntityIds = new HashSet<>();
    private Set<Integer> compositeKeyEntityTenantIds = new HashSet<>();
    private Set<Integer> nonStandardIdNameEntityIds = new HashSet<>();

    private static AtomicLong compositeKeyEntityIdGen = new AtomicLong();
    private static AtomicInteger compositeKeyEntityTenantIdGen = new AtomicInteger();
    private static AtomicInteger nonStandardIdNameEntityIdGen = new AtomicInteger();

    public void addCarId(UUID uuid) {
        if (uuid != null)
            carIds.add(uuid);
    }

    public void addCarDocumentationId(UUID uuid) {
        if (uuid != null)
            carDocumentationsIds.add(uuid);
    }

    public void addCarTokenId(UUID uuid) {
        if (uuid != null)
            carTokenIds.add(uuid);
    }

    public void addInsuranceCaseId(UUID uuid) {
        if (uuid != null)
            insuranceCaseIds.add(uuid);
    }

    private void addColourId(UUID uuid) {
        if (uuid != null)
            colourIds.add(uuid);
    }

    public void addDriverId(UUID uuid) {
        if (uuid != null)
            driverIds.add(uuid);
    }

    public void addDebtorId(UUID uuid) {
        if (uuid != null)
            debtorIds.add(uuid);
    }

    private void addUserId(UUID uuid) {
        if (uuid != null)
            userIds.add(uuid);
    }

    private void addRoleId(UUID uuid) {
        if (uuid != null)
            roleIds.add(uuid);
    }

    private void addGroupId(UUID uuid) {
        if (uuid != null)
            groupIds.add(uuid);
    }

    private void addPermissionId(UUID uuid) {
        if (uuid != null)
            permissionIds.add(uuid);
    }

    public void addModelId(UUID uuid) {
        if (uuid != null)
            modelIds.add(uuid);
    }

    public void addPlantId(UUID uuid) {
        if (uuid != null)
            plantIds.add(uuid);
    }

    private void addDriverAllocId(UUID uuid) {
        if (uuid != null)
            driverAllocIds.add(uuid);
    }

    public void addRepairId(UUID uuid) {
        if (uuid != null)
            repairIds.add(uuid);
    }

    private void addDriverGroupId(UUID uuid) {
        if (uuid != null)
            driverGroupIds.add(uuid);
    }

    private void addConstraintId(UUID uuid) {
        if (uuid != null)
            constraintIds.add(uuid);
    }

    public void addCurrencyId(String uuid) {
        if (uuid != null) {
            currencyIds.add(uuid);
        }
    }

    public void addCaseId(UUID uuid) {
        if (uuid != null) {
            caseIds.add(uuid);
        }
    }

    public void addCategoryAttributeValueId(UUID uuid) {
        if (uuid != null) {
            categoryAttributeValueIds.add(uuid);
        }
    }

    private void addId(UUID result, Set<UUID> ids) {
        if (result != null)
            ids.add(result);
    }

    public void cleanup(Connection conn) throws SQLException {
        deletePlantModelLinks(conn);
        deleteDriverAllocs(conn);
        deleteCarTokens(conn);
        deleteRepairs(conn);
        deleteInstances(conn, "REF_INSURANCE_CASE", insuranceCaseIds);
        deleteCars(conn);
        deleteCarDocumentations(conn);
        deleteModels(conn);
        deletePlants(conn);
        deleteColours(conn);
        deleteDrivers(conn);
//        deletePermissions(conn);
        deleteUserRoles(conn);
        deleteRoles(conn);
        deleteUsers(conn);
        //deleteDriverGroups(conn);
        //deleteInstances(conn, "DEBT_CASE", caseIds);
        //deleteInstances(conn, "DEBT_DEBTOR", debtorIds);
        deleteSellers(conn);
        deleteRecursiveEntities(conn);
        deleteProducts(conn);
        deleteOrders(conn);
        deleteInstances(conn, "DYNAT_ATTR_VALUE", categoryAttributeValueIds);
        deleteInstances(conn, "DYNAT_CATEGORY_ATTR", categoryAttributeIds);
        deleteInstances(conn, "DYNAT_CATEGORY", categoryIds);
//        deleteInstances(conn, "SEC_CONSTRAINT", constraintIds);
        deleteInstances(conn, "SAMPLE_REST_SEC_GROUP", groupIds);
        deleteInstances(conn, "REF_PLANT", plantIds);
        deleteInstances(conn, "REST_VALIDATED_ENTITY", validatedEntityIds);
        deleteInstances(conn, "REST_SECRET_ENTITY", secretEntityIds);
        deleteStringInstances(conn, "REF_CURRENCY", "CODE", currencyIds);
        deleteNonStandardIdEntities(conn);
    }

    private void deleteSellers(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_seller where id = ?");
        try {
            for (Long sellerId : sellerIds) {
                stmt.setObject(1, sellerId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteCars(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_car where id = ?");
        try {
            for (UUID carId : carIds) {
                stmt.setObject(1, carId);
                stmt.executeUpdate();
            }

        } finally {
            stmt.close();
        }
    }

    private void deleteCarDocumentations(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_car_documentation where id = ?");
        try {
            for (UUID carDocumentationId : carDocumentationsIds) {
                stmt.setObject(1, carDocumentationId);
                stmt.executeUpdate();
            }

        } finally {
            stmt.close();
        }
    }

    private void deleteCarTokens(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_car_token where id = ?");
        try {
            for (UUID carTokenId : carTokenIds) {
                stmt.setObject(1, carTokenId);
                stmt.executeUpdate();
            }

        } finally {
            stmt.close();
        }
    }

    private void deleteColours(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_colour where id = ?");
        try {
            for (UUID carId : colourIds) {
                stmt.setObject(1, carId);
                stmt.executeUpdate();
            }

        } finally {
            stmt.close();
        }
    }

    private void deleteDrivers(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_driver where id = ?");
        try {
            for (UUID carId : driverIds) {
                stmt.setObject(1, carId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteUsers(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from sample_rest_sec_user where id = ?");
        try {
            for (UUID userId : userIds) {
                stmt.setObject(1, userId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteRoles(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from sample_rest_sec_role where id = ?");
        try {
            for (UUID roleId : roleIds) {
                stmt.setObject(1, roleId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deletePermissions(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from sec_permission where id = ?");
        try {
            for (UUID permissionId : permissionIds) {
                stmt.setObject(1, permissionId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteModels(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_model where id = ?");
        try {
            for (UUID modelId : modelIds) {
                stmt.setObject(1, modelId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deletePlants(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_plant where id = ?");
        try {
            for (UUID plantId : plantIds) {
                stmt.setObject(1, plantId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteDriverAllocs(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_driver_alloc where id = ?");
        try {
            for (UUID driverAllocId : driverAllocIds) {
                stmt.setObject(1, driverAllocId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteRepairs(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_repair where id = ?");
        try {
            for (UUID repairId : repairIds) {
                stmt.setObject(1, repairId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deletePlantModelLinks(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_plant_model_link where plant_id = ?");
        try {
            for (UUID plantId : plantIds) {
                stmt.setObject(1, plantId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    /**
     * Deletes all references between created users and created roles.
     * Must be executed before deleteRoles() & deleteUsers
     */
    private void deleteUserRoles(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from sample_rest_sec_user_role where user_id = ?");
        try {
            for (UUID userId : userIds) {
                stmt.setObject(1, userId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteDriverGroups(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from ref_driver_group where id = ?");
        try {
            for (UUID driverGroupId : driverGroupIds) {
                stmt.setObject(1, driverGroupId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteCompositeKeyEntities(Connection conn) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from rest_composite_key where entity_id = ?");
        try {
            for (Long entityId : compositeKeyEntityIds) {
                stmt.setLong(1, entityId);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
        stmt = conn.prepareStatement("delete from ref_composite_key where tenant = ?");
        try {
            for (Integer tenant : compositeKeyEntityTenantIds) {
                stmt.setInt(1, tenant);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteRecursiveEntities(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate("delete from rest_recursive_entity");
        } finally {
            stmt.close();
        }
    }

    private void deleteOrders(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate("delete from rest_order");
        } finally {
            stmt.close();
        }
    }

    private void deleteProducts(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate("delete from rest_product");
        } finally {
            stmt.close();
        }
    }

    private void deleteNonStandardIdEntities(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate("delete from REST_NSIN_ENTITY");
        } finally {
            stmt.close();
        }
    }

    private void deleteStringInstances(Connection conn, String tableName, String idColumn, Set<String> ids) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from " + tableName + " where " + idColumn + " = ?");
        try {
            for (String id : ids) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    private void deleteInstances(Connection conn, String tableName, Set<UUID> ids) throws SQLException {
        PreparedStatement stmt;
        stmt = conn.prepareStatement("delete from " + tableName + " where id = ?");
        try {
            for (UUID uuid : ids) {
                stmt.setObject(1, uuid);
                stmt.executeUpdate();
            }
        } finally {
            stmt.close();
        }
    }

    public UUID createCarUuid() {
        UUID result = UUID.randomUUID();
        addCarId(result);
        return result;
    }

    public UUID createCarDocumentationUuid() {
        UUID result = UUID.randomUUID();
        addCarDocumentationId(result);
        return result;
    }

    public UUID createInsuranceCaseUuid() {
        UUID result = UUID.randomUUID();
        addInsuranceCaseId(result);
        return result;
    }

    public UUID createColourUuid() {
        UUID result = UUID.randomUUID();
        addColourId(result);
        return result;
    }

    public UUID createDriverUuid() {
        UUID result = UUID.randomUUID();
        addDriverId(result);
        return result;
    }

    public UUID createDebtorUuid() {
        UUID result = UUID.randomUUID();
        addDebtorId(result);
        return result;
    }

    public UUID createUserUuid() {
        UUID result = UUID.randomUUID();
        addUserId(result);
        return result;
    }

    public UUID createRoleUuid() {
        UUID result = UUID.randomUUID();
        addRoleId(result);
        return result;
    }

    public UUID createGroupUuid() {
        UUID result = UUID.randomUUID();
        addGroupId(result);
        return result;
    }

    public UUID createConstraintUuid() {
        UUID result = UUID.randomUUID();
        addConstraintId(result);
        return result;
    }

    public UUID createPermissionUuid() {
        UUID result = UUID.randomUUID();
        addPermissionId(result);
        return result;
    }

    public UUID createDriverAllocUuid() {
        UUID result = UUID.randomUUID();
        addDriverAllocId(result);
        return result;
    }

    public UUID createModelUuid() {
        UUID result = UUID.randomUUID();
        addModelId(result);
        return result;
    }

    public UUID createPlantUuid() {
        UUID result = UUID.randomUUID();
        addPlantId(result);
        return result;
    }

    public UUID createRepairUuid() {
        UUID result = UUID.randomUUID();
        addRepairId(result);
        return result;
    }

    public UUID createDriverGroupUuid() {
        UUID result = UUID.randomUUID();
        addDriverGroupId(result);
        return result;
    }

    public UUID createCaseUuid() {
        UUID result = UUID.randomUUID();
        addId(result, caseIds);
        return result;
    }

    public UUID createDebtorUUID() {
        UUID result = UUID.randomUUID();
        addId(result, debtorIds);
        return result;
    }

    public Long createSellerId() {
        Long result = 1000000L;
        sellerIds.add(result);
        return result;
    }

    public UUID createCategoryId() {
        UUID result = UUID.randomUUID();
        categoryIds.add(result);
        return result;
    }

    public UUID createCategoryAttributeId() {
        UUID result = UUID.randomUUID();
        categoryAttributeIds.add(result);
        return result;
    }

    public UUID createCategoryAttributeValueId() {
        UUID result = UUID.randomUUID();
        categoryAttributeValueIds.add(result);
        return result;
    }

    public UUID createConstraintId() {
        UUID result = UUID.randomUUID();
        addConstraintId(result);
        return result;
    }


    public Long createCompositeKeyEntityId() {
        Long result = compositeKeyEntityIdGen.incrementAndGet();
        compositeKeyEntityIds.add(result);
        return result;
    }

    public Integer createCompositeKeyEntityTenantId() {
        Integer result = compositeKeyEntityTenantIdGen.incrementAndGet();
        compositeKeyEntityTenantIds.add(result);
        return result;
    }

    public Integer createNonStandardIdNameEntityId() {
        Integer result = nonStandardIdNameEntityIdGen.incrementAndGet();
        nonStandardIdNameEntityIds.add(result);
        return result;
    }

    public UUID createValidatedEntityId() {
        UUID result = UUID.randomUUID();
        validatedEntityIds.add(result);
        return result;
    }

    public UUID createSecretEntityId() {
        UUID result = UUID.randomUUID();
        secretEntityIds.add(result);
        return result;
    }
}
