/*
 * Copyright 2021 Haulmont.
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

package component.composite.component;

import component.composite.appevent.TestAppEvent;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.CompositeComponent;
import io.jmix.ui.component.CssLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

public class TestEventPanel extends CompositeComponent<CssLayout> {

    public static final String NAME = "testEventPanel";

    @Autowired
    protected UiComponents uiComponents;

    protected int eventCounter;

    public TestEventPanel() {
        addCreateListener(this::onCreate);
    }

    protected void onCreate(CreateEvent event) {
        root = uiComponents.create(CssLayout.NAME);
    }

    public int getEventCounter() {
        return eventCounter;
    }

    @EventListener
    private void onTestAppEvent(TestAppEvent event) {
        eventCounter++;
    }
}
