/*
 * Copyright 2025 Haulmont.
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

package facet_registration;

import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.sys.registration.FacetRegistration;
import io.jmix.flowui.sys.registration.FacetRegistrationBuilder;
import org.junit.jupiter.api.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FacetRegistrationTestConfiguration {

    @Bean
    @Order(200)
    public FacetRegistration firstTimerFacet() {
        return FacetRegistrationBuilder.create(TestFirstTimerFacet.class)
                .replaceFacet(Timer.class)
                .build();
    }

    @Bean
    @Order(300)
    public FacetRegistration secondTimerFacet() {
        return FacetRegistrationBuilder.create(TestSecondTimerFacet.class)
                .replaceFacet(TestFirstTimerFacet.class)
                .build();
    }

    @Bean
    @Order(100)
    public FacetRegistration testThirdTimerFacet() {
        return FacetRegistrationBuilder.create(TestThirdTimerFacet.class)
                .replaceFacet(Timer.class)
                .withFacetLoader("timer", ExtTimerFacetLoader.class)
                .build();
    }
}
