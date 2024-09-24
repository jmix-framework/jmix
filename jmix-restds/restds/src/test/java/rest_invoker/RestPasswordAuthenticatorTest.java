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

package rest_invoker;

import io.jmix.restds.impl.RestInvoker;
import io.jmix.restds.impl.RestPasswordAuthenticator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import test_support.BaseRestDsIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RestPasswordAuthenticatorTest extends BaseRestDsIntegrationTest {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void testAuthenticationError() {
        RestInvoker restInvoker = applicationContext.getBean(RestInvoker.class, "restService2");

        RestPasswordAuthenticator authenticator = (RestPasswordAuthenticator) restInvoker.getAuthenticator();

        try {
            authenticator.authenticate("unknown", "unknown");
            fail("Should throw exception");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(BadCredentialsException.class);
        }
    }
}
