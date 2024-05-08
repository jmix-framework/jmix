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

import com.google.common.base.Strings;
import com.vaadin.flow.component.AttachEvent;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.backgroundtask.*;
import io.jmix.superset.SupersetProperties;
import io.jmix.superset.SupersetService;
import io.jmix.superset.model.GuestTokenBody;
import io.jmix.superset.model.GuestTokenResponse;
import io.jmix.supersetflowui.SupersetTokenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import io.jmix.supersetflowui.kit.component.JmixSupersetDashboard;

import static io.jmix.superset.model.GuestTokenBody.Resource.DASHBOARD_TYPE;

public class SupersetDashboard extends JmixSupersetDashboard implements ApplicationContextAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(SupersetDashboard.class);

    protected ApplicationContext applicationContext;
    protected SupersetTokenHandler supersetTokenHandler;
    protected CurrentUserSubstitution currentUserSubstitution;
    protected SupersetProperties supersetProperties;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        supersetTokenHandler = applicationContext.getBean(SupersetTokenHandler.class,
                applicationContext.getBean(SupersetService.class),
                applicationContext.getBean(BackgroundWorker.class));
        currentUserSubstitution = applicationContext.getBean(CurrentUserSubstitution.class);
        supersetProperties = applicationContext.getBean(SupersetProperties.class);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        initSupersetDomain();
        initGuestToken();

        requestUpdateDashboard();
    }

    protected GuestTokenBody buildGuestTokenBody() {
        if (Strings.isNullOrEmpty(getEmbeddedId())) {
            throw new IllegalStateException("Embedded id is required");
        }

        return GuestTokenBody.builder()
                .withResource(new GuestTokenBody.Resource(getEmbeddedId(), DASHBOARD_TYPE))
                .withUser(new GuestTokenBody.User(currentUserSubstitution.getEffectiveUser().getUsername()))
                .build();
    }

    protected void initSupersetDomain() {
        if (Strings.isNullOrEmpty(supersetDomain)
                && Strings.isNullOrEmpty(supersetProperties.getUrl())) {
            log.warn("Superset dashboard url is empty. Specify 'jmix.superset.url' property or" +
                    " set 'domain' attribute directly");
            return;
        }

        setSupersetDomainInternal(!Strings.isNullOrEmpty(supersetDomain)
                ? supersetDomain
                : supersetProperties.getUrl());
    }

    protected void initGuestToken() {
        if (guestToken == null) {
            supersetTokenHandler.requestGuestToken(buildGuestTokenBody(), this::onGuestTokenSuccess);
        } else {
            setGuestTokenInternal(guestToken);
        }
    }

    protected void onGuestTokenSuccess(GuestTokenResponse response) {
        setGuestTokenInternal(response.getToken());

        // todo rp
        requestUpdateDashboard();
    }
}
