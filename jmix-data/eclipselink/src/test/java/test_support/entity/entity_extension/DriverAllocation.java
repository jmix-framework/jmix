/*
 * Copyright 2020 Haulmont.
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
package test_support.entity.entity_extension;

import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.UUID;

@JmixEntity
@Entity(name = "exttest_DriverAllocation")
@Table(name = "EXTTEST_DRIVER_ALLOC")
public class DriverAllocation {
    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "CAR")
    private String car;

    @ManyToOne
    @JoinColumn(name = "DRIVER_ID")
    @OnDeleteInverse(DeletePolicy.DENY)
    private Driver driver;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    @InstanceName
    @DependsOnProperties({"driver", "car"})
    public String getCaption(MetadataTools metadataTools) {
        String str = driver == null ? "<no driver>" : metadataTools.getInstanceName(driver);
        str += " : ";
        str += car == null ? "<no car>" : car;
        return str;
    }
}
