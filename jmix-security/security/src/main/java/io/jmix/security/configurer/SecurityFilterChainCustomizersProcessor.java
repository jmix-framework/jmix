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

package io.jmix.security.configurer;

import org.slf4j.Logger;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

//TODO javadoc, logs
@Component("sec_SecurityFilterChainCustomizersProcessor")
public class SecurityFilterChainCustomizersProcessor implements SmartInitializingSingleton {

    private static final Logger log = getLogger(SecurityFilterChainCustomizersProcessor.class);

    protected final List<SecurityFilterChainCustomizer> customizers;
    protected final Map<String, SecurityFilterChain> chains;

    public SecurityFilterChainCustomizersProcessor(List<SecurityFilterChainCustomizer> customizers,
                                                   Map<String, SecurityFilterChain> chains) {
        this.customizers = customizers;
        this.chains = chains;
    }

    @Override
    public void afterSingletonsInstantiated() {
        for (SecurityFilterChainCustomizer customizer : customizers) {
            if (!customizer.isEnabled()) {
                log.debug("SecurityFilterChainCustomizer '{}' is disabled", customizer);
                continue;
            }
            List<String> chainBeanNames = customizer.getChainBeanNames();
            log.info("[IVGA] SecurityFilterChainCustomizer '{}' tries to customize SecurityFilterChain: {}", customizer, chainBeanNames);
            for (String chainBeanName : chainBeanNames) {
                SecurityFilterChain chain = chains.get(chainBeanName);
                if (chain != null) {
                    log.info("[IVGA] SecurityFilterChain with name '{}' is found", chainBeanName);
                    customizer.customize(chainBeanName, chain);
                } else {
                    log.info("[IVGA] SecurityFilterChain with name '{}' is not found", chainBeanName);
                }
            }
        }
    }
}