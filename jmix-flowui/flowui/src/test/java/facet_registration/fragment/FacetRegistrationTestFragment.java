/*
 * Copyright 2025 Haulmont.
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

package facet_registration.fragment;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import facet_registration.ExtFragmentDataLoadCoordinatorFacet;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewComponent;

@FragmentDescriptor("facet-registration-test-fragment.xml")
public class FacetRegistrationTestFragment extends Fragment<VerticalLayout> {

    // replaced ID by loader
    @ViewComponent
    public ExtFragmentDataLoadCoordinatorFacet extFragmentDataLoadCoordinatorFacet;
}
