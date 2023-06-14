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

package navigation.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "BackwardNavigationStandardView")
@ViewController("BackwardNavigationStandardView")
@ViewDescriptor("backward-navigation-custom-view.xml")
public class BackwardNavigationStandardView extends StandardView {
    @Autowired
    private ViewNavigators viewNavigators;

    @ViewComponent
    public JmixButton navigateToViewBtn;

    @Subscribe("navigateToViewBtn")
    protected void onNavigateToViewBtnClick(ClickEvent<Button> event) {
        viewNavigators.view(BackwardNavigationListView.class)
                .withBackwardNavigation(true)
                .navigate();
    }
}
