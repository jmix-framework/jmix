/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.app.navigation.notfoundwindow;

import io.jmix.core.Messages;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.Window;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.theme.ThemeClassNames;

import org.springframework.beans.factory.annotation.Autowired;

@Route("not-found")
@UiController(NotFoundScreen.ID)
public class NotFoundScreen extends Screen {

    public static final String ID = "notFoundScreen";

    @WindowParam(name = "requestedRoute", required = true)
    protected String requestedRoute;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Messages messages;

    @Subscribe
    protected void onInit(InitEvent event) {
        Window window = getWindow();

        Label<String> msgLabel = uiComponents.create(Label.TYPE_STRING);
        msgLabel.setAlignment(Component.Alignment.TOP_CENTER);
        msgLabel.addStyleName(ThemeClassNames.LABEL_H1);
        msgLabel.setValue(messages.formatMessage("","notAssociatedRoute", requestedRoute));

        window.add(msgLabel);

        window.setCaption(messages.formatMessage("","tabCaption", requestedRoute));
    }
}
