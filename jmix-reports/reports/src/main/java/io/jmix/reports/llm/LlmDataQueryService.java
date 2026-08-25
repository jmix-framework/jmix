/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reports.llm;

import java.util.List;

/**
 * Turns a data set prompt into a JPQL query and says whether that query would run. This is the Reports-side
 * integration seam, used while a report is authored in the designer; a report run executes the stored query
 * itself and asks nothing of this service. Reports auto-configuration supplies the default AI Tools-backed
 * implementation, while applications may substitute one.
 */
public interface LlmDataQueryService {

    /**
     * Tells whether a query can be generated at all, which the presence of this bean alone does not say: an
     * implementation may be there while the model it talks to is not configured. The report designer asks
     * before it offers the data set type, so that a type it offers is a type that works.
     * <p>
     * Every implementation answers it explicitly; one that is always ready returns {@code true}.
     *
     * @return {@code true} if generation can be performed
     */
    boolean isGenerationAvailable();

    /**
     * Generates a query for the given prompt. An implementation may correct a query it finds faulty before
     * answering with it — a generated query is the model's, and no one is waiting to be told about a mistake
     * only the model made.
     *
     * @param request prompt together with the parameters the query may reference
     * @return the generated query
     * @throws LlmDataQueryException if the query cannot be generated or is rejected as invalid
     */
    LlmDataQuery generate(LlmQueryGenerationRequest request);

    /**
     * Reports what makes a query unrunnable, without running it, without asking a model anything and without
     * correcting anything: a query checked here belongs to whoever wrote it. The designer asks after a query is
     * generated or edited, so that an author learns of a broken query while looking at it rather than on the
     * next report run.
     *
     * @param query query to check
     * @return one message per problem, in no particular order, or an empty list if the query is runnable
     */
    List<String> validate(LlmDataQuery query);
}
