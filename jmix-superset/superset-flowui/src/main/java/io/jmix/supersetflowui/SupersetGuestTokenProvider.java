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

package io.jmix.supersetflowui;

import io.jmix.superset.client.SupersetClient;
import io.jmix.superset.client.cookie.SupersetCookieManager;
import io.jmix.supersetflowui.component.SupersetDashboard;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstrainsProvider;
import jakarta.annotation.Nullable;

import java.net.http.HttpClient;
import java.util.function.Consumer;

/**
 * The interface to be implemented by Spring bean classes that should provide a guest token for the
 * {@link SupersetDashboard}. The component loader will try to get an instance of provider using a bean name that
 * should be defined in {@code guestTokenProviderBean} attribute. For instance:
 * <pre>
 * &lt;superset:dashboard id="dashboard"
 *                     height="100%"
 *                     width="100%"
 *                     guestTokenProviderBean="app_CustomGuestTokenProvider"&gt;
 * </pre>
 *
 * And the bean:
 *
 * <pre>
 * &#064;Component("app_CustomGuestTokenProvider")
 * public class CustomGuestTokenProvider implements SupersetGuestTokenProvider {
 *     &#064;Override
 *     public void fetchGuestToken(FetchGuestTokenContext context, Consumer&lt;String&gt; callback) {
 *         // fetching guest token
 *     }
 * </pre>
 *
 * This bean will replace the default guest token provider {@link DefaultGuestTokenProvider}.
 * <p>
 * The implementation of this provider can be useful if you have different mechanism of handling access, refresh, CSRF
 * tokens. For instance, if you have your own service that uses Superset API you can create a Spring bean that will
 * fetch a guest token from your service.
 *
 * <pre>
 * &#064;Component("app_CustomGuestTokenProvider")
 * public class CustomGuestTokenProvider implements SupersetGuestTokenProvider {
 *     private static final Logger log = LoggerFactory.getLogger(CustomGuestTokenProvider.class);
 *
 *     private TaskExecutor taskExecutor;
 *     private MySupersetService MySupersetService;
 *
 *     public CustomGuestTokenProvider(MySupersetService mySupersetService,
 *                                     TaskExecutor taskExecutor {
 *         this.supersetClient = supersetClient;
 *         this.taskExecutor = taskExecutor;
 *         // ...
 *     }
 *
 *     &#064;Override
 *     public void fetchGuestToken(FetchGuestTokenContext context, Consumer&lt;String&gt; callback) {
 *         taskExecutor.execute(() -> {
 *             try {
 *                 String guestToken = mySupersetService
 *                         .fetchGuestToken(buildGuestTokenBody(context))
 *                         .getToken();
 *
 *                 context.getSource().getUI()
 *                         .ifPresent(ui -> ui.access(() -> callback.accept(guestToken)));
 *             } catch (IOException | InterruptedException e) {
 *                 log.error("Could not fetch guest token", e);
 *             }
 *         });
 *     }
 *
 *     // other methods
 * }
 * </pre>
 *
 * Please note that if the CSRF protection is enabled, the request of guest token will require a special cookie that
 * Superset sends with CSRF token. It means that the HTTP client you use for performing HTTP requests should use some
 * kind of cookie manager. For instance, the default implementation of{@link SupersetClient} uses {@link HttpClient}
 * and {@link SupersetCookieManager} for managing cookies.
 */
public interface SupersetGuestTokenProvider {

    /**
     * Fetches a guest token. When guest token is fetched, method should invoke a callback to provide the token to a
     * component.
     *
     * @param context  the context for fetching guest token
     * @param callback the callback, that should be invoked when guest token is fetched
     */
    void fetchGuestToken(FetchGuestTokenContext context, Consumer<String> callback);

    /**
     * The context for fetching a guest token. It contains a source component that requests a guest token and
     * provides predefined methods to help build a request.
     */
    class FetchGuestTokenContext {

        protected final SupersetDashboard source;

        public FetchGuestTokenContext(SupersetDashboard source) {
            this.source = source;
        }

        /**
         * @return the component that requests a guest token
         */
        public SupersetDashboard getSource() {
            return source;
        }

        /**
         * @return the embedded ID of dashboard
         */
        public String getEmbeddedId() {
            return source.getEmbeddedId();
        }

        /**
         * @return the dataset constraints provider or {@code null} if not set
         */
        @Nullable
        public DatasetConstrainsProvider getDatasetConstrainsProvider() {
            return source.getDatasetConstrainsProvider();
        }
    }
}
