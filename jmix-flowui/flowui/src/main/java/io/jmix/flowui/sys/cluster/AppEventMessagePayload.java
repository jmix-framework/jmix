/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.sys.cluster;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;

public class AppEventMessagePayload implements Serializable {

    protected ApplicationEvent event;
    protected Collection<String> usernames;

    public AppEventMessagePayload(ApplicationEvent event, @Nullable Collection<String> usernames) {
        this.event = event;
        this.usernames = usernames;
    }

    @Nullable
    public Collection<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(@Nullable Collection<String> usernames) {
        this.usernames = usernames;
    }

    public ApplicationEvent getEvent() {
        return event;
    }

    public void setEvent(ApplicationEvent event) {
        this.event = event;
    }
}
