/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.flowui.devserver.frontend;

import com.vaadin.flow.server.frontend.FallibleCommand;

/**
 * Exception thrown for when a node task that is not in the task list is
 * encountered.
 */
public class UnknownTaskException extends RuntimeException {

    /**
     * Exception constructor.
     *
     * @param command
     *            command that was not found
     */
    public UnknownTaskException(FallibleCommand command) {
        super("Could not find position for task " + command.getClass());
    }
}
