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

package io.jmix.superset.service;

import io.jmix.superset.service.model.*;
import jakarta.annotation.Nullable;

public interface SupersetService {

    LoginResponse login();

    LoginResponse login(LoginBody body);

    RefreshResponse refresh(String refreshToken);

    GuestTokenResponse getGuestToken(GuestTokenBody body, String accessToken, @Nullable String csrfToken);

    CsrfTokenResponse getCsrfToken(String accessToken);
}
