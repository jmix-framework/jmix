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

package io.jmix.core;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Interface that encapsulates application-scope event publication functionality.
 * Simple facade for {@link ApplicationEventPublisher}.
 */
public interface Events {
    String NAME = "core_Events";

    /**
     * Notify all <strong>matching</strong> listeners registered with this application of an application event.
     * Events may be framework event (such as RequestHandledEvent) or application-specific event.
     * <p>
     * You can use {@link org.springframework.context.PayloadApplicationEvent} to publish any object as an event.
     *
     * @param event the event to publish
     */
    void publish(ApplicationEvent event);
}