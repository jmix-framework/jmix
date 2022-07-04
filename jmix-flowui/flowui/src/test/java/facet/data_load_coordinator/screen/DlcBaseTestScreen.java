/*
 * Copyright (c) 2020 Haulmont.
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

package facet.data_load_coordinator.screen;

import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.ComponentId;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Target;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.petclinic.Owner;
import test_support.entity.petclinic.OwnerCategory;
import test_support.entity.petclinic.Pet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DlcBaseTestScreen extends StandardView {

    @Autowired
    private Metadata metadata;

    public static class LoadEvent {
        public final String loader;
        public final LoadContext loadContext;

        public LoadEvent(String loader, LoadContext loadContext) {
            this.loader = loader;
            this.loadContext = loadContext;
        }
    }

    public List<LoadEvent> events = new ArrayList<>();

    @ComponentId
    public DataLoadCoordinator dlc;

    @ComponentId
    public CollectionContainer<Owner> ownersDc;

    @ComponentId
    public TypedTextField<String> nameFilterField;

    @ComponentId
    public EntityPicker<OwnerCategory> categoryFilterField;

    @Install(to = "ownersDl", target = Target.DATA_LOADER)
    private List<Owner> ownersDlLoadDelegate(LoadContext<Owner> loadContext) {
        events.add(new LoadEvent("ownersDl", loadContext));

        List<Owner> list = new ArrayList<>();
        Owner owner = metadata.create(Owner.class);
        owner.setName("Joe");
        list.add(owner);
        if (loadContext.getQuery().getParameters().isEmpty()) {
            owner = metadata.create(Owner.class);
            owner.setName("Jane");
            list.add(owner);
        }
        return list;
    }

    @Install(to = "petsDl", target = Target.DATA_LOADER)
    private List<Pet> petsDlLoadDelegate(LoadContext<Pet> loadContext) {
        events.add(new LoadEvent("petsDl", loadContext));

        Pet pet = metadata.create(Pet.class);
        pet.setName("Misty");
        return Collections.singletonList(pet);
    }
}
