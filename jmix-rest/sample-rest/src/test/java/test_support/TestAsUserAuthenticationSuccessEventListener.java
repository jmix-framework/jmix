/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.authserver.event.AsUserAuthenticationSuccessEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Records {@link AsUserAuthenticationSuccessEvent}s and runs an optional test-defined action on each of them.
 */
public class TestAsUserAuthenticationSuccessEventListener {

    private final List<AsUserAuthenticationSuccessEvent> events = new CopyOnWriteArrayList<>();

    private volatile Consumer<AsUserAuthenticationSuccessEvent> action = event -> {
    };

    @EventListener
    public void onAuthenticationSuccess(AsUserAuthenticationSuccessEvent event) {
        events.add(event);
        action.accept(event);
    }

    public List<AsUserAuthenticationSuccessEvent> getEvents() {
        return events;
    }

    public void setAction(Consumer<AsUserAuthenticationSuccessEvent> action) {
        this.action = action;
    }

    public void reset() {
        events.clear();
        action = event -> {
        };
    }
}
