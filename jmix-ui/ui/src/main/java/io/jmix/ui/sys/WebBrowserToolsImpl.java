/*
 * Copyright 2019 Haulmont.
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

import com.vaadin.shared.ui.BorderStyle;
import com.vaadin.spring.annotation.UIScope;
import io.jmix.ui.AppUI;
import io.jmix.ui.WebBrowserTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;

@UIScope
@Component("ui_WebBrowserTools")
public class WebBrowserToolsImpl implements WebBrowserTools {

    public static final String BEFORE_UNLOAD_LISTENER = "jmixBeforeUnloadListener";

    protected AppUI ui;

    @Autowired
    public void setAppUi(AppUI ui) {
        this.ui = ui;
    }

    @Override
    public void showWebPage(String url, @Nullable Map<String, Object> params) {
        String target = null;
        Integer width = null;
        Integer height = null;
        String border = "DEFAULT";
        Boolean tryToOpenAsPopup = null;
        if (params != null) {
            target = (String) params.get("target");
            width = (Integer) params.get("width");
            height = (Integer) params.get("height");
            border = (String) params.get("border");
            tryToOpenAsPopup = (Boolean) params.get("tryToOpenAsPopup");
        }
        if (target == null) {
            target = "_blank";
        }
        if (width != null && height != null && border != null) {
            ui.getPage().open(url, target, width, height, BorderStyle.valueOf(border));
        } else if (tryToOpenAsPopup != null) {
            ui.getPage().open(url, target, tryToOpenAsPopup);
        } else {
            ui.getPage().open(url, target, false);
        }
    }

    @Override
    public void preventBrowserTabClosing() {
        ui.getPage().getJavaScript().execute(
                "window.addEventListener('beforeunload', " + BEFORE_UNLOAD_LISTENER + ", {capture: true})"
        );
    }

    @Override
    public void allowBrowserTabClosing() {
        ui.getPage().getJavaScript().execute(
                "window.removeEventListener('beforeunload', " + BEFORE_UNLOAD_LISTENER + ", {capture: true})"
        );
    }
}