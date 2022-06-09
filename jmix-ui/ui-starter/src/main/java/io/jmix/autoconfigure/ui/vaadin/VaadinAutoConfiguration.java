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

package io.jmix.autoconfigure.ui.vaadin;

import com.vaadin.spring.annotation.EnableVaadin;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.internal.SpringViewDisplayPostProcessor;
import com.vaadin.spring.navigator.SpringNavigator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author Petter Holmström (petter@vaadin.com)
 * @author Josh Long (josh@joshlong.com)
 * @author Henri Sara (hesara@vaadin.com)
 * @see com.vaadin.spring.annotation.EnableVaadin
 */
@AutoConfiguration
@ConditionalOnClass(SpringUI.class)
public class VaadinAutoConfiguration {

    private static Logger logger = LoggerFactory
            .getLogger(VaadinAutoConfiguration.class);

    @Configuration
    @EnableVaadin
    static class EnableVaadinConfiguration implements InitializingBean {
        @Override
        public void afterPropertiesSet() {
            logger.debug("{} initialized", getClass().getName());
        }
    }

    @Configuration
    // not using @EnableVaadinNavigation to enable each bean to have its own
    // condition
    static class EnableVaadinNavigatorConfiguration
            implements InitializingBean {

        @Bean
        public static SpringViewDisplayPostProcessor springViewDisplayPostProcessor() {
            return new SpringViewDisplayPostProcessor();
        }

        @Override
        public void afterPropertiesSet() {
            logger.debug("{} initialized", getClass().getName());
        }
    }

    @Configuration
    @ConditionalOnClass(name = "com.vaadin.spring.navigator.SpringNavigator")
    static class EnableSpringVaadinNavigatorConfiguration
            implements InitializingBean {

        @ConditionalOnMissingBean(type = "com.vaadin.spring.navigator.SpringNavigator")
        @Bean
        @UIScope
        public SpringNavigator vaadinNavigator() {
            return new SpringNavigator();
        }

        @Override
        public void afterPropertiesSet() {
            logger.debug("{} initialized", getClass().getName());
        }

    }

    @Configuration
    @EnableJmixVaadinServlet
    static class EnableVaadinServletConfiguration implements InitializingBean {
        @Override
        public void afterPropertiesSet() {
            logger.debug("{} initialized", getClass().getName());
        }
    }

    /**
     * Deploys JSR-356 websocket endpoints when Atmosphere is available.
     *
     * @return the server endpoint exporter which does the actual work.
     */
    @Bean
    public ServerEndpointExporter websocketEndpointDeployer() {
        return new VaadinWebsocketEndpointExporter();
    }
}
