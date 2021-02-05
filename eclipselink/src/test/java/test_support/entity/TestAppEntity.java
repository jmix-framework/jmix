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

package test_support.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import test_support.entity.sec.User;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@JmixEntity
@Entity(name = "test_TestAppEntity")
@Table(name = "TEST_APP_ENTITY")
public class TestAppEntity extends BaseEntity {

    private static final long serialVersionUID = 8256929425690816623L;

    @Column(name = "NAME")
    private String name;

    @Column(name = "NUMBER")
    private String number;

    @Column(name = "APP_DATE")
    private Date appDate;

    @OneToMany(mappedBy = "appEntity")
    private List<TestAppEntityItem> items;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AUTHOR_ID")
    private User author;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<TestAppEntityItem> getItems() {
        return items;
    }

    public void setItems(List<TestAppEntityItem> items) {
        this.items = items;
    }

    @JmixProperty
    @DependsOnProperties("appDate")
    public Date getChangeDate() {
        return this.appDate;
    }

    @JmixProperty
    @DependsOnProperties({"author", "number"})
    public String getLabel() {
        return String.format("%s-%s", author != null ? author.getLogin() : "", number);
    }

    public Date getAppDate() {
        return appDate;
    }

    public void setAppDate(Date appDate) {
        this.appDate = appDate;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
