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

package io.jmix.rest.security.impl;

import io.jmix.core.security.AuthorizedUrlsProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component("rest_DefaultRestAuthorizedUrlsProvider")
public class DefaultRestAuthorizedUrlsProvider implements AuthorizedUrlsProvider {
    @Override
    public Collection<String> getAuthenticatedUrlPatterns() {
        return Collections.singletonList("/rest/**");
    }

    @Override
    public Collection<String> getAnonymousUrlPatterns() {
        return Collections.singletonList("/rest/public/**");
    }
}
