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

package io.jmix.samples.rest.service;

import io.jmix.samples.rest.entity.ModelEntity;
import io.jmix.samples.rest.entity.driver.Car;
import io.jmix.samples.rest.entity.driver.NotPersistentStringIdEntity;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.ParseException;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service is used in functional tests
 */
@Validated
public interface RestTestService {

    String NAME = "jmix_RestTestService";

    void emptyMethod();

    void notPermittedMethod();

    Integer sum(int number1, String number2);

    void methodWithCustomException();

    void methodWithException();

    String overloadedMethod(int param);

    String overloadedMethod(String param);

    Car findCar(UUID carId, String viewName);

    List<Car> findAllCars(String viewName);

    Car updateCarVin(Car car, String vin);

    String concatCarVins(List<Car> cars);

    List<Car> updateCarVins(List<Car> cars, String vin);

    String testNullParam(UUID uuid);

    Date testDateParam(Date param);

    String testJavaTimeParam(LocalDate localDate, LocalDateTime localDateTime, LocalTime localTime, OffsetDateTime offsetDateTime, OffsetTime offsetTime, Time time);

    BigDecimal testBigDecimalParam(BigDecimal param);

    List<PojoWithNestedEntity> getPojosWithNestedEntity();

    TestPojo getPojo() throws Exception;

    List<TestPojo> getPojoList() throws ParseException;

    int methodWithPojoParameter(TestPojo pojo);

    int methodWithPojoCollectionParameter(List<TestPojo> pojoCollection);

    String methodWithPojoCollectionParameter2(List<TestPojo> pojoCollection);

    int validatedMethod(@Pattern(regexp = "\\d+") String code);

    @NotNull
    String validatedMethodResult(@Pattern(regexp = "\\d+") String code);

    ModelEntity notPersistedEntity();

    List<String> methodWithPrimitiveListArguments(List<String> stringList, int[] intArray, int intArgument);

    List<PojoWithNestedEntity> getPojosWithNestedEntityWithView();

    int methodWithListOfMapParam(List<Map<String, BigDecimal>> param);

    List<Map<String, Object>> methodReturnsListOfMap();

    NotPersistentStringIdEntity getNotPersistentStringIdEntity();

    Map<String, String> methodWithOptionalArgs(String arg1, String arg2, String arg3);

    String overloadedMethodWithOptionalArgs(String arg1);

    String overloadedMethodWithOptionalArgs(String arg1, String arg2);
}
