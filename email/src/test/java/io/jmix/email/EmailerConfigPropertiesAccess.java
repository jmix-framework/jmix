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

package io.jmix.email;

public class EmailerConfigPropertiesAccess {
    public static void setScheduledSendingDelayCallCount(EmailerProperties properties, int scheduledSendingDelayCallCount) {
        properties.scheduledSendingDelayCallCount = scheduledSendingDelayCallCount;
    }
    public static void setUseFileStorage(EmailerProperties properties, boolean useFileStorage) {
        properties.useFileStorage = useFileStorage;
    }

    public static void setFromAddress(EmailerProperties properties, String fromAddress) {
        properties.fromAddress = fromAddress;
    }

    public static void setSendAllToAdmin(EmailerProperties properties, boolean sendAllToAdmin) {
        properties.sendAllToAdmin = sendAllToAdmin;
    }

    public static void setAdminAddress(EmailerProperties properties, String adminAddress) {
        properties.adminAddress = adminAddress;
    }

    public static void setMaxAgeOfImportantMessages(EmailerProperties properties, int maxAgeOfImportantMessages) {
        properties.maxAgeOfImportantMessages = maxAgeOfImportantMessages;
    }

    public static void setMaxAgeOfNonImportantMessages(EmailerProperties properties, int maxAgeOfNonImportantMessages) {
        properties.maxAgeOfNonImportantMessages = maxAgeOfNonImportantMessages;
    }

    public static void setCleanFileStorage(EmailerProperties properties, boolean cleanFileStorage) {
        properties.cleanFileStorage = cleanFileStorage;
    }
}
