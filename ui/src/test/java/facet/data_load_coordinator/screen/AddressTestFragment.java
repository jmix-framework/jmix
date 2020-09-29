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
import io.jmix.ui.component.DataLoadCoordinator;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.petclinic.City;
import test_support.entity.petclinic.Country;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UiController("pc_AddressTestFragment")
@UiDescriptor("address-test-fragment.xml")
public class AddressTestFragment extends ScreenFragment {

    @Autowired
    private Metadata metadata;

    @Autowired
    private EntityComboBox<City> cityField;

    @Autowired
    public DataLoadCoordinator addressDlc;

    @Autowired
    public CollectionContainer<Country> countriesDc;

    public List<LoadEvent> events = new ArrayList<>();

    @Subscribe(id = "countriesDc", target = Target.DATA_CONTAINER)
    private void onCountriesDcItemChange(InstanceContainer.ItemChangeEvent<Country> event) {
        cityField.setValue(null);
    }

    @Install(to = "countriesDl", target = Target.DATA_LOADER)
    private List<Country> countriesDlLoadDelegate(LoadContext<Country> loadContext) {
        events.add(new LoadEvent("countriesDl", loadContext));
        return Collections.singletonList(metadata.create(Country.class));
    }

    @Install(to = "citiesDl", target = Target.DATA_LOADER)
    private List<City> citiesDlLoadDelegate(LoadContext<City> loadContext) {
        events.add(new LoadEvent("citiesDl", loadContext));
        return Collections.singletonList(metadata.create(City.class));
    }

    public static class LoadEvent {
        public final String loader;
        public final LoadContext loadContext;

        public LoadEvent(String loader, LoadContext loadContext) {
            this.loader = loader;
            this.loadContext = loadContext;
        }
    }
}
