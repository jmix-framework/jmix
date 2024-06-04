/*
 * Copyright 2022 Haulmont.
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

package settings_facet.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.view.*;
import test_support.view.TestMainView;

@Route(value = "FacetDelegateTestView", layout = TestMainView.class)
@ViewController
@ViewDescriptor("facet-delegate-test-view.xml")
public class FacetDelegateTestView extends StandardView {

    @ViewComponent
    public SettingsFacet facet;

    public int calls = 0;

    @Install(to="facet", subject = "saveSettingsDelegate")
    protected void saveSettings(SettingsFacet.SettingsContext settings) {
        ++calls;
    }

    @Install(to="facet", subject = "applyDataLoadingSettingsDelegate")
    protected void applyDataLoadingSettings(SettingsFacet.SettingsContext settings) {
        ++calls;
    }

    @Install(to="facet", subject = "applySettingsDelegate")
    protected void applySettings(SettingsFacet.SettingsContext settings) {
        ++calls;
    }
}
