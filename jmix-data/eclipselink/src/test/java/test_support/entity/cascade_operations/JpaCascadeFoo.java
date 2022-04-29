/*
 * Copyright 2022 Haulmont.
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

package test_support.entity.cascade_operations;

import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity(name = "test$JpaCascadeFoo")
@JmixEntity
@Table(name = "TEST_JPA_CASCADE_FOO")
public class JpaCascadeFoo extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "BAR_ID")
    private JpaCascadeBar bar;

    @OneToMany(mappedBy = "foo", cascade = CascadeType.ALL)
    private List<JpaCascadeItem> items;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "BAR_P_ID")
    private JpaCascadeBar barP;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "BAR_M_ID")
    private JpaCascadeBar barM;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "BAR_DR_ID")
    private JpaCascadeBar barDR;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinTable(name = "BAR_FOO_LINK",
            joinColumns = @JoinColumn(name = "FOO_ID"),
            inverseJoinColumns = @JoinColumn(name = "BAR_ID"))
    private Set<JpaCascadeBar> barR;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BAR_NONCASCADE_ID")
    private JpaCascadeBar nonCascadeBar;

    @Embedded
    private JpaCascadeEmbeddable embeddable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JpaCascadeBar getBar() {
        return bar;
    }

    public void setBar(JpaCascadeBar bar) {
        this.bar = bar;
    }

    public List<JpaCascadeItem> getItems() {
        return items;
    }

    public void setItems(List<JpaCascadeItem> items) {
        this.items = items;
    }

    public JpaCascadeBar getBarP() {
        return barP;
    }

    public void setBarP(JpaCascadeBar barP) {
        this.barP = barP;
    }

    public JpaCascadeBar getBarM() {
        return barM;
    }

    public void setBarM(JpaCascadeBar barM) {
        this.barM = barM;
    }

    public JpaCascadeBar getBarDR() {
        return barDR;
    }

    public void setBarDR(JpaCascadeBar barDR) {
        this.barDR = barDR;
    }

    public Set<JpaCascadeBar> getBarR() {
        return barR;
    }

    public void setBarR(Set<JpaCascadeBar> barR) {
        this.barR = barR;
    }

    public JpaCascadeBar getNonCascadeBar() {
        return nonCascadeBar;
    }

    public void setNonCascadeBar(JpaCascadeBar nonCascadeBar) {
        this.nonCascadeBar = nonCascadeBar;
    }

    public JpaCascadeEmbeddable getEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(JpaCascadeEmbeddable embeddable) {
        this.embeddable = embeddable;
    }
}
