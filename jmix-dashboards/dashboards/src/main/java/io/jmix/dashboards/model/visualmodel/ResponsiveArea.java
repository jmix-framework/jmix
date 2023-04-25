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
package io.jmix.dashboards.model.visualmodel;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity(name = "dshbrd_ResponsiveArea")
public class ResponsiveArea {
    @Id
    @JmixProperty
    @JmixGeneratedValue
    protected UUID id;

    @NotNull
    @JmixProperty(mandatory = true)
    protected DashboardLayout component;
    @NotNull
    @JmixProperty(mandatory = true)
    protected Integer order;

    @JmixProperty
    protected Integer xs;

    @JmixProperty
    protected Integer sm;

    @JmixProperty
    protected Integer md;

    @JmixProperty
    protected Integer lg;

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public DashboardLayout getComponent() {
        return component;
    }

    public void setComponent(DashboardLayout component) {
        this.component = component;
    }

    public Integer getXs() {
        return xs;
    }

    public void setXs(Integer xs) {
        this.xs = xs;
    }

    public Integer getSm() {
        return sm;
    }

    public void setSm(Integer sm) {
        this.sm = sm;
    }

    public Integer getMd() {
        return md;
    }

    public void setMd(Integer md) {
        this.md = md;
    }

    public Integer getLg() {
        return lg;
    }

    public void setLg(Integer lg) {
        this.lg = lg;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
