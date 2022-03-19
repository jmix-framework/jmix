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

package io.jmix.core.usersubstitution;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * Class stores user substitution information. It describes which user may substitute the other user and define a time
 * range for this substitution. If {@link #startDate} and {@code #endDate} are not defined then the user substitution is
 * permanent.
 */
public class UserSubstitution {

    protected String username;

    protected String substitutedUsername;

    //todo LocalDateTime
    protected Date startDate;

    protected Date endDate;

    public UserSubstitution(String username, String substitutedUsername) {
        this.username = username;
        this.substitutedUsername = substitutedUsername;
    }

    public UserSubstitution(String username, String substitutedUsername, @Nullable Date startDate, @Nullable Date endDate) {
        this.username = username;
        this.substitutedUsername = substitutedUsername;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getUsername() {
        return username;
    }

    public String getSubstitutedUsername() {
        return substitutedUsername;
    }

    @Nullable
    public Date getStartDate() {
        return startDate;
    }

    @Nullable
    public Date getEndDate() {
        return endDate;
    }
}
