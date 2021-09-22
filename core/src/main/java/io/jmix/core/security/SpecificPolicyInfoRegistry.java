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

package io.jmix.core.security;

import io.jmix.core.accesscontext.SpecificOperationAccessContext;

import java.util.List;

/**
 * Provides an information about specific policies defined in the application. In order to be returned by the service,
 * the specific policy must have a corresponding {@link SpecificOperationAccessContext}. E.g. for the {@code
 * ui.loginToUi} policy the following class should exist:
 *
 * <pre>
 * public class UiLoginToUiContext extends SpecificOperationAccessContext {
 *
 *     public static final String NAME = "ui.loginToUi";
 *
 *     public UiLoginToUiContext() {
 *         super(NAME);
 *     }
 * }
 * </pre>
 */
public interface SpecificPolicyInfoRegistry {

    /**
     * Returns an information about specific policies defined in the application
     */
    List<SpecificPolicyInfo> getSpecificPolicyInfos();

    /**
     * Class stores an information about specific security policy that may be used in the application
     */
    class SpecificPolicyInfo {

        private final String name;

        public SpecificPolicyInfo(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
