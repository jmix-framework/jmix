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

package view_overriding.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route("ViewOverridingTestView")
@ViewController("ViewOverridingTestView")
@ViewDescriptor("view-overriding-test-view.xml")
public class ViewOverridingTestView extends StandardView {
    @Autowired
    protected ViewNavigators viewNavigators;

    @ViewComponent
    public JmixButton navigateToViewByIdBtn;
    @ViewComponent
    public JmixButton navigateToViewByClassBtn;

    @Subscribe("navigateToViewByIdBtn")
    protected void onNavigateToViewByIdBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view("ViewOverridingOrigin")
                .navigate();
    }

    @Subscribe("navigateToViewByClassBtn")
    protected void onNavigateToViewByClassBtnClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(ViewOverridingOrigin.class)
                .navigate();
    }
}
