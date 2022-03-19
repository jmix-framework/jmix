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

package io.jmix.ui.component.data.options;

import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.event.sys.VoidSubscription;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.Options;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Options based on a map.
 *
 * @param <I> item type
 */
public class MapOptions<I> implements Options<I> {
    protected Map<String, I> options;

    public MapOptions(Map<String, I> options) {
        checkNotNullArgument(options);

        this.options = options;
    }

    public Map<String, I> getItemsCollection() {
        return options;
    }

    @Override
    public Stream<I> getOptions() {
        return options.values().stream();
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Subscription addStateChangeListener(Consumer listener) {
        return VoidSubscription.INSTANCE;
    }

    @Override
    public Subscription addOptionsChangeListener(Consumer listener) {
        return VoidSubscription.INSTANCE;
    }
}