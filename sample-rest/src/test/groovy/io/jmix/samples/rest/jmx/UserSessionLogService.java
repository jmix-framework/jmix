/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.jmx;


public interface UserSessionLogService {
    void changeUserSessionLogEnabled(boolean enabled);

    String findLastLoggedSessionId();

    String getLastLoggedSessionAction(String sessionId);

    String cleanupLogEntries();
}
