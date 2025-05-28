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

package io.jmix.tabbedmode.event;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Location;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.RouteSupport;
import org.springframework.context.ApplicationEvent;

/**
 * The event that is fired each time the location changes by {@link RouteSupport} bean.
 * <p>
 * The event can be handled only in {@link View} controllers of the active {@link UI}.
 */
public class LocationChangeEvent extends ApplicationEvent {

    protected final Location location;

    public LocationChangeEvent(UI source, Location location) {
        super(source);
        this.location = location;
    }

    @Override
    public UI getSource() {
        return (UI) super.getSource();
    }

    /**
     * Returns the location associated with the event.
     *
     * @return the location that triggered the event
     */
    public Location getLocation() {
        return location;
    }
}
