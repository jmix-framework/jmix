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

package io.jmix.rest.security;

import io.jmix.core.JmixModules;
import io.jmix.core.security.AuthorizedUrlsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component("rest_RestAuthorizedUrlsProvider")
public class RestAuthorizedUrlsProvider implements AuthorizedUrlsProvider {
    @Autowired
    private JmixModules jmixModules;

    @Override
    @NonNull
    public Collection<String> getAuthenticatedUrlPatterns() {
        List<String> urlPatterns = jmixModules.getPropertyValues("jmix.rest.authenticatedUrlPatterns");
        return urlPatterns.stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getAnonymousUrlPatterns() {
        List<String> urlPatterns = jmixModules.getPropertyValues("jmix.rest.anonymousUrlPatterns");
        return urlPatterns.stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .collect(Collectors.toList());
    }
}
