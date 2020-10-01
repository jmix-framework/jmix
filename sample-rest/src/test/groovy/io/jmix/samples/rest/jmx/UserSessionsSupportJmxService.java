/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.jmx;

import com.haulmont.masquerade.jmx.JmxName;

@JmxName("app-core.refapp:type=UserSessionsSupport")
public interface UserSessionsSupportJmxService {
    String killSessions(String[] logins);
}
