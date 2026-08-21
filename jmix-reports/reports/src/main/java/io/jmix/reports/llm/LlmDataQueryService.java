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
 * Turns a data set prompt into a JPQL query and executes it. This is the Reports-side integration seam: Reports
 * auto-configuration supplies the default AI Tools-backed implementation, while applications may substitute one.
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
     * Generates a query for the given prompt.
     *
     * @param request prompt together with the parameters the query may reference
     * @return the generated query
     * @throws LlmDataQueryException if the query cannot be generated or is rejected as invalid
     */
    LlmDataQuery generate(LlmQueryGenerationRequest request);

    /**
     * Reports what makes a query unrunnable, without running it and without asking a model anything. The
     * designer asks after a query is generated or edited, so that an author learns of a broken query while
     * looking at it rather than on the next report run.
     *
     * @param query query to check
     * @return one message per problem, in no particular order, or an empty list if the query is runnable
     */
    List<String> validate(LlmDataQuery query);

    /**
     * Executes a query already found runnable and returns its rows. The caller checks the query with {@link #validate(LlmDataQuery)} first — a report run does it
     * once and then executes the query for every row of the band's parent — so an implementation is not
     * expected to check it again.
     * <p>
     * Data access constraints of the current user apply, so the rows may be narrower or shorter than the
     * query alone would suggest.
     *
     * @param request query and the arguments to bind
     * @return the rows and whether more of them were left behind
     * @throws LlmDataQueryException if the query is rejected as invalid or its execution fails
     */
    LlmQueryExecutionResult execute(LlmQueryExecutionRequest request);
}
