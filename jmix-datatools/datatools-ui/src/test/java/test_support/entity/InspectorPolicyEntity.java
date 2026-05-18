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

package test_support.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.CascadeType;
import java.util.UUID;

@Table(name = "TEST_INSPECTOR_POLICY_ENTITY")
@Entity(name = "test_InspectorPolicyEntity")
@JmixEntity
public class InspectorPolicyEntity {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "NAME")
    protected String name;

    @JoinColumn(name = "ALLOWED_RELATION_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    protected InspectorPolicyRelatedEntity allowedRelation;

    @Embedded
    protected InspectorPolicyEmbeddable embeddedDetails;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "COMPOSED_ENTITY_ID")
    protected InspectorPolicyComposedEntity composedDetails;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InspectorPolicyRelatedEntity getAllowedRelation() {
        return allowedRelation;
    }

    public void setAllowedRelation(InspectorPolicyRelatedEntity allowedRelation) {
        this.allowedRelation = allowedRelation;
    }

    public InspectorPolicyEmbeddable getEmbeddedDetails() {
        return embeddedDetails;
    }

    public void setEmbeddedDetails(InspectorPolicyEmbeddable embeddedDetails) {
        this.embeddedDetails = embeddedDetails;
    }

    public InspectorPolicyComposedEntity getComposedDetails() {
        return composedDetails;
    }

    public void setComposedDetails(InspectorPolicyComposedEntity composedDetails) {
        this.composedDetails = composedDetails;
    }
}
