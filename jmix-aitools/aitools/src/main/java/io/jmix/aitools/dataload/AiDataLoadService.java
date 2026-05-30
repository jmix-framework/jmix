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

package io.jmix.aitools.dataload;


import reactor.core.publisher.Flux;

/**
 * Entry point of the LLM-powered natural-language data-load flow. Supports two modes:
 * <ul>
 *     <li>
 *         conversational - {@link #send} / {@link #stream} let the LLM answer free-form
 *         questions about the application's data and return a natural-language reply;
 *     </li>
 *     <li>
 *         data loading - {@link #loadData} generates a JPQL query for the user's request,
 *         executes it and returns the structured {@link EntityDataLoadResult}.
 *     </li>
 * </ul>
 * Methods without {@code conversationId} run statelessly and do not persist chat history.
 */
public interface AiDataLoadService {

    /**
     * Sends a stateless message to the data-load chat and blocks until the full reply is produced.
     * Chat history is not persisted.
     *
     * @param message user message in natural language
     * @return natural-language reply from the LLM
     */
    String send(String message);

    /**
     * Streams a stateless reply to the data-load chat as a flux of text chunks.
     * Chat history is not persisted.
     *
     * @param message user message in natural language
     * @return flux emitting reply chunks in order
     */
    Flux<String> stream(String message);

    /**
     * Streams a reply to the data-load chat within the given conversation, persisting the
     * exchange in the chat memory under {@code conversationId}.
     *
     * @param message        user message in natural language
     * @param conversationId identifier of the conversation to append the exchange to
     * @return flux emitting reply chunks in order
     */
    Flux<String> stream(String message, String conversationId);

    /**
     * Sends a message to the data-load chat within the given conversation and blocks until the
     * full reply is produced. The exchange is persisted in the chat memory under
     * {@code conversationId}.
     *
     * @param message        user message in natural language
     * @param conversationId identifier of the conversation to append the exchange to
     * @return natural-language reply from the LLM
     */
    String send(String message, String conversationId);

    /**
     * Generates a JPQL query for the given natural-language request, executes it and returns
     * the structured result (query draft, validation, rows). Runs statelessly without
     * persisting chat history.
     *
     * @param userText user request in natural language
     * @return result containing the generated query, its validation and the fetched rows
     */
    EntityDataLoadResult loadData(String userText);

    /**
     * Generates a JPQL query for the given natural-language request within the given
     * conversation, executes it and returns the structured result. The exchange is persisted
     * in the chat memory under {@code conversationId} so that follow-up requests can refine
     * the query.
     *
     * @param userText       user request in natural language
     * @param conversationId identifier of the conversation to append the exchange to
     * @return result containing the generated query, its validation and the fetched rows
     */
    EntityDataLoadResult loadData(String userText, String conversationId);
}
