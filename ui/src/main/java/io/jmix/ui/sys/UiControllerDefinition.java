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

package io.jmix.ui.sys;

import io.jmix.ui.navigation.RouteDefinition;
import org.springframework.core.io.Resource;

import javax.annotation.Nullable;

public final class UiControllerDefinition {

    private final String id;
    private final String controllerClass;
    private final Resource resource;
    private final RouteDefinition routeDefinition;

    public UiControllerDefinition(String id, String controllerClass, Resource resource, @Nullable RouteDefinition routeDefinition) {
        this.id = id;
        this.controllerClass = controllerClass;
        this.resource = resource;
        this.routeDefinition = routeDefinition;
    }

    public UiControllerDefinition(String id, String controllerClass, @Nullable RouteDefinition routeDefinition) {
        this.id = id;
        this.controllerClass = controllerClass;
        this.routeDefinition = routeDefinition;
        this.resource = null;
    }

    public String getId() {
        return id;
    }

    public String getControllerClass() {
        return controllerClass;
    }

    @Nullable
    public RouteDefinition getRouteDefinition() {
        return routeDefinition;
    }

    @Nullable
    public Resource getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return "UiControllerDefinition{" +
                "id='" + id + '\'' +
                ", controllerClass='" + controllerClass + '\'' +

                (routeDefinition == null
                        ? ""
                        : ", " + routeDefinition.toString()) +
                '}';
    }
}
