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

package autowire.fragment;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;

import java.util.ArrayList;
import java.util.List;

public class LifecycleEventsDependencyInjectorFragment extends Fragment<HorizontalLayout> {

    protected List<String> executedEvents = new ArrayList<>();

    @Subscribe
    protected void onReady(ReadyEvent event) {
        executedEvents.add("Fragment." + event.getClass().getSimpleName());
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostInit(View.InitEvent event) {
        executedEvents.add("Fragment.Host." + event.getClass().getSimpleName());
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostBeforeShow(View.BeforeShowEvent event) {
        executedEvents.add("Fragment.Host." + event.getClass().getSimpleName());
    }

    @Subscribe(target = Target.HOST_CONTROLLER)
    protected void onHostReady(View.ReadyEvent event) {
        executedEvents.add("Fragment.Host." + event.getClass().getSimpleName());
    }

    public List<String> getExecutedEvents() {
        return executedEvents;
    }
}
