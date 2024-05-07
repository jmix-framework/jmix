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

import com.vaadin.flow.component.AttachEvent;
import io.jmix.flowui.backgroundtask.*;
import io.jmix.superset.SupersetService;
import io.jmix.supersetflowui.SupersetTokenHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import supersetflowui.kit.component.JmixSupersetDashboard;

public class SupersetDashboard extends JmixSupersetDashboard implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected SupersetTokenHandler supersetTokenHandler;

    protected String embeddedId;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        supersetTokenHandler = applicationContext.getBean(SupersetTokenHandler.class,
                applicationContext.getBean(SupersetService.class),
                applicationContext.getBean(BackgroundWorker.class));
    }

    public String getEmbeddedId() {
        return embeddedId;
    }

    public void setDashboardEmbeddedId(String embeddedId) {
        this.embeddedId = embeddedId;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        supersetTokenHandler.loginToSuperset();
    }


}
