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

package component.fragment.component;

import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import test_support.entity.petclinic.City;
import test_support.entity.petclinic.Country;

@FragmentDescriptor("test-address-fragment-provided.xml")
public class TestAddressFragmentProvided extends Fragment<FormLayout> {

    public EntityComboBox<Country> getCountryField() {
        return getInnerComponent("countryField");
    }

    public EntityComboBox<City> getCityField() {
        return getInnerComponent("cityField");
    }

    public TypedTextField<String> getPostcodeField() {
        return getInnerComponent("postcodeField");
    }
}
