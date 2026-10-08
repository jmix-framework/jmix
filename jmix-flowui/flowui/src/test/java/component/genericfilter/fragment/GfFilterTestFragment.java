/*
 * Copyright 2026 Haulmont.
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

package component.genericfilter.fragment;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewComponent;

/**
 * A fragment that hosts a {@code genericFilter}, used to check that the filter behaves
 * inside a fragment as it does directly in a view.
 */
@FragmentDescriptor("gf-filter-test-fragment.xml")
public class GfFilterTestFragment extends Fragment<VerticalLayout> {

    @ViewComponent
    public GenericFilter genericFilter;
}
