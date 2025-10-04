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

package io.jmix.search.searching;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public interface SearchQueryConfigurator<SRB, QB, OB> {

    void configureRequest(RequestContext<SRB> requestContext,
                          TargetQueryBuilder<QB, OB> targetQueryBuilder);

    void configureRequest(RequestContext<SRB> requestContext,
                          Function<String, Set<String>> subfieldsGenerator,
                          TargetQueryBuilder<QB, OB> targetQueryBuilder);

    interface TargetQueryBuilder<QB, OB> {
        OB apply(QB queryBuilder, List<String> fields);
    }
}
