/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.service.app;

import io.jmix.samples.rest.entity.driver.Car;
import io.jmix.samples.rest.entity.driver.NotPersistentStringIdEntity;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service is used in functional tests
 */
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

    BigDecimal testBigDecimalParam(BigDecimal param);

    List<PojoWithNestedEntity> getPojosWithNestedEntity();

    TestPojo getPojo() throws Exception;

    List<TestPojo> getPojoList() throws ParseException;

    int methodWithPojoParameter(TestPojo pojo);

    int methodWithPojoCollectionParameter(List<TestPojo> pojoCollection);

    String methodWithPojoCollectionParameter2(List<TestPojo> pojoCollection);

    @Validated
    int validatedMethod(@Pattern(regexp = "\\d+") String code);

    @Validated
    @NotNull
    String validatedMethodResult(@Pattern(regexp = "\\d+") String code);

//    Stencil notPersistedEntity();

    List<String> methodWithPrimitiveListArguments(List<String> stringList, int[] intArray, int intArgument);

    List<PojoWithNestedEntity> getPojosWithNestedEntityWithView();

    int methodWithListOfMapParam(List<Map<String, BigDecimal>> param);

    List<Map<String, Object>> methodReturnsListOfMap();

//    TransientDriver getTransientDriver();

    NotPersistentStringIdEntity getNotPersistentStringIdEntity();
}
