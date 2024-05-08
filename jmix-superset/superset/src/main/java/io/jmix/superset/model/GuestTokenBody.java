package io.jmix.superset.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GuestTokenBody implements Serializable {

    private List<Resource> resources = new ArrayList<>();
    private List<RowLevelRole> rls = new ArrayList<>();
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<RowLevelRole> getRls() {
        return rls;
    }

    public void setRls(List<RowLevelRole> rls) {
        this.rls = rls;
    }

    public static GuestTokenBodyBuilder builder() {
        return new GuestTokenBodyBuilder();
    }

    public record Resource(String id, String type) implements Serializable {
        public static final String DASHBOARD_TYPE = "dashboard";
    }

    public record RowLevelRole(String clause, Integer dataset) implements Serializable {
    }

    public record User(String username) implements Serializable {
    }

    public static class GuestTokenBodyBuilder {
        protected GuestTokenBody body = new GuestTokenBody();

        public GuestTokenBodyBuilder withResources(List<Resource> resources) {
            body.setResources(resources);
            return this;
        }

        public GuestTokenBodyBuilder withResource(Resource resource) {
            body.getResources().add(resource);
            return this;
        }

        public GuestTokenBodyBuilder withRowLevelRoles(List<RowLevelRole> rls) {
            body.setRls(rls);
            return this;
        }

        public GuestTokenBodyBuilder withRowLevelRole(RowLevelRole rowLevelRole) {
            body.getRls().add(rowLevelRole);
            return this;
        }

        public GuestTokenBodyBuilder withUser(User user) {
            body.setUser(user);
            return this;
        }

        public GuestTokenBody build() {
            return body;
        }
    }
}
