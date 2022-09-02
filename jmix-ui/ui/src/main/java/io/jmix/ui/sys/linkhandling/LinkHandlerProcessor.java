/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.sys.linkhandling;

import io.jmix.ui.navigation.NavigationHandler;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.sys.LinkHandler;

/**
 * Interface that is used by {@link LinkHandler}
 * to handle links from outside of the application.
 * <br> {@link LinkHandler} traverses processors to find first able to handle link.
 * <br> To set processor priority use {@link org.springframework.core.annotation.Order @Order},
 * {@link org.springframework.core.Ordered} or {@link javax.annotation.Priority @Priority}.
 *
 * @deprecated will be removed in the next minor release. Use URL history and navigation API, e.g. {@link Route},
 * {@link UrlRouting}, {@link NavigationHandler} instead
 */
@Deprecated
public interface LinkHandlerProcessor {

    /**
     * @return true if action with such request parameters should be handled by this processor.
     */
    boolean canHandle(ExternalLinkContext linkContext);

    /**
     * Called to handle action.
     */
    void handle(ExternalLinkContext linkContext);
}
