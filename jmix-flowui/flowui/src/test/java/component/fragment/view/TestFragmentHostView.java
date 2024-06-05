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

package component.fragment.view;

import com.vaadin.flow.router.Route;
import component.fragment.component.TestAddressFragmentProvided;
import component.fragment.component.TestDataGridFragment;
import component.fragment.component.TestDataGridFragmentProvided;
import component.fragment.component.TestHostFragment;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.petclinic.Address;

@Route(value = "TestFragmentHostView/:id")
@ViewController
@ViewDescriptor("test-fragment-host-view.xml")
@EditedEntityContainer("addressDc")
public class TestFragmentHostView extends StandardDetailView<Address> {

    @ViewComponent
    public TestDataGridFragment dataGridFragment;
    @ViewComponent
    public TestDataGridFragmentProvided dataGridFragmentProvided;
    @ViewComponent
    public TestHostFragment hostFragment;

    @Autowired
    public Fragments fragments;

    public TestAddressFragmentProvided addressFragment;

    @Subscribe
    public void onInit(InitEvent event) {
        addressFragment = fragments.create(this, TestAddressFragmentProvided.class);
        getContent().add(addressFragment);
    }
}
