/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.web.screenfacet.screens;

import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ScreenFacet;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

import javax.inject.Inject;

@UiController("cuba_ScreenFacetTestScreen")
@UiDescriptor("screen-facet-test-screen.xml")
public class ScreenFacetTestScreen extends Screen {

    @Inject
    public Button button;
    @Inject
    public Action action;

    @Inject
    public ScreenFacet<ScreenToOpenWithFacet> screenIdFacet;
    @Inject
    public ScreenFacet<ScreenToOpenWithFacet> screenClassFacet;

    public boolean afterShowListenerTriggered = false;
    public boolean afterCloseListenerTriggered = false;

    @Subscribe("screenIdFacet")
    public void onScreenAfterShow(ScreenFacet.AfterShowEvent event) {
        afterShowListenerTriggered = true;
    }

    @Subscribe("screenIdFacet")
    public void onScreenAfterClose(ScreenFacet.AfterCloseEvent event) {
        afterCloseListenerTriggered = true;
    }
}
