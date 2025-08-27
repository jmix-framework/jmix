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

public class PredicateUtils {

    @SuppressWarnings("unchecked")
    public static <T, U, P extends BiPredicate<T, U>> P combineBiPredicates(List<P> predicates) {
        final BiPredicate<T, U> composite = (P) (BiPredicate<T, U>) (t, u) -> {
            /*
                Using loop instead of 'and()' to work with custom type of predicates
                and to mitigate possible stack overflow due to undetermined amount of predicates (low probability)
             */
            for (BiPredicate<T, U> p : (List<BiPredicate<T, U>>) (List<?>) predicates) {
                if (!p.test(t, u)) {
                    return false;
                }
            }
            return true;
        };

        return (P) composite;
    }

    public static RoleAssignmentCandidatePredicate combineRoleAssignmentPredicates(List<RoleAssignmentCandidatePredicate> predicates) {
        return (user, role) -> {
            /*
                Using loop instead of 'and()' to work with custom type of predicates
                and to mitigate possible stack overflow due to undetermined amount of predicates (low probability)
             */
            for (RoleAssignmentCandidatePredicate p : predicates) {
                if (!p.test(user, role)) {
                    return false;
                }
            }
            return true;
        };
    }

    public static UserSubstitutionCandidatePredicate combineUserSubstitutionPredicates(List<UserSubstitutionCandidatePredicate> predicates) {
        return (user1, user2) -> {
            /*
                Using loop instead of 'and()' to work with custom type of predicates
                and to mitigate possible stack overflow due to undetermined amount of predicates (low probability)
            */
            for (UserSubstitutionCandidatePredicate p : predicates) {
                if (!p.test(user1, user2)) {
                    return false;
                }
            }
            return true;
        };
    }


}
