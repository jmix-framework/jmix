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

package io.jmix.tabbedmode.builder;

import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.LookupWindowBuilder;

import java.util.function.Consumer;
import java.util.function.Function;

public class LookupViewBuilderAdapter<E, V extends View<?>> extends LookupViewBuilder<E, V> {

    public LookupViewBuilderAdapter(LookupWindowBuilder<E, V> windowBuilder,
                                    Class<V> viewClass,
                                    Function<LookupViewBuilder<E, V>, V> buildHandler,
                                    Consumer<ViewOpeningContext> openHandler) {
        super(windowBuilder.getOrigin(), windowBuilder.getEntityClass(), viewClass, buildHandler, openHandler);

        applyFrom(windowBuilder);
    }

    protected void applyFrom(LookupWindowBuilder<E, V> windowBuilder) {
        ViewBuilderAdapterUtils.apply(this, windowBuilder);

        container = windowBuilder.getContainer().orElse(null);
        listDataComponent = windowBuilder.getListDataComponent().orElse(null);

        field = windowBuilder.getField().orElse(null);
        lookupComponentMultiSelect = windowBuilder.isLookupComponentMultiSelect();
        fieldCollectionValue = windowBuilder.isFieldCollectionValue();

        transformation = windowBuilder.getTransformation().orElse(null);

        selectHandler = windowBuilder.getSelectHandler().orElse(null);
        selectValidator = windowBuilder.getSelectValidator().orElse(null);
    }
}
