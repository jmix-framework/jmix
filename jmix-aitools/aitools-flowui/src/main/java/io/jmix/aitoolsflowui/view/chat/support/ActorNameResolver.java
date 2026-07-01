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

package io.jmix.aitoolsflowui.view.chat.support;

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.core.MetadataTools;
import io.jmix.core.security.CurrentAuthentication;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Resolves a human-readable actor name for a user-authored {@link AiChatMessage}.
 * <p>
 * For messages authored by the currently logged-in user, returns the user's
 * instance name (preferred) or, failing that, the username. For messages
 * authored by someone else (admin viewing another user's conversation),
 * returns the raw {@code createdBy} string. Falls back to a configurable
 * default if both sources are blank.
 * <p>
 * The add-on does not assume a particular {@code User} class, so unlike the
 * CRM equivalent this resolver relies on {@link MetadataTools#getInstanceName}
 * to render whichever User entity the consumer application uses.
 */
@Component("aitls_ActorNameResolver")
public class ActorNameResolver {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Resolves the display name of the actor that authored the given message.
     *
     * @param message          message whose author is resolved
     * @param defaultActorName name to fall back to when the author cannot be determined
     * @return the resolved actor name
     */
    public String resolve(@Nullable AiChatMessage message, String defaultActorName) {
        UserDetails currentUser = currentAuthentication.getUser();
        String createdBy = message != null ? message.getCreatedBy() : null;

        if (isCurrentUser(createdBy, currentUser)) {
            return currentUserDisplayName(currentUser, defaultActorName);
        }
        return Objects.requireNonNullElse(createdBy, defaultActorName);
    }

    private boolean isCurrentUser(@Nullable String createdBy, UserDetails currentUser) {
        return !StringUtils.hasText(createdBy)
                || Objects.equals(createdBy, currentUser.getUsername());
    }

    private String currentUserDisplayName(UserDetails currentUser, String defaultActorName) {
        String instanceName = metadataTools.getInstanceName(currentUser);
        if (StringUtils.hasText(instanceName)) {
            return instanceName;
        }
        if (StringUtils.hasText(currentUser.getUsername())) {
            return currentUser.getUsername();
        }
        return defaultActorName;
    }
}
