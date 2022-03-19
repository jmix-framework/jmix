/*
 * Copyright (c) 2008-2018 Haulmont.
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

package io.jmix.ui.bulk;


import io.jmix.core.FetchPlan;
import io.jmix.core.metamodel.model.MetaClass;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface BulkEditorDataService {

    /**
     * Reloads selected items with the passed view.
     *
     * @param loadDescriptor load descriptor
     * @return reloaded instances
     */
    <E> List<E> reload(LoadDescriptor<E> loadDescriptor);

    class LoadDescriptor<E> implements Serializable {

        protected final Collection<E> selectedItems;
        protected final MetaClass metaClass;
        protected final FetchPlan fetchPlan;

        public LoadDescriptor(Collection<E> selectedItems, MetaClass metaClass, FetchPlan fetchPlan) {
            this.selectedItems = selectedItems;
            this.metaClass = metaClass;
            this.fetchPlan = fetchPlan;
        }

        public Collection<E> getSelectedItems() {
            return selectedItems;
        }

        public MetaClass getMetaClass() {
            return metaClass;
        }

        public FetchPlan getFetchPlan() {
            return fetchPlan;
        }
    }
}