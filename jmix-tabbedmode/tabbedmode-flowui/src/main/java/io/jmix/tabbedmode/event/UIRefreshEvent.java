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

package io.jmix.tabbedmode.event;

import com.vaadin.flow.component.UI;
import io.jmix.flowui.view.View;
import org.springframework.context.ApplicationEvent;

/**
 * The event that is fired each time a web page is refreshed, if the UI state
 * has been restored from the cached views.
 * <p>
 * The event can be handled only in {@link View} controllers of the active {@link UI}.
 */
public class UIRefreshEvent extends ApplicationEvent {

    public UIRefreshEvent(UI source) {
        super(source);
    }

    @Override
    public UI getSource() {
        return (UI) super.getSource();
    }
}