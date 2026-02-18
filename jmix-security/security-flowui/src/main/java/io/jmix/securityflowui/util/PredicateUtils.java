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

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class PredicateUtils {

    /**
     * Combines multiple {@link RoleAssignmentCandidatePredicate} into single {@link RoleAssignmentCandidatePredicate}.
     *
     * @param predicates predicates to combine
     * @return composite predicate
     */
    public static RoleAssignmentCandidatePredicate combineRoleAssignmentPredicates(List<RoleAssignmentCandidatePredicate> predicates) {
        return combineBiPredicates(predicates, RoleAssignmentCandidatePredicate::of);
    }

    /**
     * Combines multiple {@link RoleHierarchyCandidatePredicate} into single {@link RoleHierarchyCandidatePredicate}.
     *
     * @param predicates predicates to combine
     * @return composite predicate
     */
    public static RoleHierarchyCandidatePredicate combineRoleHierarchyPredicates(List<RoleHierarchyCandidatePredicate> predicates) {
        return combineBiPredicates(predicates, RoleHierarchyCandidatePredicate::of);
    }

    /**
     * Combines multiple {@link UserSubstitutionCandidatePredicate} into single {@link UserSubstitutionCandidatePredicate}.
     *
     * @param predicates predicates to combine
     * @return composite predicate
     */
    public static UserSubstitutionCandidatePredicate combineUserSubstitutionPredicates(List<UserSubstitutionCandidatePredicate> predicates) {
        return combineBiPredicates(predicates, UserSubstitutionCandidatePredicate::of);
    }

    /**
     * Combines multiple {@code BiPredicate<T, U>} into single {@code BiPredicate<T, U>}.
     *
     * @param predicates predicates to combine
     * @param <T>        type of the first argument of the predicate
     * @param <U>        type of the second argument of the predicate
     * @return composite predicate
     */
    public static <T, U> BiPredicate<T, U> combineBiPredicates(List<BiPredicate<T, U>> predicates) {
        return combineBiPredicates(predicates, Function.identity());
    }

    /**
     * Combines multiple {@code BiPredicate<T, U>} into single {@code BiPredicate<T, U>}
     * and wrap the result into specific predicate subtype.
     *
     * @param predicates predicates to combine
     * @param wrapper    wrapper function
     * @param <T>        type of the first argument of the predicate
     * @param <U>        type of the second argument of the predicate
     * @param <P>        type of the composite predicate
     * @return composite predicate
     */
    public static <T, U, P extends BiPredicate<T, U>> P combineBiPredicates(List<P> predicates, Function<BiPredicate<T, U>, P> wrapper) {
        BiPredicate<T, U> combined = (t, u) -> {
            /*
                Using loop instead of 'and()' to work with custom type of predicates
                and to mitigate possible stack overflow due to undetermined amount of predicates (low probability)
             */
            if (predicates != null) {
                for (P p : predicates) {
                    if (!p.test(t, u)) {
                        return false;
                    }
                }
            }
            return true;
        };

        return wrapper.apply(combined);
    }
}
