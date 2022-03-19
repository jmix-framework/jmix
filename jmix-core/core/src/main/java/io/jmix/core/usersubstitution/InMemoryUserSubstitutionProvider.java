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

package io.jmix.core.usersubstitution;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.core.usersubstitution.event.UserSubstitutionsChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@link UserSubstitutionProvider} that stores {@link UserSubstitution} in memory.
 */
@Component("core_InMemoryUserSubstitutionProvider")
public class InMemoryUserSubstitutionProvider implements UserSubstitutionProvider {

    //the key of the map is UserSubstitution.username
    protected Multimap<String, UserSubstitution> userSubstitutions = HashMultimap.create();

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Override
    public Collection<UserSubstitution> getUserSubstitutions(String username, Date date) {
        return userSubstitutions.get(username).stream()
                .filter(userSubstitution -> (userSubstitution.getStartDate() == null || userSubstitution.getStartDate().before(date)) &&
                        (userSubstitution.getEndDate() == null || userSubstitution.getEndDate().after(date)))
                .collect(Collectors.toList());
    }

    public void addUserSubstitution(UserSubstitution userSubstitution) {
        this.userSubstitutions.put(userSubstitution.getUsername(), userSubstitution);
    }

    public void clear() {
        this.userSubstitutions.clear();
    }

    @EventListener
    protected void onUserRemove(UserRemovedEvent event) {
        String username = event.getUsername();
        Set<String> usernames = new HashSet<>();

        if (userSubstitutions.containsKey(username)) {
            userSubstitutions.removeAll(username);
            usernames.add(username);
        }

        List<UserSubstitution> substitutions = userSubstitutions.values().stream()
                .filter(userSubstitution ->
                        userSubstitution.getSubstitutedUsername().equals(username))
                .collect(Collectors.toList());

        for (UserSubstitution substitution : substitutions) {
            userSubstitutions.remove(substitution.getUsername(), substitution);
            usernames.add(substitution.getUsername());
        }

        for (String name : usernames) {
            fireUserSubstitutionsChanged(name);
        }
    }

    protected void fireUserSubstitutionsChanged(String username) {
        UserSubstitutionsChangedEvent changedEvent = new UserSubstitutionsChangedEvent(username);
        eventPublisher.publishEvent(changedEvent);
    }
}
