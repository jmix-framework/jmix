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

package io.jmix.ui.sys;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import static io.jmix.ui.sys.WebBrowserToolsImpl.BEFORE_UNLOAD_LISTENER;

@Component("ui_JmixBootstrapListener")
public class JmixBootstrapListener implements BootstrapListener {

    @Override
    public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
    }

    @Override
    public void modifyBootstrapPage(BootstrapPageResponse response) {
        Element head = response.getDocument().getElementsByTag("head").get(0);

        includeBeforeUnloadListener(response, head);
    }

    private static void includeBeforeUnloadListener(BootstrapPageResponse response, Element head) {
        Element script = response.getDocument().createElement("script");
        script.attr("type", "text/javascript");
        script.html(BEFORE_UNLOAD_LISTENER + " = (event) => {" +
                "event.preventDefault();" +
                "return (event.returnValue = '');};");
        head.appendChild(script);
    }
}
