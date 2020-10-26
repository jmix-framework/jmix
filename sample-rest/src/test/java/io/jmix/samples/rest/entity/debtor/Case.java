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

package io.jmix.samples.rest.entity.debtor;


import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "DEBT_CASE")
@Entity(name = "debt$Case")
public class Case extends StandardEntity {
    @Column(name = "TEST1", length = 50)
    protected String test1;

    @Column(name = "TEST2", length = 50)
    protected String test2;

    @Column(name = "TEST3", length = 50)
    protected String test3;

    @Column(name = "TEST4", length = 50)
    protected String test4;

    @Column(name = "TEST5", length = 50)
    protected String test5;

    @Column(name = "TEST6", length = 50)
    protected String test6;

    @Column(name = "TEST7", length = 50)
    protected String test7;

    @Column(name = "TEST8", length = 50)
    protected String test8;

    @Column(name = "TEST9", length = 50)
    protected String test9;

    @Column(name = "TEST10", length = 50)
    protected String test10;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DEBTOR_ID")
    protected Debtor debtor;

    @Transient
    @ModelProperty
    @DependsOnProperties({"test1", "test2"})
    protected String nonPersistent1;

    @Transient
    @ModelProperty
    protected List<Debtor> doubleDebtor;

    private static final long serialVersionUID = 5403048066474211978L;

    public void setTest1(String test1) {
        this.test1 = test1;
    }

    public String getTest1() {
        return test1;
    }

    public void setTest2(String test2) {
        this.test2 = test2;
    }

    public String getTest2() {
        return test2;
    }

    public void setTest3(String test3) {
        this.test3 = test3;
    }

    public String getTest3() {
        return test3;
    }

    public void setTest4(String test4) {
        this.test4 = test4;
    }

    public String getTest4() {
        return test4;
    }

    public void setTest5(String test5) {
        this.test5 = test5;
    }

    public String getTest5() {
        return test5;
    }

    public void setTest6(String test6) {
        this.test6 = test6;
    }

    public String getTest6() {
        return test6;
    }

    public void setTest7(String test7) {
        this.test7 = test7;
    }

    public String getTest7() {
        return test7;
    }

    public void setTest8(String test8) {
        this.test8 = test8;
    }

    public String getTest8() {
        return test8;
    }

    public void setTest9(String test9) {
        this.test9 = test9;
    }

    public String getTest9() {
        return test9;
    }

    public void setTest10(String test10) {
        this.test10 = test10;
    }

    public String getTest10() {
        return test10;
    }

    public void setDebtor(Debtor debtor) {
        this.debtor = debtor;
    }

    public Debtor getDebtor() {
        return debtor;
    }

    public String getNonPersistent1() {
        return nonPersistent1;
    }

    public void setNonPersistent1(String nonPersistent1) {
        this.nonPersistent1 = nonPersistent1;
    }

//    @MetaProperty(related = {"test3", "test4"})
//    public String getNonPersistent2() {
//        return test3 + " " + test4;
//    }
//
//    @MetaProperty(related = "debtor")
//    public String getNonPersistent3() {
//        return debtor == null ? null : debtor.getTitle();
//    }
//
//    @MetaProperty(related = "debtor")
//    public Debtor getDebtorFake() {
//        return debtor;
//    }
//
//    @MetaProperty
//    public TransientDriver getTransientDriver() {
//        return new TransientDriver();
//    }
//
//    public void setTransientDriver(TransientDriver transientDriver) {
//
//    }

    public List<Debtor> getDoubleDebtor() {
        List<Debtor> result = new ArrayList<>();
        result.add(debtor);
        result.add(debtor);
        return result;
    }
}