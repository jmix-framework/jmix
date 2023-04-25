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

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "EXTTEST_WAYBILL")
@Entity(name = "exttest_Waybill")
public class Waybill {
    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "CREATED_DATE")
    @CreatedDate
    private Date createdDate;

    @ManyToOne
    @JoinColumn(name = "BUS_ID", insertable = false, updatable = false, nullable = false)
    protected Bus bus;

    @OneToOne
    @JoinColumn(name = "DOC_ID")
    protected Doc doc;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "EXTTEST_WAYBILL_PLACE_LINK",
            joinColumns = @JoinColumn(name = "WAYBILL_ID"),
            inverseJoinColumns = @JoinColumn(name = "PLACE_ID", nullable = false, insertable = false, updatable = false))
    protected List<Place> places;
    @JoinTable(name = "JMIX-ALL_WAYBILL_SIGNER_LINK",
            joinColumns = @JoinColumn(name = "WAYBILL_ID"),
            inverseJoinColumns = @JoinColumn(name = "SIGNER_ID"))
    @ManyToMany
    private List<Station> stations;

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    public Doc getDoc() {
        return doc;
    }

    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}