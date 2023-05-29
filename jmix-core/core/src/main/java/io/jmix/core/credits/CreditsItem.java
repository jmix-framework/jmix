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

package io.jmix.core.credits;

import jakarta.annotation.Nullable;
import java.util.Objects;

/**
 * Carries information about third-party software.
 */
public class CreditsItem implements Comparable<CreditsItem> {

    private String name;
    private String url;
    private String licenseName;
    private String licenseUrl;

    public CreditsItem(String name, @Nullable String url, @Nullable String licenseName, @Nullable String licenseUrl) {
        this.name = name;
        this.url = url != null ? url : "";
        this.licenseName = licenseName != null ? licenseName : "";
        this.licenseUrl = licenseUrl != null ? licenseUrl : "";
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    @Override
    public int compareTo(CreditsItem other) {
        return name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreditsItem that = (CreditsItem) o;
        return name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
