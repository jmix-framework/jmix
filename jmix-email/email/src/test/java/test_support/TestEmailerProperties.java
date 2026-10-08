/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.email.EmailerProperties;

/**
 * Creates {@link EmailerProperties} instances with default values for unit tests.
 */
public class TestEmailerProperties {

    public static EmailerProperties create(EmailerProperties.OAuth2 oauth2) {
        return new EmailerProperties("DoNotReply@localhost", 2, 100, 10, 240, "admin@localhost",
                false, false, "admin", "0 * * * * ?", 0, 0, "0 0 0 * * ?", false, oauth2);
    }
}
