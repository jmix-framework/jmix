/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import io.jmix.flowui.util.WebBrowserTools;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import jakarta.servlet.ServletContext;

@Component("flowui_LogoutSupport")
public class LogoutSupport {

    protected ServletContext servletContext;

    public LogoutSupport(@Nullable ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void logout() {
        // window's 'beforeunload' event is triggered by changing Page's location,
        // so we need to remove 'beforeunload' event listener, because logout happens
        // anyway, even if a user stops browser tab closing, as a result it breaks app
        WebBrowserTools.allowBrowserTabClosing(UI.getCurrent())
                .then(jsonValue -> doLogout());
    }

    protected void doLogout() {
        String contextPath = servletContext == null ? null : servletContext.getContextPath();
        String logoutPath = Strings.isNullOrEmpty(contextPath) ? "/logout" : contextPath + "/logout";

        UI.getCurrent().getPage().setLocation(logoutPath);
    }
}
