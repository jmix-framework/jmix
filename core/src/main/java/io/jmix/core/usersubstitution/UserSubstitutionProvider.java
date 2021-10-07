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

package io.jmix.core.usersubstitution;

import java.util.Collection;
import java.util.Date;

/**
 * Interface to be implemented by classes that provide {@link UserSubstitution}. Each provider is responsible for
 * extracting an information from a specific source (in-memory, database, etc.)
 */
public interface UserSubstitutionProvider {

    /**
     * Method returns a collection of {@link UserSubstitution} available for the given {@code username} and active at
     * the given {@code date}. If {@link UserSubstitution#startDate} and {@link UserSubstitution#endDate} are
     * null then this substitution is considered active.
     */
    Collection<UserSubstitution> getUserSubstitutions(String username, Date date);
}
