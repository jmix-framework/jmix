/*
 * Copyright 2020 Haulmont.
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

package io.jmix.autoconfigure.sessions;

import io.jmix.core.CoreConfiguration;
import io.jmix.sessions.SessionsConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;

import java.util.HashMap;

@Configuration
@Import({CoreConfiguration.class, SessionsConfiguration.class})
public class SessionsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SessionRepository.class)
    public SessionRepository<MapSession> sessionRepository(){
        return new MapSessionRepository(new HashMap<>());
    }
}
