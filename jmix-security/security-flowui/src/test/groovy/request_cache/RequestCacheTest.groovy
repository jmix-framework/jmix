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

package request_cache

import io.jmix.securityflowui.security.FlowuiVaadinWebSecurity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.web.util.matcher.NegatedRequestMatcher
import org.springframework.security.web.util.matcher.RegexRequestMatcher
import spock.lang.Specification

class RequestCacheTest extends Specification {

    def "Image request should not be cached"(String uri, boolean cached) {
        given:
        var matcher = new NegatedRequestMatcher(new RegexRequestMatcher(FlowuiVaadinWebSecurity.RESOURCE_REQUEST_REGEXP, null, true));
        var request = new MockHttpServletRequest();
        request.setServletPath(uri)

        expect:
        matcher.matches(request) == cached

        where:
        uri                               | cached
        "/frontend/icons/add_stream.png"  | false
        "/frontend/icons/add_stream.PNG"  | false
        "/frontend/icons/add_stream.jpg"  | false
        "/frontend/icons/add_stream.JPG"  | false
        "/frontend/icons/add_stream.jpeg" | false
        "/frontend/icons/add_stream.JPEG" | false
        "/frontend/icons/add_stream.svg"  | false
        "/frontend/icons/add_stream.SVG"  | false
        "/frontend/icons/add_stream.gif"  | false
        "/frontend/icons/add_stream.GIF"  | false
        "/frontend/icons/add_stream.pdf"  | false
        "/frontend/icons/add_stream.PDF"  | false
        "/sec/resourceroles/"             | true
        "/test/"                          | true
        "/"                               | true
    }
}