/*
 * Copyright 2021 Haulmont.
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

package test_support.custom.service;

import io.jmix.core.Metadata;
import io.jmix.graphql.modifier.GraphQLRemoveEntityDataFetcher;
import io.jmix.graphql.modifier.GraphQLRemoveEntityDataFetcherContext;
import io.jmix.graphql.modifier.GraphQLUpsertEntityDataFetcher;
import io.jmix.graphql.modifier.GraphQLUpsertEntityDataFetcherContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import test_support.entity.Car;

import java.math.BigDecimal;

@Profile("custom")
@Component("Test_CarModifier")
public class CarModifier implements GraphQLRemoveEntityDataFetcher<Car>, GraphQLUpsertEntityDataFetcher<Car> {

    @Autowired
    Metadata metadata;

    private static Logger log = LoggerFactory.getLogger(CarModifier.class);

    @Override
    public Car importEntities(GraphQLUpsertEntityDataFetcherContext<Car> context) {
        Car car = context.getEntities().get(0);
        car.setPrice(BigDecimal.valueOf(10));
        return car;
    }

    @Override
    public void deleteEntity(GraphQLRemoveEntityDataFetcherContext<Car> graphQLRemoveEntityDataFetcherContext) {
        log.warn("Delete entity with id " + graphQLRemoveEntityDataFetcherContext.getId());
    }
}
