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

package io.jmix.search.searching.impl;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.security.CurrentUserSecurityFacade;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component("search_SearchSecurityDecorator")
public class SearchSecurityDecorator {

    protected final Metadata metadata;
    protected final CurrentUserSecurityFacade securityFacade;

    public SearchSecurityDecorator(Metadata metadata,
                                   CurrentUserSecurityFacade securityFacade) {
        this.metadata = metadata;
        this.securityFacade = securityFacade;
    }

    public List<String> resolveEntitiesAllowedToSearch(Collection<String> requestedEntities) {
        return requestedEntities.stream()
                .filter(entity -> {
                    MetaClass metaClass = metadata.getClass(entity);
                    return securityFacade.canEntityBeRead(metaClass);
                })
                .collect(Collectors.toList());
    }

    public boolean canAttributeBeRead(MetaPropertyPath metaPropertyPath) {
        return securityFacade.canAttributeBeRead(metaPropertyPath);
    }
}
