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

package io.jmix.email;

/**
 * Should be used to delete old emails and attachments.
 */
public interface EmailCleaner {
    /**
     * Deletes old email messages and attachments.
     * <p>
     * Uses the settings of emails age ({@code jmix.email.maxAgeOfImportantMessages},
     * {@code jmix.email.maxAgeOfNonImportantMessages}) which are configurable by {@code application.properties}.
     * @return count of messages and attachments, that has been deleted
     */
    Integer deleteOldEmails();
}
