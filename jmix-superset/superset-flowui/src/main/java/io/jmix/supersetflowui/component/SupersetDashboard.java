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

package io.jmix.supersetflowui.component;

import io.jmix.core.common.util.Preconditions;
import io.jmix.superset.SupersetProperties;
import io.jmix.supersetflowui.DefaultGuestTokenProvider;
import io.jmix.supersetflowui.SupersetGuestTokenProvider;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstraintsProvider;
import jakarta.annotation.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import io.jmix.supersetflowui.kit.component.JmixSupersetDashboard;

/**
 * The component for showing embedded dashboards from Superset. It uses the embedded-sdk library on the client-side to
 * embed a dashboard into an IFrame.
 * <p>
 * To work with the component correctly, you should provide the embedded ID from a configured dashboard in Superset
 * and set it to the component.
 * <p>
 * By default, the component manages guest token getting and refreshing requests. It configures the request according
 * to the Superset API and contains the following information: dataset constraints, the embedded ID and the current
 * username.
 * <p>
 * The client-side of the component handles token expiration and the component requests fetching a new token. The
 * component enables setting a custom guest token provider that will replace the default one and will be invoked when
 * the token is about to expire.
 * <p>
 * The example of component in a view descriptor:
 * <pre>
 * xmlns:superset="http://jmix.io/schema/superset/ui"
 *
 * &lt;superset:dashboard id="dashboard"
 *                     height="100%"
 *                     width="100%"
 *                     embeddedId="d8bb6f86-dfef-4049-a39a-fd73da1a601d"/&gt;
 * </pre>
 */
public class SupersetDashboard extends JmixSupersetDashboard implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected DatasetConstraintsProvider datasetConstraintsProvider;
    protected SupersetGuestTokenProvider guestTokenProvider;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        guestTokenProvider = applicationContext.getBean(DefaultGuestTokenProvider.class);

        setUrlInternal(applicationContext.getBean(SupersetProperties.class).getUrl());
    }

    /**
     * @return dataset constraints provider or {@code null} if not set
     */
    @Nullable
    public DatasetConstraintsProvider getDatasetConstraintsProvider() {
        return datasetConstraintsProvider;
    }

    /**
     * Sets dataset constraints provider. These constraints are applied to datasets that are used in an embedded
     * dashboard {@link #getEmbeddedId()}.
     *
     * @param datasetConstraintsProvider dataset constraints providers
     */
    public void setDatasetConstraintsProvider(@Nullable DatasetConstraintsProvider datasetConstraintsProvider) {
        this.datasetConstraintsProvider = datasetConstraintsProvider;
    }

    /**
     * @return guest token provider
     */
    public SupersetGuestTokenProvider getGuestTokenProvider() {
        return guestTokenProvider;
    }

    /**
     * Sets a guest token provider. This provider will be used instead of default one.
     * <p>
     * The usage example you can find in {@link SupersetGuestTokenProvider}.
     *
     * @param guestTokenProvider provider to set
     */
    public void setGuestTokenProvider(SupersetGuestTokenProvider guestTokenProvider) {
        Preconditions.checkNotNullArgument(guestTokenProvider);
        this.guestTokenProvider = guestTokenProvider;
    }

    @Override
    protected void fetchGuestToken() {
        super.fetchGuestToken();

        guestTokenProvider.fetchGuestToken(
                new SupersetGuestTokenProvider.FetchGuestTokenContext(this),
                this::setGuestTokenInternal);
    }
}
