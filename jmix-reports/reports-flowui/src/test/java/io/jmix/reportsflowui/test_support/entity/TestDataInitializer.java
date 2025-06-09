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

package io.jmix.reportsflowui.test_support.entity;

import io.jmix.core.DataManager;
import io.jmix.core.UnconstrainedDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
public class TestDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(TestDataInitializer.class);
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TestDataInitializer(DataManager unconstrainedDataManager) {
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    public void init() {
        if (!unconstrainedDataManager.load(Publisher.class).all().list().isEmpty()) {
            log.info("Test data already created");
            return;
        }

        log.info("Creating test data for reports");

        List<Publisher> publishers = createPublishers();
        List<GameTitle> games = createGames(publishers);
        List<UserRegistration> users = createUserRegistrations();
        List<PurchasedGame> purchases = createPurchasedGames(users, games);
        log.info("Test data created");
    }

    private List<Publisher> createPublishers() {
        Object[][] data = new Object[][]{
                {"Nintendo"},
                {"Activision"},
                {"Ubisoft"}
        };

        return Arrays.stream(data)
                .map(datum -> {
                    Publisher p = unconstrainedDataManager.create(Publisher.class);
                    p.setName((String) datum[0]);
                    return unconstrainedDataManager.save(p);
                })
                .toList();
    }

    private List<GameTitle> createGames(List<Publisher> publishers) {
        Object[][] data = new Object[][]{
                {"Tetris", LocalDate.of(1989, 1, 2), new BigDecimal(5), "Nintendo"},
                {"Mario Kart DS", LocalDate.of(2005, 2, 3), new BigDecimal(8), "Nintendo"},
                {"Modern Warfare 3", LocalDate.of(2011, 3, 4), new BigDecimal(18), "Activision"},
                {"Destiny", LocalDate.of(2014, 4, 5), new BigDecimal(25), "Activision"},
                {"Assassin's Creed", LocalDate.of(2007, 5, 6), new BigDecimal(27), "Ubisoft"}
        };

        return Arrays.stream(data)
                .map(datum -> {
                    GameTitle g = unconstrainedDataManager.create(GameTitle.class);
                    g.setName((String) datum[0]);
                    g.setReleaseDate((LocalDate) datum[1]);
                    g.setPrice((BigDecimal) datum[2]);
                    g.setPublisher(publishers.stream().filter(p -> p.getName().equals(datum[3])).findAny().orElseThrow());
                    return unconstrainedDataManager.save(g);
                })
                .toList();
    }

    private List<UserRegistration> createUserRegistrations() {
        Object[][] data = new Object[][]{
                {"Liliana", "Medrano", "lmedrano", ldt("2020-01-01 15:45")},
                {"Kenzie", "Wall", "ken466", ldt("2021-02-03 18:55")},
                {"Issac", "Shelton", "shelton", ldt("2021-03-07 18:55")},
                {"Lola", "Barber", "lola18", ldt("2020-11-25 21:03")}
        };

        return Arrays.stream(data)
                .map(datum -> {
                    UserRegistration ur = unconstrainedDataManager.create(UserRegistration.class);
                    ur.setFirstName((String) datum[0]);
                    ur.setLastName(((String) datum[1]));
                    ur.setUsername((String) datum[2]);
                    ur.setRegistrationDate((LocalDateTime) datum[3]);
                    return unconstrainedDataManager.save(ur);
                })
                .toList();
    }

    private List<PurchasedGame> createPurchasedGames(List<UserRegistration> users, List<GameTitle> games) {
        Object[][] data = new Object[][]{
                {"lmedrano", "Mario Kart DS", ldt("2025-01-15 00:00"), 9},
                {"lmedrano", "Destiny", ldt("2025-02-25 00:00"), 8},

                {"ken466", "Assassin's Creed", ldt("2025-05-17 00:00"), 5},

                {"shelton", "Mario Kart DS", ldt("2025-02-03 00:00"), 7},
                {"shelton", "Modern Warfare 3", ldt("2025-03-11 00:00"), 6},

                {"lola18", "Tetris", ldt("2025-04-12 00:00"), null},
                {"lola18", "Mario Kart DS", ldt("2025-05-12 00:00"), 8},
                {"lola18", "Assassin's Creed", ldt("2025-05-30 00:00"), 9}
        };
        return Arrays.stream(data)
                .map(datum -> {
                    PurchasedGame pg = unconstrainedDataManager.create(PurchasedGame.class);
                    pg.setUser(users.stream().filter(u -> u.getUsername().equals(datum[0])).findAny().orElseThrow());
                    pg.setGame(games.stream().filter(g -> g.getName().equals(datum[1])).findAny().orElseThrow());
                    pg.setPurchaseDate((LocalDateTime) datum[2]);
                    pg.setUserRating(((Integer) datum[3]));
                    return unconstrainedDataManager.save(pg);
                })
                .toList();
    }

    private LocalDateTime ldt(String string) {
        return LocalDateTime.parse(string, formatter);
    }
}
