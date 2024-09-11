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

package test_support;

import io.jmix.restds.impl.RestTokenHolder;

public class ThreadLocalRestTokenHolder implements RestTokenHolder {

    private static ThreadLocal<String> accessTokenHolder = new ThreadLocal<>();
    private static ThreadLocal<String> refreshTokenHolder = new ThreadLocal<>();

    @Override
    public String getAccessToken() {
        return accessTokenHolder.get();
    }

    @Override
    public String getRefreshToken() {
        return refreshTokenHolder.get();
    }

    @Override
    public void setTokens(String accessToken, String refreshToken) {
        accessTokenHolder.set(accessToken);
        refreshTokenHolder.set(refreshToken);
    }
}
