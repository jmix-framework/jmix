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

package facet.data_load_coordinator.screen;

import com.vaadin.flow.router.Route;
import facet.data_load_coordinator.fragment.DlcAutoProvidedParamTestFragment;
import facet.data_load_coordinator.fragment.DlcAutoTestFragment;
import facet.data_load_coordinator.fragment.DlcManualNoParamTestFragment;
import facet.data_load_coordinator.fragment.DlcManualTestFragment;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route("facet/dlc/dlc-fragment-host-test-view")
@ViewController
@ViewDescriptor("dlc-fragment-host-test-view.xml")
public class DlcFragmentHostTestView extends StandardView {

    @ViewComponent
    public DlcAutoProvidedParamTestFragment dlcAutoProvidedParamFragment;

    @ViewComponent
    public DlcAutoTestFragment dlcAutoFragment;

    @ViewComponent
    public DlcManualNoParamTestFragment dlcManualNoParamFragment;

    @ViewComponent
    public DlcManualTestFragment dlcManualFragment;
}
