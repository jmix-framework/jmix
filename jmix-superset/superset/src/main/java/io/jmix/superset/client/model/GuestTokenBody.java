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

package io.jmix.superset.client.model;

import io.jmix.core.common.util.Preconditions;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GuestTokenBody implements Serializable {

    private List<Resource> resources = new ArrayList<>();
    private List<RowLevelRole> rls = new ArrayList<>();
    private User user;

    @Nullable
    public User getUser() {
        return user;
    }

    public void setUser(@Nullable User user) {
        this.user = user;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(@Nullable List<Resource> resources) {
        this.resources = resources == null ? new ArrayList<>() : resources;
    }

    public List<RowLevelRole> getRls() {
        return rls;
    }

    public void setRls(@Nullable List<RowLevelRole> rls) {
        this.rls = rls == null ? new ArrayList<>() : rls;
    }

    public static GuestTokenBodyBuilder builder() {
        return new GuestTokenBodyBuilder();
    }

    public static class Resource implements Serializable {
        public static final String DASHBOARD_TYPE = "dashboard";
        protected String id;
        protected String type;

        @Nullable
        public String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        public Resource withId(@Nullable String id) {
            setId(id);
            return this;
        }

        @Nullable
        public String getType() {
            return type;
        }

        public void setType(@Nullable String type) {
            this.type = type;
        }

        public Resource withType(@Nullable String type) {
            setType(type);
            return this;
        }
    }

    public static class RowLevelRole implements Serializable {
        protected String clause;
        protected Integer dataset;

        @Nullable
        public String getClause() {
            return clause;
        }

        public void setClause(@Nullable String clause) {
            this.clause = clause;
        }

        public RowLevelRole withClause(@Nullable String clause) {
            setClause(clause);
            return this;
        }

        @Nullable
        public Integer getDataset() {
            return dataset;
        }

        public void setDataset(@Nullable Integer dataset) {
            this.dataset = dataset;
        }

        public RowLevelRole withDataset(@Nullable Integer dataset) {
            setDataset(dataset);
            return this;
        }
    }

    public static class User implements Serializable {
        protected String username;

        @Nullable
        public String getUsername() {
            return username;
        }

        public void setUsername(@Nullable String username) {
            this.username = username;
        }

        public User withUsername(@Nullable String username) {
            setUsername(username);
            return this;
        }
    }

    public static class GuestTokenBodyBuilder {
        protected GuestTokenBody body = new GuestTokenBody();

        public GuestTokenBodyBuilder withResources(@Nullable List<Resource> resources) {
            body.setResources(resources);
            return this;
        }

        public GuestTokenBodyBuilder withResource(Resource resource) {
            Preconditions.checkNotNullArgument(resource);
            body.getResources().add(resource);
            return this;
        }

        public GuestTokenBodyBuilder withRowLevelRoles(@Nullable List<RowLevelRole> rls) {
            body.setRls(rls);
            return this;
        }

        public GuestTokenBodyBuilder withRowLevelRole(RowLevelRole rowLevelRole) {
            Preconditions.checkNotNullArgument(rowLevelRole);
            body.getRls().add(rowLevelRole);
            return this;
        }

        public GuestTokenBodyBuilder withUser(User user) {
            Preconditions.checkNotNullArgument(user);
            body.setUser(user);
            return this;
        }

        public GuestTokenBody build() {
            return body;
        }
    }
}
