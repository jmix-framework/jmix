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

package io.jmix.ui.event;

import io.jmix.ui.AppUI;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is fired on each web page refresh.
 * The event can be handled only in screen controllers of the active AppUI.
 */
public class UIRefreshEvent extends ApplicationEvent {
    public UIRefreshEvent(AppUI source) {
        super(source);
    }

    @Override
    public AppUI getSource() {
        return (AppUI) super.getSource();
    }
}