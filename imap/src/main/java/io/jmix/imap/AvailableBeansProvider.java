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

package io.jmix.imap;

import io.jmix.imap.events.BaseImapEvent;

import java.util.List;
import java.util.Map;

public interface AvailableBeansProvider {

    /**
     * Return information about beans and their methods that can be attached to IMAP folder event.
     * @return  map of bean names to lists of their methods
     */
    Map<String, List<String>> getEventHandlers(Class<? extends BaseImapEvent> eventClass);

    /**
     * Return bean names and class names of custom IMAP Event Generators
     * @return  map of bean name to class name
     */
    Map<String, String> getEventsGenerators();

}
