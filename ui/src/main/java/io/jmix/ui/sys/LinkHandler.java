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

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.Window;
import io.jmix.ui.sys.linkhandling.ExternalLinkContext;
import io.jmix.ui.sys.linkhandling.LinkHandlerProcessor;
import org.springframework.context.annotation.Scope;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

/**
 * Handles links from outside of the application.
 * <br> This bean is used particularly when a request URL contains one of
 * {@link io.jmix.ui.UiProperties#getLinkHandlerActions()} actions.
 * <br> The bean traverses all implementations of {@link LinkHandlerProcessor}
 * by their priority and gives control to first possible to handle processor.
 */
@org.springframework.stereotype.Component("ui_LinkHandler")
@Scope("prototype")
public class LinkHandler {

    @Autowired
    protected List<LinkHandlerProcessor> processors;

    // todo should be bound to AppUI
    protected App app;
    protected String action;
    protected Map<String, String> requestParams;

    public LinkHandler(App app, String action, Map<String, String> requestParams) {
        this.app = app;
        this.action = action;
        this.requestParams = requestParams;
    }

    /**
     * Check state of LinkHandler and application.
     *
     * @return true if application and LinkHandler in an appropriate state.
     */
    public boolean canHandleLink() {
        return AppUI.getCurrent().getTopLevelWindow().getFrameOwner() instanceof Window.HasWorkArea;
    }

    /**
     * Called to handle the link.
     */
    public void handle() {
        try {
            ExternalLinkContext linkContext = new ExternalLinkContext(requestParams, action, app);
            for (LinkHandlerProcessor processor : processors) {
                if (processor.canHandle(linkContext)) {
                    processor.handle(linkContext);
                    break;
                }
            }
        } finally {
            VaadinRequest request = VaadinService.getCurrentRequest();
            WrappedSession wrappedSession = request.getWrappedSession();
            wrappedSession.removeAttribute(AppUI.LAST_REQUEST_PARAMS_ATTR);
            wrappedSession.removeAttribute(AppUI.LAST_REQUEST_ACTION_ATTR);
        }
    }
}
