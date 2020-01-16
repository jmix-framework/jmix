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

package io.jmix.core;

import com.sample.addon1.TestAddon1Configuration;
import com.sample.app.TestAppConfiguration;
import com.sample.app.entity.Pet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmixCoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class FluentLoaderViewBuilderTest {

    @Inject
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
                .view(viewBuilder -> viewBuilder.addView(View.MINIMAL).addAll(
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
                .view(viewBuilder -> viewBuilder.addView(View.LOCAL).addAll(
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
        LoadContext<Pet> loadContext = dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addAll(
                        "name",
                        "owner.name",
                        "owner.address.city"))
                .createLoadContext();

        View view = loadContext.getView();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);

        loadContext = dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addView(View.LOCAL).addAll(
                        "owner.name",
                        "owner.address.city"))
                .createLoadContext();

        view = loadContext.getView();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);

        loadContext = dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addView(View.LOCAL).addAll(
                        "owner.name",
                        "owner.address.city"))
                .createLoadContext();

        view = loadContext.getView();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);

        loadContext = dataManager.load(Pet.class)
                .viewProperties(
                        "name",
                        "owner.name",
                        "owner.address.city")
                .createLoadContext();

        view = loadContext.getView();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);

        loadContext = dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addView(View.LOCAL))
                .viewProperties(
                        "owner.name",
                        "owner.address.city")
                .createLoadContext();

        view = loadContext.getView();
        assertFalse(containsSystemProperties(view));
        checkPetView(view);
    }

    @Test
    public void testViewWithViewBuilder() {
        LoadContext<Pet> loadContext = dataManager.load(Pet.class)
                .view(View.LOCAL)
                .view(viewBuilder -> viewBuilder.addAll(
                        "owner.name",
                        "owner.address.city"))
                .createLoadContext();

        View view = loadContext.getView();
        checkPetView(view);

        loadContext = dataManager.load(Pet.class)
                .view(viewBuilder -> viewBuilder.addAll(
                        "owner.name",
                        "owner.address.city"))
                .view(View.LOCAL)
                .createLoadContext();

        view = loadContext.getView();
        checkPetView(view);
    }

    private void checkPetView(View view) {
        assertTrue(view.containsProperty("name"));

        assertNotNull(view.getProperty("owner"));
        View ownerView = view.getProperty("owner").getView();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertTrue(ownerView.containsProperty("address"));

        View addressView = ownerView.getProperty("address").getView();
        assertTrue(addressView.containsProperty("city"));
    }

    private boolean containsSystemProperties(View view) {
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