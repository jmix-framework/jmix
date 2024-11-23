/*
 * Copyright 2024 Haulmont.
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

package importexport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.*;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.DataTestConfiguration;
import test_support.TestContextInititalizer;
import test_support.entity.importexport.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, DataTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
public class EntityImportExportTest {

    @Autowired
    EntityImportExport entityImportExport;
    @Autowired
    EntityImportPlans entityImportPlans;
    @Autowired
    Metadata metadata;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    TransactionTemplate transaction;
    @Autowired
    DataManager dataManager;
    @Autowired
    FetchPlans fetchPlans;

    @PersistenceContext
    EntityManager em;

    Role role1;
    Permission permission1;
    Permission permission2;
    Currency currency;
    PricingRegion pricingRegion1;
    PricingRegion pricingRegion2;
    Car car;
    Repair repair1;
    Repair repair2;
    Driver driver;

    Plant plant;
    Model model1;
    Model model2;

    @BeforeEach
    void setUp() {
        pricingRegion1 = metadata.create(PricingRegion.class);
        pricingRegion1.setName("region1");

        pricingRegion2 = metadata.create(PricingRegion.class);
        pricingRegion2.setName("region2");
        pricingRegion2.setParent(pricingRegion1);

        role1 = metadata.create(Role.class);
        role1.setName("Role 1");
        role1.setType(RoleType.DENYING);
        role1.setDescription("Role1 description");

        permission1 = metadata.create(Permission.class);
        permission1.setRole(role1);
        permission1.setType(PermissionType.SCREEN);
        permission1.setTarget("permission 1 target");

        Set<Permission> permissions = new HashSet<>();
        permissions.add(permission1);

        permission2 = metadata.create(Permission.class);

        currency = metadata.create(Currency.class);
        currency.setCode("USD");
        currency.setName("Dollar");

        car = metadata.create(Car.class);
        car.setVin("001");

        repair1 = metadata.create(Repair.class);
        repair1.setDescription("Repair 1");
        repair1.setCar(car);

        repair2 = metadata.create(Repair.class);
        repair2.setDescription("Repair 2");
        repair2.setCar(car);

        role1.setPermissions(permissions);

        driver = metadata.create(Driver.class);
        driver.setName("John Smith");

        Address address = metadata.create(Address.class);
        address.setCountry("Russia");
        address.setCity("Samara");
        driver.setAddress(address);

        plant = metadata.create(Plant.class);

        model1 = metadata.create(Model.class);
        model1.setName("Model1");
        model2 = metadata.create(Model.class);
        model2.setName("Model2");
    }

    @AfterEach
    void tearDown() {
        jdbc.update("delete from TESTIMPORTEXPORT_PERMISSION");
        jdbc.update("delete from TESTIMPORTEXPORT_ROLE");
        jdbc.update("delete from TESTIMPORTEXPORT_CURRENCY");
        jdbc.update("delete from TESTIMPORTEXPORT_PRICING_REGION");
        jdbc.update("delete from TESTIMPORTEXPORT_REPAIR");
        jdbc.update("delete from TESTIMPORTEXPORT_CAR");
        jdbc.update("delete from TESTIMPORTEXPORT_DRIVER");
        jdbc.update("delete from TESTIMPORTEXPORT_PLANT_MODEL_LINK");
        jdbc.update("delete from TESTIMPORTEXPORT_PLANT");
        jdbc.update("delete from TESTIMPORTEXPORT_MODEL");
    }

    @Test
    void testNewOneToMany() {
        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(role1);

        EntityImportPlan importView = entityImportPlans.builder(Role.class)
                .addLocalProperty("name")
                .addLocalProperty("type")
                .addOneToManyProperty("permissions",
                        entityImportPlans.builder(Permission.class).addLocalProperty("type").build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Role loadedRole1 = em.find(Role.class, role1.getId());
            assertNotNull(loadedRole1);
            assertEquals("Role 1", loadedRole1.getName());
            assertEquals(RoleType.DENYING, loadedRole1.getType());
            assertNull(loadedRole1.getDescription());

            assertEquals(1, loadedRole1.getPermissions().size());
            Permission loadedPermission1 = loadedRole1.getPermissions().iterator().next();
            assertEquals(PermissionType.SCREEN, loadedPermission1.getType());
            assertEquals(loadedRole1, loadedPermission1.getRole());
            assertNull(loadedPermission1.getTarget());
        });
    }

    @Test
    void testErrorOnMissingManyToOneReference() {
        try {
            ArrayList<Object> entitiesToPersist = new ArrayList<>();
            entitiesToPersist.add(permission1);
            EntityImportPlan importView = entityImportPlans.builder(Permission.class)
                    .addLocalProperties()
                    .addManyToOneProperty("role", ReferenceImportBehaviour.ERROR_ON_MISSING)
                    .build();

            entityImportExport.importEntities(entitiesToPersist, importView);

            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).contains("Referenced entity for property 'role' is missing");
        }
    }

    @Test
    void testIgnoreMissingManyToOneReference() {
        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(permission1);
        EntityImportPlan importView = entityImportPlans.builder(Permission.class)
                .addLocalProperties()
                .addManyToOneProperty("role", ReferenceImportBehaviour.IGNORE_MISSING)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Permission reloadedPermission = em.find(Permission.class, permission1.getId());

            assertNotNull(reloadedPermission);
            assertEquals("permission 1 target", reloadedPermission.getTarget());
            assertEquals(PermissionType.SCREEN, reloadedPermission.getType());
            assertNull(reloadedPermission.getRole());
        });
    }

    @Test
    void testCreateManyToOneReference() {
        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(permission1);
        EntityImportPlan importView = entityImportPlans.builder(Permission.class)
                .addLocalProperties()
                .addManyToOneProperty("role", entityImportPlans.builder(Role.class).addLocalProperties().build())
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Permission reloadedPermission = em.find(Permission.class, permission1.getId());

            assertNotNull(reloadedPermission);
            assertEquals("permission 1 target", reloadedPermission.getTarget());
            assertEquals(PermissionType.SCREEN, reloadedPermission.getType());
            Role reloadedRole = reloadedPermission.getRole();
            assertNotNull(reloadedRole);
            assertEquals("Role 1", reloadedRole.getName());
        });
    }

    @Test
    void testCreateManyToOneReferenceIfReferenceIsNull() {
        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(permission1);
        permission1.setRole(role1);
        EntityImportPlan importView = entityImportPlans.builder(Permission.class)
                .addLocalProperties()
                .addManyToOneProperty("role", entityImportPlans.builder(Role.class).addLocalProperties().build())
                .build();

        //first we import the permission with the "role" reference filled
        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Permission reloadedPermission = em.find(Permission.class, permission1.getId());

            assertNotNull(reloadedPermission);
            assertEquals("permission 1 target", reloadedPermission.getTarget());
            assertEquals(PermissionType.SCREEN, reloadedPermission.getType());
            Role reloadedRole = reloadedPermission.getRole();
            assertNotNull(reloadedRole);
            assertEquals("Role 1", reloadedRole.getName());
        });

        //then we set the role reference to NULL and import the permission again. The "role"
        //reference must be cleared
        permission1.setRole(null);
        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Permission reloadedPermission = em.find(Permission.class, permission1.getId());

            assertNotNull(reloadedPermission);
            assertEquals("permission 1 target", reloadedPermission.getTarget());
            assertEquals(PermissionType.SCREEN, reloadedPermission.getType());
            Role reloadedRole = reloadedPermission.getRole();
            assertNull(reloadedRole);
        });
    }

    @Test
    void testOneToManyKeepAbsentCollectionItems() {
        permission2 = metadata.create(Permission.class);
        permission2.setRole(role1);
        permission2.setType(PermissionType.SCREEN);
        permission2.setTarget("permission 2 target");

        role1.getPermissions().add(permission1);
        role1.getPermissions().add(permission2);

        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(role1);
        EntityImportPlan importView = entityImportPlans.builder(Role.class)
                .addLocalProperties()
                .addOneToManyProperty("permissions",
                        entityImportPlans.builder(Permission.class).addLocalProperties().build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Role reloadedRole = em.find(Role.class, role1.getId());
            assertNotNull(reloadedRole);
            assertEquals(role1.getName(), reloadedRole.getName());

            //check that both permissions were initially imported
            assertEquals(2, reloadedRole.getPermissions().size());
        });

        //next import the same role but with only one permission in the collection
        role1.setName("role1 new name");

        role1.getPermissions().clear();
        role1.getPermissions().add(permission1);

        entitiesToPersist.clear();
        entitiesToPersist.add(role1);

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Role reloadedRole = em.find(Role.class, role1.getId());
            assertNotNull(reloadedRole);
            assertEquals(role1.getName(), reloadedRole.getName());

            //check that permission2 was not removed
            assertEquals(2, reloadedRole.getPermissions().size());
        });
    }

    @Test
    void testOneToManyRemoveAbsentCollectionItems() {
        permission2 = metadata.create(Permission.class);
        permission2.setRole(role1);
        permission2.setType(PermissionType.SCREEN);
        permission2.setTarget("permission 2 target");

        role1.getPermissions().add(permission1);
        role1.getPermissions().add(permission2);

        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(role1);
        EntityImportPlan importView = entityImportPlans.builder(Role.class)
                .addLocalProperties()
                .addOneToManyProperty("permissions",
                        entityImportPlans.builder(Permission.class).addLocalProperties().build(),
                        CollectionImportPolicy.REMOVE_ABSENT_ITEMS)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Role reloadedRole = em.find(Role.class, role1.getId());
            assertNotNull(reloadedRole);
            assertEquals(role1.getName(), reloadedRole.getName());

            //check that both permissions were initially imported
            assertEquals(2, reloadedRole.getPermissions().size());
        });

        //next import the same role but with only one permission in the collection
        role1.setName("role1 new name");

        role1.getPermissions().clear();
        role1.getPermissions().add(permission1);

        entitiesToPersist.clear();
        entitiesToPersist.add(role1);

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Role reloadedRole = em.find(Role.class, role1.getId());
            assertNotNull(reloadedRole);
            assertEquals(role1.getName(), reloadedRole.getName());

            //check that permission2 was removed
            assertEquals(1, reloadedRole.getPermissions().size());
            assertEquals(permission1, reloadedRole.getPermissions().iterator().next());
        });
    }

    @Test
    void testManyToManyCreateItemsAndRemoveAbsentCollectionItems() {
        transaction.executeWithoutResult(transactionStatus -> {
            Set<Model> modelsList = new HashSet<>();
            modelsList.add(model1);
            plant.setModels(modelsList);
            em.persist(model1);
            em.persist(plant);
        });

        transaction.executeWithoutResult(transactionStatus -> {
            Plant reloadedPlant = em.find(Plant.class, plant.getId());
            assertEquals(1, reloadedPlant.getModels().size());
        });

        Set<Model> modelsList = new HashSet<>();
        modelsList.add(model2);
        plant.setModels(modelsList);

        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(plant);

        EntityImportPlan importView = entityImportPlans.builder(Plant.class)
                .addLocalProperty("name")
                .addManyToManyProperty("models",
                        entityImportPlans.builder(Model.class).addLocalProperty("name").build(),
                        CollectionImportPolicy.REMOVE_ABSENT_ITEMS)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Plant reloadedPlant = em.find(Plant.class, plant.getId());
            assertNotNull(reloadedPlant);
            assertEquals(plant.getName(), reloadedPlant.getName());

            //now one of repairs was removed
            assertEquals(1, reloadedPlant.getModels().size());
            assertEquals("Model2", reloadedPlant.getModels().iterator().next().getName());

            //model1 entity was not removed from the DB, just from the plant.models collection
            Model reloadedModel1 = em.find(Model.class, model1.getId());
            assertNotNull(reloadedModel1);
        });
    }

    @Test
    void testManyToManyUpdateItemsAndKeepAbsentCollectionItems() {
        transaction.executeWithoutResult(transactionStatus -> {
            Set<Model> modelsList = new HashSet<>();
            modelsList.add(model1);
            modelsList.add(model2);
            plant.setModels(modelsList);
            em.persist(model1);
            em.persist(model2);
            em.persist(plant);
        });

        transaction.executeWithoutResult(transactionStatus -> {
            Plant reloadedPlant = em.find(Plant.class, plant.getId());
            //ensure that the car has two repairs
            assertEquals(2, reloadedPlant.getModels().size());
        });

        Set<Model> modelsList = new HashSet<>();
        model1.setName("Model 1 (modified)");
        modelsList.add(model1);
        plant.setModels(modelsList);

        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(plant);

        EntityImportPlan importView = entityImportPlans.builder(Plant.class)
                .addLocalProperty("name")
                .addManyToManyProperty("models",
                        entityImportPlans.builder(Model.class).addLocalProperty("name").build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Plant reloadedPlant = em.find(Plant.class, plant.getId());
            assertNotNull(reloadedPlant);
            assertEquals(plant.getName(), reloadedPlant.getName());

            //none of repairs was removed
            assertEquals(2, reloadedPlant.getModels().size());
        });
    }

    @Test
    void testManyToManyFindReferenceRemoveAbsentCollectionItems() {
        transaction.executeWithoutResult(transactionStatus -> {
            Set<Model> modelsList = new HashSet<>();
            modelsList.add(model1);
            modelsList.add(model2);
            plant.setModels(modelsList);

            em.persist(model1);
            em.persist(model2);
            em.persist(plant);
        });

        Model model3 = metadata.create(Model.class);

        Set<Model> modelsList = new HashSet<>();
        modelsList.add(model1);
        modelsList.add(model3);
        plant.setModels(modelsList);

        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(plant);

        EntityImportPlan importView = entityImportPlans.builder(Plant.class)
                .addLocalProperty("name")
                .addManyToManyProperty("models",
                        ReferenceImportBehaviour.IGNORE_MISSING,
                        CollectionImportPolicy.REMOVE_ABSENT_ITEMS)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Plant reloadedPlant = em.find(Plant.class, plant.getId());
            assertNotNull(reloadedPlant);
            assertEquals(plant.getName(), reloadedPlant.getName());

            //plant must have 1 model only: model3 is missing reference, module2 was removed from the collection
            assertEquals(1, reloadedPlant.getModels().size());
            assertEquals(model1.getName(), reloadedPlant.getModels().iterator().next().getName());
        });
    }

    @Test
    void testManyToManyFindReferenceKeepAbsentCollectionItems() {
        transaction.executeWithoutResult(transactionStatus -> {
            Set<Model> modelsList = new HashSet<>();
            modelsList.add(model1);
            modelsList.add(model2);
            plant.setModels(modelsList);

            em.persist(model1);
            em.persist(model2);
            em.persist(plant);
        });

        Model model3 = metadata.create(Model.class);
        model3.setName("Model 3");

        //we didn't add model1 to models list now - but later it must persist
        Set<Model> modelsList = new HashSet<>();
        modelsList.add(model2);
        modelsList.add(model3);
        plant.setModels(modelsList);

        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(plant);

        EntityImportPlan importView = entityImportPlans.builder(Plant.class)
                .addLocalProperty("name")
                .addManyToManyProperty("models",
                        ReferenceImportBehaviour.IGNORE_MISSING,
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Plant reloadedPlant = em.find(Plant.class, plant.getId());
            assertNotNull(reloadedPlant);
            assertEquals(plant.getName(), reloadedPlant.getName());

            //plant must have 2 models, because model2 must be removed, and model3 is missing reference
            assertEquals(2, reloadedPlant.getModels().size());
//            assertEquals(model1.getName(), reloadedPlant.getModels().iterator().next().getName());
        });
    }

    @Test
    void testManyToManyErrorOnMissingCollectionItems() {
        transaction.executeWithoutResult(transactionStatus -> {
            em.persist(plant);
        });

        Set<Model> modelsList = new HashSet<>();
        modelsList.add(model1);
        plant.setModels(modelsList);

        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(plant);

        EntityImportPlan importView = entityImportPlans.builder(Plant.class)
                .addLocalProperty("name")
                .addManyToManyProperty("models",
                        ReferenceImportBehaviour.ERROR_ON_MISSING,
                        CollectionImportPolicy.REMOVE_ABSENT_ITEMS)
                .build();

        try {
            entityImportExport.importEntities(entitiesToPersist, importView);
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).contains("Referenced entity for property 'models' is missing");
        }
    }

    @Test
    void testManyToManyCreateCollectionItems() {
        Set<Model> modelsList = new HashSet<>();
        modelsList.add(model1);
        plant.setModels(modelsList);

        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(plant);

        EntityImportPlan importView = entityImportPlans.builder(Plant.class)
                .addLocalProperty("name")
                .addManyToManyProperty("models",
                        entityImportPlans.builder(Model.class).addLocalProperties().build(),
                        CollectionImportPolicy.REMOVE_ABSENT_ITEMS)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Plant reloadedPlant = em.find(Plant.class, plant.getId());
            assertNotNull(reloadedPlant);
            assertEquals(plant.getName(), reloadedPlant.getName());

            //repair 1 must be created
            assertEquals(1, reloadedPlant.getModels().size());
            assertEquals(model1.getName(), reloadedPlant.getModels().iterator().next().getName());
        });
    }

    @Test
    void testImportDeletedEntity() {
        transaction.executeWithoutResult(transactionStatus -> {
            em.remove(role1);
        });

        EntityImportPlan importView = entityImportPlans.builder(Role.class)
                .addLocalProperties()
                .build();

        role1.setName("Modified role 1 name");

        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(role1);
        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Role reloadedRole = em.find(Role.class, role1.getId());

            assertNotNull(reloadedRole);
            assertEquals("Modified role 1 name", reloadedRole.getName());
            assertEquals(0, reloadedRole.getPermissions().size());

        });
    }

    @Test
    void testImportWhenReferenceIsBeforeEntityToBeCreated() {
        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(pricingRegion2);
        entitiesToPersist.add(pricingRegion1);

        EntityImportPlan importView = entityImportPlans.builder(PricingRegion.class)
                .addLocalProperties()
                .addManyToOneProperty("parent",
                        ReferenceImportBehaviour.ERROR_ON_MISSING)
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            PricingRegion reloadedRegion1 = em.find(PricingRegion.class, pricingRegion1.getId());
            PricingRegion reloadedRegion2 = em.find(PricingRegion.class, pricingRegion2.getId());

            assertNotNull(reloadedRegion1);
            assertNotNull(reloadedRegion2);
            assertEquals("region1", reloadedRegion1.getName());
            assertEquals("region2", reloadedRegion2.getName());
            assertEquals(reloadedRegion1, reloadedRegion2.getParent());

        });
    }

    @Test
    void testImportEntityWithEmbeddedAttr() {
        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(driver);

        EntityImportPlan importView = entityImportPlans.builder(Driver.class)
                .addLocalProperties()
                .addEmbeddedProperty("address", entityImportPlans.builder(Address.class).addLocalProperties().build())
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Driver reloadedDriver = em.find(Driver.class, driver.getId());

            assertNotNull(reloadedDriver);
            assertEquals("John Smith", reloadedDriver.getName());
            assertEquals("Russia", reloadedDriver.getAddress().getCountry());
            assertEquals("Samara", reloadedDriver.getAddress().getCity());

        });
    }

    @Test
    void testImportExistingEntityWithEmbeddedAttr() {
        transaction.executeWithoutResult(transactionStatus -> {
            em.persist(driver);
        });

        //reset country, but don't add it to the view, so it shouldn't be updated
        driver.getAddress().setCountry(null);
        driver.getAddress().setCity("Moscow");
        ArrayList<Object> entitiesToPersist = new ArrayList<>();
        entitiesToPersist.add(driver);

        EntityImportPlan importView = entityImportPlans.builder(Driver.class)
                .addLocalProperties()
                .addEmbeddedProperty("address", entityImportPlans.builder(Address.class).addLocalProperty("city").build())
                .build();

        entityImportExport.importEntities(entitiesToPersist, importView);

        transaction.executeWithoutResult(transactionStatus -> {
            Driver reloadedDriver = em.find(Driver.class, driver.getId());

            assertNotNull(reloadedDriver);
            assertEquals("John Smith", reloadedDriver.getName());
            assertEquals("Russia", reloadedDriver.getAddress().getCountry());
            assertEquals("Moscow", reloadedDriver.getAddress().getCity());
        });
    }

    @Test
    void testExportToJson() throws JsonProcessingException {
        dataManager.save(role1, permission1);

        FetchPlan fetchPlan = fetchPlans.builder(Role.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("permissions")
                .build();

        String json = entityImportExport.exportEntitiesToJSON(List.of(role1), fetchPlan);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<Role> roles = mapper.readValue(json, new TypeReference<>() { });

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getPermissions()).hasSize(1);
    }
}