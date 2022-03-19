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

package io.jmix.core.impl.importexport;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CollectionCompare {
    protected Consumer<Object> createConsumer;
    protected Consumer<Object> deleteConsumer;
    protected BiConsumer<Object, Object> updateConsumer;

    private CollectionCompare() {
    }

    public static CollectionCompare with() {
        return new CollectionCompare();
    }

    public CollectionCompare onCreate(Consumer<Object> createConsumer) {
        this.createConsumer = createConsumer;
        return this;
    }

    public CollectionCompare onDelete(Consumer<Object> deleteConsumer) {
        this.deleteConsumer = deleteConsumer;
        return this;
    }

    public CollectionCompare onUpdate(BiConsumer<Object, Object> updateConsumer) {
        this.updateConsumer = updateConsumer;
        return this;
    }

    public void compare(Collection<Object> src, Collection<Object> dst) {
        final Collection<Object> srcNN = Optional.ofNullable(src)
                .orElse(Collections.emptyList());
        final Collection<Object> dstNN = Optional.ofNullable(dst)
                .orElse(Collections.emptyList());
        for (Object srcEntity : srcNN) {
            Optional<Object> existingOptional = dstNN.stream()
                    .filter(e -> Objects.equals(e, srcEntity))
                    .findFirst();
            if (existingOptional.isPresent()) {
                updateConsumer.accept(srcEntity, existingOptional.get());
            } else {
                createConsumer.accept(srcEntity);
            }
        }
        dstNN.stream().filter(item -> !srcNN.contains(item)).forEach(deleteConsumer);
    }
}
