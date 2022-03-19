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

package facet.screen.screen;

import facet.screen.screen.product_tag.ProductTagBrowse;
import facet.screen.screen.product_tag.ProductTagEdit;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.EditorScreenFacet;
import io.jmix.ui.component.LookupScreenFacet;
import io.jmix.ui.component.ScreenFacet;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.ProductTag;

@UiController
@UiDescriptor("screen-facet-test-screen.xml")
public class ScreenFacetTestScreen extends Screen {

    @Autowired
    public Button button;
    @Autowired
    public Action action;

    @Autowired
    public ScreenFacet<FacetTestScreen> screenIdFacet;
    @Autowired
    public ScreenFacet<FacetTestScreen> screenClassFacet;

    @Autowired
    public LookupScreenFacet<ProductTag, ProductTagBrowse> screenIdLookupScreen;
    @Autowired
    public LookupScreenFacet<ProductTag, ProductTagBrowse> screenClassLookupScreen;

    @Autowired
    public EditorScreenFacet<ProductTag, ProductTagEdit> screenIdEditScreen;
    @Autowired
    public EditorScreenFacet<ProductTag, ProductTagEdit> screenClassEditScreen;

    public boolean afterShowListenerTriggered = false;
    public boolean afterCloseListenerTriggered = false;

    @Subscribe("screenIdFacet")
    public void onScreenAfterShow(Screen.AfterShowEvent event) {
        afterShowListenerTriggered = true;
    }

    @Subscribe("screenIdFacet")
    public void onScreenAfterClose(Screen.AfterCloseEvent event) {
        afterCloseListenerTriggered = true;
    }
}
