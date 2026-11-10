/*
 * Copyright 2026 Haulmont.
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

package io.jmix.multitenancy.event;

import io.jmix.core.cluster.ClusterApplicationEvent;

/**
 * Sent when a tenant is created or deleted. The event is published cluster-wide, so it is received by all
 * application instances, including the one where the tenant was changed.
 */
public class TenantEvent extends ClusterApplicationEvent {

    protected final Type type;
    protected final String tenantId;

    public TenantEvent(Object source, Type type, String tenantId) {
        super(source);
        this.type = type;
        this.tenantId = tenantId;
    }

    public Type getType() {
        return type;
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String toString() {
        return "TenantEvent{" +
               "type=" + type +
               ", tenantId='" + tenantId + '\'' +
               '}';
    }

    public enum Type {
        CREATED, DELETED
    }
}
