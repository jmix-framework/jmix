/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.service.app;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.validation.CustomValidationException;
import io.jmix.samples.rest.entity.driver.Car;
import io.jmix.samples.rest.entity.driver.NotPersistentStringIdEntity;
import io.jmix.samples.rest.exception.CustomHttpClientErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service(RestTestService.NAME)
public class RestTestServiceBean implements RestTestService {

    @Autowired
    protected Metadata metadata;

//    @Autowired
//    protected Persistence persistence;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Override
    public void emptyMethod() {
    }

    @Override
    public void notPermittedMethod() {
    }

    @Override
    public Integer sum(int number1, String number2) {
        return number1 + Integer.valueOf(number2);
    }

    @Override
    public void methodWithCustomException() {
        throw new CustomHttpClientErrorException(HttpStatus.I_AM_A_TEAPOT, "Server is not a coffee machine");
    }

    @Override
    public void methodWithException() {
        throw new RuntimeException("Error!");
    }

    @Override
    public String overloadedMethod(int param) {
        return "int";
    }

    @Override
    public String overloadedMethod(String param) {
        return "String";
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Override
    public Car findCar(UUID carId, String viewName) {
        return dataManager.load(Car.class)
                .query("select c from ref_Car c where c.id = :carId")
                .parameter("carId", carId)
                .fetchPlan(viewName)
                .one();
    }

    @Override
    public List<Car> findAllCars(String viewName) {
        return dataManager.load(Car.class)
                .query("select c from ref_Car c")
                .fetchPlan(viewName)
                .list();
    }

    @Override
    public Car updateCarVin(Car car, String vin) {
        car.setVin(vin);
        return car;
    }

    @Override
    public String concatCarVins(List<Car> cars) {
        StringBuilder sb = new StringBuilder();
        cars.forEach(car -> sb.append(car.getVin()));
        return sb.toString();
    }

    @Override
    public List<Car> updateCarVins(List<Car> cars, String vin) {
        for (Car car : cars) {
            car.setVin(vin);
        }
        return cars;
    }

    @Override
    public String testNullParam(UUID uuid) {
        return uuid == null ? "true" : "false";
    }

    @Override
    public Date testDateParam(Date param) {
        return param;
    }

    @Override
    public BigDecimal testBigDecimalParam(BigDecimal param) {
        return param;
    }

    @Override
    public TestPojo getPojo() throws Exception {
        TestPojo.TestNestedPojo testNestedPojo = new TestPojo.TestNestedPojo();
        testNestedPojo.setNestedField(2);
        TestPojo testPojo = new TestPojo();
        testPojo.setField1("field1 value");
        testPojo.setNestedPojo(testNestedPojo);
        testPojo.setDateField(sdf.parse("15.01.2017 17:56:00"));
        return testPojo;
    }

    @Override
    public List<TestPojo> getPojoList() throws ParseException {
        TestPojo pojo1 = new TestPojo();
        pojo1.setField1("pojo1");

        TestPojo.TestNestedPojo testNestedPojo = new TestPojo.TestNestedPojo();
        testNestedPojo.setNestedField(1);
        pojo1.setNestedPojo(testNestedPojo);
        pojo1.setDateField(sdf.parse("15.01.2017 17:56:00"));

        TestPojo pojo2 = new TestPojo();
        pojo2.setField1("pojo2");

        return Arrays.asList(pojo1, pojo2);
    }

    @Override
    public int methodWithPojoParameter(TestPojo pojo) {
        return pojo.getNestedPojo().getNestedField();
    }

    @Override
    public int methodWithPojoCollectionParameter(List<TestPojo> pojoCollection) {
        return pojoCollection.size();
    }

    @Override
    public String methodWithPojoCollectionParameter2(List<TestPojo> pojoCollection) {
        TestPojo testPojo = pojoCollection.get(0);
        return testPojo.getField1();
    }

    @Override
    public int validatedMethod(@Pattern(regexp = "\\d+") String code) {
        return 0;
    }

    @Nonnull
    @Override
    public String validatedMethodResult(@Pattern(regexp = "\\d+") String code) {
        if ("100".equals(code)) {
            throw new CustomValidationException("Epic fail!");
        }
        return null;
    }

//    @Override
//    public Stencil notPersistedEntity() {
//        Stencil stencil = metadata.create(Stencil.class);
//        stencil.setTitle("stencil 1");
//        return stencil;
//    }

    @Override
    public List<String> methodWithPrimitiveListArguments(List<String> stringList, int[] intArray, int intArgument) {
        return stringList;
    }

    @Override
    public List<PojoWithNestedEntity> getPojosWithNestedEntity() {
        List<Car> cars = dataManager.load(Car.class)
                .fetchPlan(FetchPlan.LOCAL)
                .query("select c from ref_Car c order by c.vin")
                .list();
        final int[] counter = {1};
        return cars.stream()
                .map(car -> new PojoWithNestedEntity(car, counter[0]++))
                .collect(Collectors.toList());
    }

    @Override
    public List<PojoWithNestedEntity> getPojosWithNestedEntityWithView() {
        List<Car> cars = dataManager.load(Car.class)
                .fetchPlan("car-with-colour")
                .query("select c from ref_Car c order by c.vin")
                .list();
        final int[] counter = {1};
        return cars.stream()
                .map(car -> new PojoWithNestedEntity(car, counter[0]++))
                .collect(Collectors.toList());
    }

    @Override
    public int methodWithListOfMapParam(List<Map<String, BigDecimal>> param) {
        return param.stream()
                .mapToInt(Map::size)
                .sum();
    }

    @Override
    public List<Map<String, Object>> methodReturnsListOfMap() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("key1", 1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("key2", 2);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map1);
        list.add(map2);
        return list;
    }

    //    @Override
//    public TransientDriver getTransientDriver() {
//        TransientDriver transientDriver = metadata.create(TransientDriver.class);
//        transientDriver.setName("Bob");
//        transientDriver.setGroupName("Group1");
//        return transientDriver;
//    }
//
    @Override
    public NotPersistentStringIdEntity getNotPersistentStringIdEntity() {
        NotPersistentStringIdEntity notPersistentStringIdEntity = metadata.create(NotPersistentStringIdEntity.class);
        EntityValues.setId(notPersistentStringIdEntity, "1");
        notPersistentStringIdEntity.setName("Bob");
        return notPersistentStringIdEntity;
    }
}
