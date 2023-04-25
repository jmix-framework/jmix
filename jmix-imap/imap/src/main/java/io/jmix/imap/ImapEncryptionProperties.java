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

package io.jmix.imap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.imap.encryption")
public class ImapEncryptionProperties {
    String key;
    String iv;

    public ImapEncryptionProperties(@DefaultValue("HBXv3Q70IlmBMiW4EMyPHw==") String key,
                                    @DefaultValue("DYOKud/GWV5boeGvmR/ttg==") String iv) {
        this.key = key;
        this.iv = iv;
    }

    public String getKey() {
        return key;
    }

    public String getIv() {
        return iv;
    }
}
