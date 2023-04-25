/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package test_support.entity.cars;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@JmixEntity
@Entity(name = "cars_Colour")
@Table(name = "CARS_COLOUR")
public class Colour extends BaseEntity {

    private static final long serialVersionUID = -6966135766799019463L;

    @Column(name = "NAME", nullable = false)
    @InstanceName
    private String name;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
