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

package io.jmix.flowui.facet.queryparameters;

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.facet.QueryParametersFacet;
import io.jmix.flowui.facet.QueryParametersFacet.QueryParametersChangeEvent;
import io.jmix.flowui.kit.event.EventBus;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class AbstractQueryParametersBinder implements QueryParametersFacet.Binder {

    protected String id;

    private EventBus eventBus;

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Override
    public Registration addQueryParametersChangeListener(Consumer<QueryParametersChangeEvent> listener) {
        return getEventBus().addListener(QueryParametersChangeEvent.class, listener);
    }

    protected void fireQueryParametersChanged(QueryParametersChangeEvent event) {
        getEventBus().fireEvent(event);
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }
}
