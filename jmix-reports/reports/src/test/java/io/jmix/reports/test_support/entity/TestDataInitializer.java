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

import io.jmix.core.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.test.context.event.BeforeTestClassEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
public class TestDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(TestDataInitializer.class);
    private final DataManager dataManager;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TestDataInitializer(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    // todo not working @EventListener
    public void handleBeforeTestClassEvent(BeforeTestClassEvent event) {
        if (!dataManager.load(Publisher.class).all().list().isEmpty()) {
            log.info("Test data already created");
            return;
        }

        log.info("Creating test data for reports");

        List<Publisher> publishers = createPublishers();
        List<GameTitle> games = createGames(publishers);
        List<UserRegistration> users = createUserRegistrations();
        List<Achievement> achievements = createAchievements(games);
        List<PurchasedGame> purchases = createPurchasedGames(users, games);
        List<UserAchievement> userAchievements = createUserAchievements(purchases, achievements);
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
                    Publisher p = dataManager.create(Publisher.class);
                    p.setName((String) datum[0]);
                    return dataManager.save(p);
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
                    GameTitle g = dataManager.create(GameTitle.class);
                    g.setName((String) datum[0]);
                    g.setReleaseDate((LocalDate) datum[1]);
                    g.setPrice((BigDecimal) datum[2]);
                    g.setPublisher(publishers.stream().filter(p -> p.getName().equals(datum[3])).findAny().orElseThrow());
                    return dataManager.save(g);
                })
                .toList();
    }

    private List<Achievement> createAchievements(List<GameTitle> games) {
        Object[][] data = new Object[][]{
                {"Launch the game", "Tetris"},
                {"Alive after 1000 iterations", "Tetris"},
                {"Launch the game", "Mario Kart DS"},
                {"Survive maximum speed", "Mario Kart DS"},
                {"Win 1000 enemies", "Modern Warfare 3"},
                {"Make 10000 shots", "Modern Warfare 3"},
                {"Defend position alone", "Destiny"},
                {"Fast walkthrough", "Destiny"},
                {"1000 target hit", "Destiny"},
                {"Don't fall in 3 hours", "Assassin's Creed"},
                {"Steal 10000 coins", "Assassin's Creed"}
        };

        return Arrays.stream(data)
                .map(datum -> {
                    Achievement a = dataManager.create(Achievement.class);
                    a.setName((String) datum[0]);
                    a.setGame(games.stream().filter(g -> g.getName().equals(datum[1])).findAny().orElseThrow());
                    return dataManager.save(a);
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
                    UserRegistration ur = dataManager.create(UserRegistration.class);
                    ur.setFirstName((String) datum[0]);
                    ur.setLastName(((String) datum[1]));
                    ur.setUsername((String) datum[2]);
                    ur.setRegistrationDate((LocalDateTime) datum[3]);
                    return dataManager.save(ur);
                })
                .toList();
    }

    private List<PurchasedGame> createPurchasedGames(List<UserRegistration> users, List<GameTitle> games) {
        Object[][] data = new Object[][]{
                {"lmedrano", "Mario Kart DS", ldt("2025-01-15 00:00")},
                {"lmedrano", "Destiny", ldt("2025-02-25 00:00")},

                {"ken466", "Assassin's Creed", ldt("2025-05-17 00:00")},

                {"shelton", "Mario Kart DS", ldt("2025-02-03 00:00")},
                {"shelton", "Modern Warfare 3", ldt("2025-03-11 00:00")},

                {"lola18", "Tetris", ldt("2025-04-12 00:00")},
                {"lola18", "Mario Kart DS", ldt("2025-05-12 00:00")}
        };
        return Arrays.stream(data)
                .map(datum -> {
                    PurchasedGame pg = dataManager.create(PurchasedGame.class);
                    pg.setUser(users.stream().filter(u -> u.getUsername().equals(datum[0])).findAny().orElseThrow());
                    pg.setGame(games.stream().filter(g -> g.getName().equals(datum[1])).findAny().orElseThrow());
                    pg.setPurchaseDate((LocalDateTime) datum[2]);
                    return dataManager.save(pg);
                })
                .toList();
    }

    private List<UserAchievement> createUserAchievements(List<PurchasedGame> purchases, List<Achievement> achievements) {
        Object[][] data = new Object[][]{
                {"lmedrano", "Mario Kart DS", "Launch the game", ldt("2025-01-15 18:45")},

                {"lmedrano", "Destiny", "Defend position alone", ldt("2025-02-25 15:13")},
                {"lmedrano", "Destiny", "1000 target hit", ldt("2025-02-25 23:25")},

                {"ken466", "Assassin's Creed", "Don't fall in 3 hours", ldt("2025-05-18 11:05")},

                {"shelton", "Mario Kart DS", "Launch the game", ldt("2025-02-03 15:10")},
                {"shelton", "Mario Kart DS", "Survive maximum speed", ldt("2025-02-03 16:30")},

                {"lola18", "Tetris", "Launch the game", ldt("2025-04-12 20:05")},
                {"lola18", "Tetris", "Alive after 1000 iterations", ldt("2025-04-12 23:45")},

                {"lola18", "Mario Kart DS", "Survive maximum speed", ldt("2025-05-12 23:55")},
                {"lola18", "Mario Kart DS", "Launch the game", ldt("2025-05-13 02:15")},
        };
        return Arrays.stream(data)
                .map(datum -> {
                    UserAchievement ua = dataManager.create(UserAchievement.class);
                    ua.setPurchasedGame(purchases.stream()
                            .filter(pg -> pg.getUser().getUsername().equals(datum[0]) && pg.getGame().getName().equals(datum[1]))
                            .findAny()
                            .orElseThrow());
                    ua.setAchievement(achievements.stream()
                            .filter(a -> a.getName().equals(datum[2]) && a.getGame().getName().equals(datum[1]))
                            .findAny()
                            .orElseThrow());
                    ua.setDate((LocalDateTime) datum[3]);
                    return dataManager.save(ua);
                })
                .toList();
    }

    private LocalDateTime ldt(String string) {
        return LocalDateTime.parse(string, formatter);
    }
}
