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

package test_support.entity.importexport;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import java.util.Set;

@JmixEntity
@Entity(name = "testimportexport_PricingRegion")
@Table(name = "TESTIMPORTEXPORT_PRICING_REGION")
public class PricingRegion extends StandardEntity {

    @Column(name = "NAME", nullable = false, length = 50)
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    protected PricingRegion parent;

    @OneToMany(mappedBy = "parent")
    @Composition
    protected Set<PricingRegion> children;

    public Set<PricingRegion> getChildren() {
        return children;
    }

    public void setChildren(Set<PricingRegion> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PricingRegion getParent() {
        return parent;
    }

    public void setParent(PricingRegion parent) {
        this.parent = parent;
    }
}