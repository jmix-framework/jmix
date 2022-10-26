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

package io.jmix.flowui.facet;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.HasSubParts;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

public interface QueryParametersFacet extends Facet, HasSubParts {

    String NAME = "queryParameters";

    /**
     * Register a new query parameters binder.
     *
     * @param binder a binder instance to register
     */
    void registerBinder(Binder binder);

    /**
     * @return a list of registered query parameter binders
     */
    List<Binder> getBinders();

    /**
     * An interface to be implemented by an object that binds UI component with query parameters.
     */
    interface Binder {

        /**
         * @return the query parameters binder id
         */
        @Nullable
        String getId();

        /**
         * Sets the query parameters binder id.
         *
         * @param id id to set
         */
        void setId(@Nullable String id);

        /**
         * Informs query parameters binder that view query parameters has been changed.
         *
         * @param queryParameters a view query parameters
         */
        void updateState(QueryParameters queryParameters);

        /**
         * Adds {@link QueryParametersChangeEvent} listener.
         *
         * @param listener the listener to add, not {@code null}
         * @return a registration object that can be used for removing the listener.
         */
        Registration addQueryParametersChangeListener(Consumer<QueryParametersChangeEvent> listener);
    }

    /**
     * An event which is fired when a query parameters binder informs that
     * its internal state has been changed and it should be reflected on
     * URL's query parameters.
     */
    class QueryParametersChangeEvent extends EventObject {

        protected QueryParameters queryParameters;

        public QueryParametersChangeEvent(Binder source, QueryParameters queryParameters) {
            super(source);
            this.queryParameters = queryParameters;
        }

        @Override
        public Binder getSource() {
            return (Binder) super.getSource();
        }

        /**
         * @return query parameters to set to URL
         */
        public QueryParameters getQueryParameters() {
            return queryParameters;
        }
    }
}
