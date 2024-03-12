/*
 * Copyright 2024 Haulmont.
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

package io.jmix.securityflowui.util;

import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.internal.NavigationRouteTarget;
import com.vaadin.flow.router.internal.RouteTarget;
import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.VaadinConfigurationProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A copy of {@link com.vaadin.flow.spring.security.RequestUtil} from the Vaadin 24.1 Its {@link #isAnonymousRoute}
 * method doesn't rely on the {@link NavigationAccessControl} introduced in Vaadin 24.3. It uses the
 * {@link AccessAnnotationChecker} instead to check whether the view should be available for anonymous users. We needed
 * this because anonymous view access stopped working in Jmix 2.2.0. The current class will be removed after related
 * issue is fixed.
 *
 * @see <a href="https://github.com/jmix-framework/jmix/issues/2985">Jmix GitHub issue</a>
 */
@Component
public class PrevVaadinRequestUtil {

    @Autowired
    private AccessAnnotationChecker accessAnnotationChecker;

    @Autowired
    private VaadinConfigurationProperties configurationProperties;

    @Autowired
    private ServletRegistrationBean<SpringServlet> springServletRegistration;

    public boolean isAnonymousRoute(HttpServletRequest request) {
        String vaadinMapping = configurationProperties.getUrlMapping();
        String requestedPath = HandlerHelper
                .getRequestPathInsideContext(request);
        Optional<String> maybePath = HandlerHelper
                .getPathIfInsideServlet(vaadinMapping, requestedPath);
        if (!maybePath.isPresent()) {
            return false;
        }
        String path = maybePath.get();
        if (path.startsWith("/")) {
            // Requested path includes a beginning "/" but route mapping is done
            // without one
            path = path.substring(1);
        }

        SpringServlet servlet = springServletRegistration.getServlet();
        VaadinService service = servlet.getService();
        if (service == null) {
            // The service has not yet been initialized. We cannot know if this
            // is an anonymous route, so better say it is not.
            return false;
        }
        Router router = service.getRouter();
        RouteRegistry routeRegistry = router.getRegistry();

        NavigationRouteTarget target = routeRegistry
                .getNavigationRouteTarget(path);
        if (target == null) {
            return false;
        }
        RouteTarget routeTarget = target.getRouteTarget();
        if (routeTarget == null) {
            return false;
        }
        Class<? extends com.vaadin.flow.component.Component> targetView = routeTarget
                .getTarget();
        if (targetView == null) {
            return false;
        }

        // Check if a not authenticated user can access the view
        boolean result = accessAnnotationChecker.hasAccess(targetView, null,
                role -> false);
        if (result) {
            getLogger().debug(path + " refers to a public view");
        }
        return result;
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

}
