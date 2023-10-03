/*
 * Copyright 2019 Haulmont.
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

package test_support.app.entity;

import io.jmix.core.metamodel.annotation.*;
import test_support.base.entity.BaseEntity;

import javax.persistence.*;

@Entity(name = "app_Pet")
@JmixEntity
@Comment("Pet - a domestic animal")
public class Pet extends BaseEntity {

    private static final long serialVersionUID = 6106462788935207865L;

    @Column(name = "NAME")
    @InstanceName
    @Comment("Name of the pet")
    private String name;

    @JmixProperty
    private String nick;

    @JmixProperty
    @DependsOnProperties({"name", "nick"})
    public String getDescription() {
        return "Name: " + name + ", nick: " + nick;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID")
    private Owner owner;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }
}
