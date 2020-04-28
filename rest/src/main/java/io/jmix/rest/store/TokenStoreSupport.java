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

package io.jmix.rest.store;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component(TokenStoreSupport.NAME)
public class TokenStoreSupport {

    public static final String NAME = "jmix_TokenStoreSupport";

    public static final String JMIX_REST_SYNC_TOKEN_REPLICATION = "jmix.rest.syncTokenReplication";
    public static final String JMIX_REST_STORE_TOKENS_IN_DB = "jmix.rest.storeTokensInDb";

    @Inject
    protected Environment environment;

    public Boolean isRestSyncTokenReplication() {
        Boolean isSyncTokenReplication = environment.getProperty(JMIX_REST_SYNC_TOKEN_REPLICATION, Boolean.TYPE);
        return isSyncTokenReplication != null ? isSyncTokenReplication : false;
    }

    public Boolean isRestStoreTokensInDb() {
        Boolean isStoreTokensInDb = environment.getProperty(JMIX_REST_STORE_TOKENS_IN_DB, Boolean.TYPE);
        return isStoreTokensInDb != null ? isStoreTokensInDb : false;
    }
}
