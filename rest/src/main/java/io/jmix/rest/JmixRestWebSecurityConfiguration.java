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

package io.jmix.rest;

import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

//@Configuration
//@EnableWebSecurity
//@Order(200)
//todo remove JmixRestWebSecurityConfiguration ?
public class JmixRestWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

//    private static final String REST_API = "rest-api";
//
//    @Autowired
//    protected UserDetailsService userDetailsService;
//
//    @Bean
//    protected JmixUserAuthenticationProvider cubaUserAuthenticationProvider() {
//        return new JmixUserAuthenticationProvider(userDetailsService);
//    }
//
//    @Bean
//    public AuthenticationEntryPoint authenticationEntryPoint() {
//        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
//        entryPoint.setRealmName(REST_API);
//        return entryPoint;
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .anyRequest().authenticated()
//                .and()
//                .httpBasic().authenticationEntryPoint(authenticationEntryPoint());
//    }
//
//    @Autowired
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) {
//        auth.authenticationProvider(cubaUserAuthenticationProvider());
//    }
}
