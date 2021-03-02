/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.impl.session;

import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;

@Component("core_SessionData")
@Scope(value = WebApplicationContext.SCOPE_SESSION)
public class SessionDataImpl implements SessionData {

    private static final long serialVersionUID = 229714256878675372L;

    @Autowired
    private ObjectFactory<HttpSession> httpSessionFactory;

    @Override
    public Collection<String> getAttributeNames() {
        return Collections.unmodifiableList(Collections.list(getHttpSession().getAttributeNames()));
    }

    @Nullable
    @Override
    public Object getAttribute(String name) {
        return getHttpSession().getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object attribute) {
        getHttpSession().setAttribute(name, attribute);
    }

    @Override
    public String getSessionId() {
        return getHttpSession().getId();
    }

    private HttpSession getHttpSession() {
        return httpSessionFactory.getObject();
    }
}