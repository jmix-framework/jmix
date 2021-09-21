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

package io.jmix.securitydata.impl;

import io.jmix.core.TimeSource;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.UserSubstitutionManager;
import io.jmix.core.security.event.UserSubstitutedEvent;
import io.jmix.core.security.impl.SubstitutedUserAuthenticationToken;
import io.jmix.securitydata.entity.UserSubstitution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component("sec_UserSubstitutionManager")
public class UserSubstitutionManagerImpl implements UserSubstitutionManager {

    @Autowired
    private UnconstrainedDataManager dataManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TimeSource timeSource;
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Override
    public List<UserDetails> getCurrentSubstitutedUsers() {
        return getSubstitutedUsers(currentAuthentication.getUser().getUsername());
    }


    @Override
    public List<UserDetails> getSubstitutedUsers(String userName) {

        List<UserSubstitution> userSubstitutions = dataManager.load(UserSubstitution.class)
                .query("e.userName=:userName " +
                        "and (e.startDate is null or e.startDate<=:currentDate) " +
                        "and (e.endDate is null or e.endDate>=:currentDate)")
                .parameter("currentDate", timeSource.currentTimestamp())
                .parameter("userName", userName)
                .list();
        List<UserDetails> result = new LinkedList<>();
        for (UserSubstitution substitution : userSubstitutions) {
            result.add(userRepository.loadUserByUsername(substitution.getSubstitutedUserName()));
        }
        return result;
    }

    /**
     * Check {@link UserSubstitution} records and performs user substitution
     *
     * @throws IllegalArgumentException if current user isn't allowed to substitute user with specified name
     */
    public void substituteUser(String substitutedUserName) {

        if (!canSubstitute(currentAuthentication.getUser().getUsername(), substitutedUserName)) {
            throw new IllegalArgumentException(
                    String.format("User '%s' cannot substitute '%s'",
                            currentAuthentication.getUser().getUsername(),
                            substitutedUserName));
        }

        SubstitutedUserAuthenticationToken authentication = (SubstitutedUserAuthenticationToken) authenticationManager.authenticate(
                new SubstitutedUserAuthenticationToken(Objects.requireNonNull(currentAuthentication.getAuthentication()),
                        substitutedUserName));

        SecurityContextHelper.setAuthentication(authentication);
        eventPublisher.publishEvent(new UserSubstitutedEvent((UserDetails) authentication.getPrincipal(), (UserDetails) authentication.getSubstitutedPrincipal()));
    }

    protected boolean canSubstitute(String userName, String substitutedUserName) {
        return userName.equals(substitutedUserName)
                || getSubstitutedUsers(userName).stream()
                .anyMatch(userDetails -> userDetails.getUsername().equals(substitutedUserName));
    }


}
