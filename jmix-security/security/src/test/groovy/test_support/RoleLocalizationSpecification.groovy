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

package test_support

import io.jmix.core.security.ClientDetails
import io.jmix.core.security.SecurityContextHelper
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

/**
 * Authenticates the user of a feature in a locale. German is the default locale of the module's tests, see
 * {@code test_support/test-app.properties}, so a user of another locale tells the text a design-time role keeps apart
 * from the text of the user's locale.
 */
abstract class RoleLocalizationSpecification extends SecuritySpecification {

    void cleanup() {
        SecurityContextHelper.setAuthentication(null)
    }

    static void authenticate(Locale locale) {
        def authentication = new UsernamePasswordAuthenticationToken('user', null, [])
        authentication.details = ClientDetails.builder().locale(locale).build()
        SecurityContextHelper.setAuthentication(authentication)
    }
}
