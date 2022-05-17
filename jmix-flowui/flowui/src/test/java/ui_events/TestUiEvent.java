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

package ui_events;

import org.springframework.context.ApplicationEvent;

public class TestUiEvent extends ApplicationEvent {

    protected String message;

    public TestUiEvent(Object source, String message) {
        super(source);

        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
