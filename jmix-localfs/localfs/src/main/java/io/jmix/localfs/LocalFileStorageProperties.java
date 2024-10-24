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

package io.jmix.localfs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.localfs")
public class LocalFileStorageProperties {

    /**
     * {@link LocalFileStorage} storage directory. If not set, {@code jmix.core.work-dir/filestorage} will be used.
     */
    String storageDir;

    /**
     * Disables checking whether requested file is located inside the storage directory.
     */
    boolean disablePathCheck;

    public LocalFileStorageProperties(
            String storageDir,
            @DefaultValue("false") boolean disablePathCheck) {
        this.storageDir = storageDir;
        this.disablePathCheck = disablePathCheck;
    }

    /**
     * @see #storageDir
     */
    public String getStorageDir() {
        return storageDir;
    }

    /**
     * @see #disablePathCheck
     */
    public boolean isDisablePathCheck() {
        return disablePathCheck;
    }
}
