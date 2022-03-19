/*
 * Copyright 2020 Haulmont.
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

package fragment.screen;

import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

@UiController
@UiDescriptor("fragment-xml-listener-test-screen.xml")
public class FragmentXmlListenerTestScreen extends Screen {
    @Autowired
    protected EventListenerTestFragment fragmentWithEvent;

    public Consumer<EventListenerTestFragment.HelloEvent> handler;

    @Subscribe(id = "fragmentWithEvent", target = Target.CONTROLLER)
    protected void onHello(EventListenerTestFragment.HelloEvent event) {
        handler.accept(event);
    }
}
