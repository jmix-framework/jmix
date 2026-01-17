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

package io.jmix.securityflowui.util;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.BiPredicate;

/**
 * Interface to provide predicate to test the possibility to use some user as substitution based on target user.
 * Accepts the user to be substituted and another user as substitution candidate.
 */
public interface UserSubstitutionCandidatePredicate extends BiPredicate<UserDetails, UserDetails> {

    /**
     * Wraps the generic {@code BiPredicate<UserDetails, UserDetails>} in {@link UserSubstitutionCandidatePredicate} type.
     */
    static UserSubstitutionCandidatePredicate of(BiPredicate<UserDetails, UserDetails> p) {
        return p::test;
    }
}