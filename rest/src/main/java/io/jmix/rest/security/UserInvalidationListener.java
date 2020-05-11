/*
 * Copyright (c) 2008-2019 Haulmont.
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

import org.springframework.stereotype.Component;

@Component("jmix_UserInvalidationListener")
public class UserInvalidationListener {

    //todo User Session
//    private final Logger log = LoggerFactory.getLogger(UserInvalidationListener.class);
//
//    @Inject
//    protected UserManagementService userManagementService;
//
//    @Inject
//    protected ServerTokenStore serverTokenStore;
//
//    @Inject
//    protected UserSessionsImpl userSessionsAPI;
//
////    @Inject
////    protected Persistence persistence;
//
//    @Order(Events.HIGHEST_CORE_PRECEDENCE + 100)
//    @EventListener
//    public void handleUserInvalidation(UserInvalidationEvent event) {
//        User user = event.getSource();
//
//        log.info("Handling user invalidation: {}", user.getLogin());
//
//        try (Transaction tx = persistence.createTransaction()) {
//            List<UUID> sessionsIds = userSessionsAPI.getUserSessionsStream()
//                    .filter(session -> session != null &&
//                            (user.equals(session.getUser()) || user.equals(session.getSubstitutedUser())))
//                    .map(UserSession::getId)
//                    .collect(Collectors.toList());
//
//            sessionsIds.forEach(userSessionsAPI::killSession);
//
//            serverTokenStore.getAccessTokenValuesByUserLogin(user.getLogin())
//                    .forEach(serverTokenStore::removeAccessToken);
//
//            serverTokenStore.getRefreshTokenValuesByUserLogin(user.getLogin())
//                    .forEach(serverTokenStore::removeRefreshToken);
//
//            userManagementService.resetRememberMeTokens(Collections.singletonList(user.getId()));
//
//            tx.commit();
//
//            log.info("UserSessions, REST API & 'Remember me' tokens were invalidated for a user: {}", user.getLogin());
//        } catch (Throwable t) {
//            log.error("An error occurred while handling user invalidation for user: {}.", user.getLogin(), t);
//        }
//    }
}

