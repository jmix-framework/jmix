/*
 * Copyright 2024 Haulmont.
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

package ui_events.fragment;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.fragment.Fragment;
import org.springframework.context.event.EventListener;
import ui_events.TestUiEvent;

public class UiEventTestFragment extends Fragment<VerticalLayout> {

    public String eventMessage = "noop";

    @EventListener
    public void testUiEventHandler(TestUiEvent event) {
        eventMessage = event.getMessage();
    }
}
