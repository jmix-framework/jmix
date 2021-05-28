/*
 * Copyright 2021 Haulmont.
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

package io.jmix.securityoauth2.impl;

import io.jmix.securityoauth2.SecurityOAuth2Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("sec_TokenMasker")
public class TokenMasker {

    protected static final String MASK_PREFIX = "***";

    @Autowired
    protected SecurityOAuth2Properties oauth2Properties;

    /**
     * Masks token value, i.e. replaces the actual value <b>5a4587a0-e070-11e0-ae98-67f86f948320</b> with the <b>***-67f86f948320</b>
     *
     * @param token initial token value
     * @return masked token value
     */
    public String maskToken(String token) {
        if (!oauth2Properties.isTokenMaskingEnabled()) {
            return token;
        } else {
            return token.length() > 23 ? MASK_PREFIX + token.substring(23) : MASK_PREFIX;
        }
    }
}
