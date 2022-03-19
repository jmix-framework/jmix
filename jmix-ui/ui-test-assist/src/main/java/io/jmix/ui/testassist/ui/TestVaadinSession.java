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

package io.jmix.ui.testassist.ui;

import com.vaadin.server.*;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;
import java.util.Locale;

public class TestVaadinSession extends VaadinSession {

    protected WebBrowser webBrowser;
    protected WrappedSession wrappedSession;

    public TestVaadinSession(WebBrowser webBrowser, Locale locale) {
        super(new VaadinServletService(){});
        this.webBrowser = webBrowser;
        setLocale(locale);
        wrappedSession = new WrappedHttpSession(new MockHttpSession());
    }

    @Override
    public boolean hasLock() {
        return true;
    }

    @Override
    public void lock() {
    }

    @Override
    public void unlock() {
    }

    @Override
    public WrappedSession getSession() {
        return wrappedSession;
    }

    @Override
    public WebBrowser getBrowser() {
        return webBrowser;
    }
}