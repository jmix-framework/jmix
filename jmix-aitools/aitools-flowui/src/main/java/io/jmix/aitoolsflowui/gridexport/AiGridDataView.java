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

package io.jmix.aitoolsflowui.gridexport;

import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.NullMarked;

/**
 * A view that lets the user ask the AI about the rows of a data grid. The view opened by "Ask AI" in the grid export
 * dialog gets the grid through this interface; a view that replaces the standard one by its id implements it to get
 * the grid too, and can open the chat with {@link AiGridDataSupport#openChat}.
 */
@Experimental
@NullMarked
public interface AiGridDataView {

    /**
     * Passes the grid whose rows the user wants to ask about. Called before the view is shown.
     *
     * @param context the grid and the way its rows are read
     */
    void setGridDataContext(AiGridDataContext context);
}
