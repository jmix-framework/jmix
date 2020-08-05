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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.component.TablePagination;

public class TablePaginationLoader extends PaginationLoader {

    @Override
    public void createComponent() {
        resultComponent = factory.create(TablePagination.NAME);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadShowMaxResults(element)
                .ifPresent(resultComponent::setShowMaxResults);
        loadShowNullMaxResult(element)
                .ifPresent(resultComponent::setShowNullMaxResult);
        loadMaxResults(element)
                .ifPresent(resultComponent::setMaxResultOptions);

        loadAutoLoad(element)
                .ifPresent(resultComponent::setAutoLoad);
    }
}
