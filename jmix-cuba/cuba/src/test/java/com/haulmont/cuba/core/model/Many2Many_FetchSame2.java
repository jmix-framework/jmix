/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.core.model;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.List;

@NamePattern("%s|name")
@Table(name = "TEST_MANY2_MANY_FETCH_SAME2")
@Entity(name = "test$Many2Many_FetchSame2")
@JmixEntity
public class Many2Many_FetchSame2 extends StandardEntity {
    private static final long serialVersionUID = -8287667692509037970L;

    @Column(name = "NAME")
    protected String name;

    @JoinTable(name = "TEST_MANY2_MANY_FETCH_SAME1_MANY2_MANY_FETCH_SAME2_LINK",
            joinColumns = @JoinColumn(name = "MANY2_MANY__FETCH_SAME2_ID"),
            inverseJoinColumns = @JoinColumn(name = "MANY2_MANY__FETCH_SAME1_ID"))
    @ManyToMany
    protected List<Many2Many_FetchSame1> many1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANY3_ID")
    protected Many2Many_FetchSame3 many3;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANY_TO_ONE1_ID")
    protected Many2Many_FetchSame1 manyToOne1;

    public void setMany1(List<Many2Many_FetchSame1> many1) {
        this.many1 = many1;
    }

    public List<Many2Many_FetchSame1> getMany1() {
        return many1;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Many2Many_FetchSame3 getMany3() {
        return many3;
    }

    public void setMany3(Many2Many_FetchSame3 many3) {
        this.many3 = many3;
    }

    public Many2Many_FetchSame1 getManyToOne1() {
        return manyToOne1;
    }

    public void setManyToOne1(Many2Many_FetchSame1 manyToOne1) {
        this.manyToOne1 = manyToOne1;
    }
}
