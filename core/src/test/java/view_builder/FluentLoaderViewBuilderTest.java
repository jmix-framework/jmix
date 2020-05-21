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

package view_builder;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.JmixCoreConfiguration;
import io.jmix.core.LoadContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.Pet;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;

import static io.jmix.core.FluentLoaderTestAccess.createLoadContext;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JmixCoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class FluentLoaderViewBuilderTest {

    @Autowired
    DataManager dataManager;

    @Test
    public void testUsage() {
        UUID petId = UUID.randomUUID();

        dataManager.load(Pet.class)
                .id(petId)
                .view(viewBuilder -> viewBuilder.addAll(
                        "name",
                        "owner.name",
                        "owner.address.city"))
        /*.one()*/;

        dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addView(FetchPlan.MINIMAL).addAll(
                        "owner.name",
                        "owner.address.city"))
                .id(petId)
        /*.one()*/;

        dataManager.load(Pet.class)
                .id(petId)
                .viewProperties(
                        "name",
                        "owner.name",
                        "owner.address.city")
        /*.one()*/;

        dataManager.load(Pet.class)
                .query("...")
                .view(viewBuilder -> viewBuilder.addView(FetchPlan.LOCAL).addAll(
                        "owner.name",
                        "owner.address.city"))
        /*.list()*/;

        dataManager.load(Pet.class)
                .query("...")
                .viewProperties(
                        "name",
                        "owner.name",
                        "owner.address.city")
        /*.list()*/;
    }

    @Test
    public void testLoadContext() {
        //noinspection unchecked
        LoadContext<Pet> loadContext = createLoadContext(dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addAll(
                        "name",
                        "owner.name",
                        "owner.address.city")));

        FetchPlan view = loadContext.getFetchPlan();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);

        //noinspection unchecked
        loadContext = createLoadContext(dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addView(FetchPlan.LOCAL).addAll(
                        "owner.name",
                        "owner.address.city")));

        view = loadContext.getFetchPlan();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);

        //noinspection unchecked
        loadContext = createLoadContext(dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addView(FetchPlan.LOCAL).addAll(
                        "owner.name",
                        "owner.address.city")));

        view = loadContext.getFetchPlan();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);

        //noinspection unchecked
        loadContext = createLoadContext(dataManager.load(Pet.class)
                .viewProperties(
                        "name",
                        "owner.name",
                        "owner.address.city"));

        view = loadContext.getFetchPlan();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);

        //noinspection unchecked
        loadContext = createLoadContext(dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addView(FetchPlan.LOCAL))
                .viewProperties(
                        "owner.name",
                        "owner.address.city"));

        view = loadContext.getFetchPlan();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);
    }

    @Test
    public void testViewWithViewBuilder() {
        //noinspection unchecked
        LoadContext<Pet> loadContext = createLoadContext(dataManager.load(Pet.class)
                .view(FetchPlan.LOCAL)
                .view(viewBuilder -> viewBuilder.addAll(
                        "owner.name",
                        "owner.address.city")));

        FetchPlan view = loadContext.getFetchPlan();
        checkPetView(view);

        //noinspection unchecked
        loadContext = createLoadContext(dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addAll(
                        "owner.name",
                        "owner.address.city"))
                .view(FetchPlan.LOCAL));

        view = loadContext.getFetchPlan();
        checkPetView(view);
    }

    private void checkPetView(FetchPlan view) {
        assertTrue(view.containsProperty("name"));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertTrue(ownerView.containsProperty("address"));

        FetchPlan addressView = ownerView.getProperty("address").getFetchPlan();
        assertTrue(addressView.containsProperty("city"));
    }

    private boolean containsSystemProperties(FetchPlan view) {
        return view.containsProperty("id")
                && view.containsProperty("version")
                && view.containsProperty("deleteTs")
                && view.containsProperty("deletedBy")
                && view.containsProperty("createTs")
                && view.containsProperty("createdBy")
                && view.containsProperty("updateTs")
                && view.containsProperty("updatedBy");
    }
}
