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

package io.jmix.samples.rest;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticator;
import io.jmix.data.DataConfiguration;
import io.jmix.rest.RestConfiguration;
import io.jmix.security.SecurityConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;

@SpringBootApplication
@Import({CoreConfiguration.class,
        SecurityConfiguration.class,
        DataConfiguration.class,
        RestConfiguration.class})
public class SampleRestApplication {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    Authenticator authenticator;

    public static void main(String[] args) {
        SpringApplication.run(SampleRestApplication.class, args);
    }

    @EventListener(ApplicationStartedEvent.class)
    private void onStartup() {
        authenticator.withSystem(() -> {
            Greeting greeting = dataManager.create(Greeting.class);
            greeting.setText("Hello");
            dataManager.save(greeting);

            return null;
        });
    }

    @Bean
    protected DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

//    @Bean
//    public SessionRegistry sessionRegistry() {
//        return new SessionRegistryImpl();
//    }
//
//    @Bean
//    public SessionAuthenticationStrategy sessionAuthenticationStrategy(){
//        return new JmixSessionAuthenticationStrategy();
//    }
//
//    @Bean
//    public RememberMeServices rememberMeServices(){
//        return new SpringSessionRememberMeServices();
//    }
}
