/*
 * Copyright 2019 Haulmont.
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

package fetch_plan_builder;

import io.jmix.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.Pet;

import java.util.List;
import java.util.UUID;

import static io.jmix.core.FluentLoaderTestAccess.createLoadContext;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class FluentLoaderFetchPlanBuilderTest {

    @Autowired
    ObjectProvider<FluentLoader> fluentLoaderProvider;
    @Autowired
    Metadata metadata;
    @Autowired
    MetadataTools metadataTools;


    @SuppressWarnings("unchecked")
    @Test
    public void testUsage() {
        UUID petId = UUID.randomUUID();

        FluentLoader<Pet> fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        fluentLoader
                .id(petId)
                .fetchPlan(fpBuilder -> fpBuilder.addFetchPlan(FetchPlan.INSTANCE_NAME).addAll(
                        "owner.name",
                        "owner.address.city"));

        fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        fluentLoader
                .id(petId)
                .fetchPlanProperties(
                        "name",
                        "owner.name",
                        "owner.address.city");

        fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        fluentLoader
                .query("...")
                .fetchPlan(fpBuilder -> fpBuilder.addFetchPlan(FetchPlan.LOCAL).addAll(
                        "owner.name",
                        "owner.address.city"));

        fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        fluentLoader
                .query("...")
                .fetchPlanProperties(
                        "name",
                        "owner.name",
                        "owner.address.city");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadContext() {
        FluentLoader<Pet> fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        LoadContext<Pet> loadContext = createLoadContext(fluentLoader.all()
                .fetchPlan(fpBuilder -> fpBuilder.addAll(
                        "name",
                        "owner.name",
                        "owner.address.city")));

        FetchPlan fetchPlan = loadContext.getFetchPlan();
        assertFalse(containsSystemProperties(fetchPlan));
        checkPetFetchPlan(fetchPlan);

        fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        loadContext = createLoadContext(fluentLoader.all()
                .fetchPlan(fpBuilder -> fpBuilder.addFetchPlan(FetchPlan.LOCAL).addAll(
                        "owner.name",
                        "owner.address.city")));

        fetchPlan = loadContext.getFetchPlan();
        assertTrue(containsSystemProperties(fetchPlan));//all system properties of Pet is local
        checkPetFetchPlan(fetchPlan);

        fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        loadContext = createLoadContext(fluentLoader.all()
                .fetchPlan(fpBuilder -> fpBuilder.addFetchPlan(FetchPlan.LOCAL).addAll(
                        "owner.name",
                        "owner.address.city")));

        fetchPlan = loadContext.getFetchPlan();
        assertTrue(containsSystemProperties(fetchPlan));
        checkPetFetchPlan(fetchPlan);

        fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        loadContext = createLoadContext(fluentLoader.all()
                .fetchPlanProperties(
                        "name",
                        "owner.name",
                        "owner.address.city"));

        fetchPlan = loadContext.getFetchPlan();
        assertFalse(containsSystemProperties(fetchPlan));
        checkPetFetchPlan(fetchPlan);

        fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        loadContext = createLoadContext(fluentLoader.all()
                .fetchPlan(fpBuilder -> fpBuilder.addFetchPlan(FetchPlan.LOCAL))
                .fetchPlanProperties(
                        "owner.name",
                        "owner.address.city"));

        fetchPlan = loadContext.getFetchPlan();
        assertTrue(containsSystemProperties(fetchPlan));
        checkPetFetchPlan(fetchPlan);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFetchPlanWithBuilder() {
        FluentLoader<Pet> fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        LoadContext<Pet> loadContext = createLoadContext(fluentLoader.all()
                .fetchPlan(FetchPlan.LOCAL)
                .fetchPlan(fpBuilder -> fpBuilder.addAll(
                        "owner.name",
                        "owner.address.city")));

        FetchPlan fetchPlan = loadContext.getFetchPlan();
        checkPetFetchPlan(fetchPlan);

        fluentLoader = fluentLoaderProvider.getObject(Pet.class);
        loadContext = createLoadContext(fluentLoader.all()
                .fetchPlan(fpBuilder -> fpBuilder.addAll(
                        "owner.name",
                        "owner.address.city"))
                .fetchPlan(FetchPlan.LOCAL));

        fetchPlan = loadContext.getFetchPlan();
        checkPetFetchPlan(fetchPlan);
    }

    private void checkPetFetchPlan(FetchPlan fetchPlan) {
        assertTrue(fetchPlan.containsProperty("name"));

        assertNotNull(fetchPlan.getProperty("owner"));
        FetchPlan ownerFetchPlan = fetchPlan.getProperty("owner").getFetchPlan();
        assertNotNull(ownerFetchPlan);
        assertFalse(containsSystemProperties(ownerFetchPlan));
        assertTrue(ownerFetchPlan.containsProperty("name"));
        assertTrue(ownerFetchPlan.containsProperty("address"));

        FetchPlan addressView = ownerFetchPlan.getProperty("address").getFetchPlan();
        assertTrue(addressView.containsProperty("city"));
    }

    private boolean containsSystemProperties(FetchPlan fetchPlan) {
        List<String> systemProperties = metadataTools.getSystemProperties(metadata.getClass(fetchPlan.getEntityClass()));
        return systemProperties.stream().allMatch(fetchPlan::containsProperty);
    }
}
