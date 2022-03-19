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

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.dashboards.utils.DashboardLayoutManager;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JmixEntity(name = "dshbrd_ResponsiveLayout")
public class ResponsiveLayout extends DashboardLayout implements ContainerLayout {

    @JmixProperty
    protected Integer xs = 12;

    @JmixProperty
    protected Integer sm = 6;

    @JmixProperty
    protected Integer md = 6;

    @JmixProperty
    protected Integer lg = 3;

    @JmixProperty
    protected Set<ResponsiveArea> areas = new HashSet<>();

    public Set<ResponsiveArea> getAreas() {
        return areas;
    }

    public void setAreas(Set<ResponsiveArea> areas) {
        this.areas = areas;
        this.areas.forEach(area -> area.getComponent().setParent(this));
    }

    @PostConstruct
    protected void initCaption(DashboardLayoutManager layoutManager) {
        setCaption(layoutManager.getCaption(this));
    }

    @Override
    public void addChild(DashboardLayout child) {
        throw new UnsupportedOperationException("Use addArea() method");
    }

    public void addArea(ResponsiveArea area) {
        Integer order = getAreas().stream()
                .map(ResponsiveArea::getOrder)
                .max(Comparator.naturalOrder()).orElse(0) + 1;
        area.setOrder(order);
        if (getAreas().add(area)) {
            area.getComponent().setParent(this);
        }
    }

    @Override
    public void removeOwnChild(DashboardLayout child) {
        ResponsiveArea target = null;
        for (ResponsiveArea area : areas) {
            if (area.getComponent().equals(child)) {
                target = area;
                break;
            }
        }
        if (areas.remove(target)) {
            child.setParent(null);
        }
    }

    @Override
    public List<DashboardLayout> getChildren() {
        return getAreas().stream()
                .sorted(Comparator.comparingInt(ResponsiveArea::getOrder))
                .map(ResponsiveArea::getComponent)
                .collect(Collectors.toList());
    }

    @Override
    public void setChildren(List<DashboardLayout> children) {

    }

    public ResponsiveArea findArea(DashboardLayout layout) {
        return getAreas().stream()
                .filter(e -> e.getComponent().getId().equals(layout.getId()))
                .findAny()
                .orElse(null);
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
}
