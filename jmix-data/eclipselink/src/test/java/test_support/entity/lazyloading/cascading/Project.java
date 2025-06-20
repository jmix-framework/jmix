/*
 * Copyright 2025 Haulmont.
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

package test_support.entity.lazyloading.cascading;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;

@Table(name = "TEST_LLC_PROJECT")
@JmixEntity
@Entity(name = "test_llc_Project")
public class Project {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "DETAILS", nullable = false)
    private String details;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = {PERSIST, REMOVE, DETACH}, orphanRemoval = true)
    private List<ResourceAllocation> mandatoryResources;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<ResourceAllocation> optionalResources;

    public List<ResourceAllocation> getMandatoryResources() {
        return mandatoryResources;
    }

    public void setMandatoryResources(List<ResourceAllocation> mandatoryResources) {
        this.mandatoryResources = mandatoryResources;
    }

    public List<ResourceAllocation> getOptionalResources() {
        return optionalResources;
    }

    public void setOptionalResources(List<ResourceAllocation> optionalResources) {
        this.optionalResources = optionalResources;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
