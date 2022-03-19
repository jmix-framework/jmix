/*
 * Copyright 2021 Haulmont.
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

package io.jmix.security.model;

import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Represents row level policy in-memory predicate. It expects two arguments: entity instance and {@link
 * ApplicationContext}. Use {@code ApplicationContext} when you need to get any Spring bean instance if you need to use
 * the bean inside the function.
 */
@FunctionalInterface
public interface RowLevelBiPredicate<T, U extends ApplicationContext> extends BiPredicate<T, U>, Serializable {
}
