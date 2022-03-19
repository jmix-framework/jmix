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

package io.jmix.ui.event.screen;

import io.jmix.ui.Screens;
import org.springframework.context.ApplicationEvent;

/**
 * Application event which is fired when the framework closes all windows. For instance, when the user logs out.
 */
public class CloseWindowsInternalEvent extends ApplicationEvent {

    public CloseWindowsInternalEvent(Screens screens) {
        super(screens);
    }

    @Override
    public Screens getSource() {
        return (Screens) super.getSource();
    }
}
