/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.devserver;

import java.util.Optional;

import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.internal.ErrorTargetEntry;
import com.vaadin.flow.server.RouteRegistry;

public class JmixRouter extends Router {

    public JmixRouter(RouteRegistry registry) {
        super(registry);
    }

    @Override
    public Optional<ErrorTargetEntry> getErrorNavigationTarget(Exception exception) {
        if (NotFoundException.class.isAssignableFrom(exception.getClass())) {
            return Optional.of(new ErrorTargetEntry(RouteNotFoundError.class, exception.getClass()));
        } else {
            return super.getErrorNavigationTarget(exception);
        }
    }
}
