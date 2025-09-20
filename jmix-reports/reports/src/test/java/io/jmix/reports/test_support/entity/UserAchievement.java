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

package io.jmix.reports.test_support.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Entity
@Table(name = "USER_ACHIEVEMENT", indexes = {
        @Index(name = "IDX_USER_ACHIEVEMENT_ACHIEVEMENT", columnList = "ACHIEVEMENT_ID"),
        @Index(name = "IDX_USER_ACHIEVEMENT_PURCHASED_GAME", columnList = "PURCHASED_GAME_ID")
})
public class UserAchievement {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @NotNull
    @JoinColumn(name = "PURCHASED_GAME_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PurchasedGame purchasedGame;

    @NotNull
    @JoinColumn(name = "ACHIEVEMENT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Achievement achievement;

    @NotNull
    @Column(name = "DATE_", nullable = false)
    private LocalDateTime date;

    public PurchasedGame getPurchasedGame() {
        return purchasedGame;
    }

    public void setPurchasedGame(PurchasedGame purchasedGame) {
        this.purchasedGame = purchasedGame;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Achievement getAchievement() {
        return achievement;
    }

    public void setAchievement(Achievement achievement) {
        this.achievement = achievement;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}
