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

package io.jmix.flowui.model.impl;

import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.model.CollectionContainer;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

/**
 * A weak wrapper for a {@link Consumer} that listens to {@link CollectionContainer.CollectionChangeEvent}s.
 * This class ensures that the listener does not prevent garbage collection of the original
 * {@link Consumer}, thus avoiding memory leaks.
 *
 * @param <E> the type of elements in the collection container
 */
public class WeakCollectionChangeListener<E>
        implements Consumer<CollectionContainer.CollectionChangeEvent<E>> {

    private final WeakReference<Consumer<CollectionContainer.CollectionChangeEvent<E>>> reference;
    private final Subscription subscription;

    @SuppressWarnings("unchecked")
    public WeakCollectionChangeListener(CollectionContainer container,
                                        Consumer<CollectionContainer.CollectionChangeEvent<E>> collectionChangeListener) {
        reference = new WeakReference(collectionChangeListener);
        subscription = container.addCollectionChangeListener(this);
    }

    @Override
    public void accept(CollectionContainer.CollectionChangeEvent<E> e) {
        Consumer<CollectionContainer.CollectionChangeEvent<E>> collectionChangeListener = reference.get();
        if (collectionChangeListener != null) {
            collectionChangeListener.accept(e);
        } else {
            subscription.remove();
        }
    }

    /**
     * Removes the associated listener from the event source, ensuring that this instance no longer
     * listens to changes in the underlying collection or data source.
     */
    public void removeItself() {
        subscription.remove();
    }
}
