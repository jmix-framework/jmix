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

package cors;

import test_support.AbstractRestControllerFT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jmix.rest.allowedOrigins = http://www.allowed1.com, http://www.allowed2.com"
})
public class CorsTest extends AbstractRestControllerFT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCorsAllowed1() throws Exception {
        String accessToken = obtainAccessToken("admin", "admin123", this.mockMvc);
        this.mockMvc.perform(options("/rest/entities/ref_Car")
                .header("Authorization", "Bearer " + accessToken)
                .header("Origin", "http://www.allowed1.com"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCorsAllowed2() throws Exception {
        String accessToken = obtainAccessToken("admin", "admin123", this.mockMvc);
        this.mockMvc.perform(options("/rest/entities/ref_Car")
                .header("Authorization", "Bearer " + accessToken)
                .header("Origin", "http://www.allowed2.com"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCorsForbidden() throws Exception {
        String accessToken = obtainAccessToken("admin", "admin123", this.mockMvc);
        this.mockMvc.perform(options("/rest/entities/ref_Car")
                .header("Authorization", "Bearer " + accessToken)
                .header("Origin", "http://www.forbidden.com"))
                .andExpect(status().isForbidden());
    }

    //todo move to helper class
    public static String obtainAccessToken(String username, String password, MockMvc mockMvc) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", username);
        params.add("password", password);

        ResultActions result = mockMvc.perform(post("/oauth/token")
                .params(params)
                .with(httpBasic("client", "secret"))
                .accept("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("access_token").toString();
    }

}
