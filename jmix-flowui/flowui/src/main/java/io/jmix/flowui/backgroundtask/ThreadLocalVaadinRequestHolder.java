/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.backgroundtask;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinRequest;
import io.jmix.core.annotation.Internal;
import org.jspecify.annotations.Nullable;

/**
 * Provides thread-local storage for Vaadin request. This utility class facilitates the transfer of Vaadin
 * request to newly spawned threads that do not inherently have access to the data of the original Vaadin request.
 * <p>
 * This class mainly used to provide access to Vaadin request within {@link UI#access(Command)} closure in background
 * tasks. It is not recommended to use this class in the application code.
 * <p>
 * Ensure that you clear the thread-local storage after use. Use {@link #clear()} method once the thread-local data is
 * no longer needed.
 */
@Internal
public class ThreadLocalVaadinRequestHolder {

    private static final ThreadLocal<VaadinRequest> requestHolder = new ThreadLocal<>();

    public static void setRequest(@Nullable VaadinRequest request) {
        requestHolder.set(request);
    }

    @Nullable
    public static VaadinRequest getRequest() {
        return requestHolder.get();
    }

    public static void clear() {
        requestHolder.remove();
    }
}
