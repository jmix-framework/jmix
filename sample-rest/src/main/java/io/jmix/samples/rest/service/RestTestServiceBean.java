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

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.validation.CustomValidationException;
import io.jmix.samples.rest.controller.CustomHttpClientErrorException;
import io.jmix.samples.rest.entity.ModelEntity;
import io.jmix.samples.rest.entity.driver.Car;
import io.jmix.samples.rest.entity.driver.NotPersistentStringIdEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service(RestTestService.NAME)
public class RestTestServiceBean implements RestTestService {

    @Autowired
    protected Metadata metadata;

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
        return number1 + Integer.parseInt(number2);
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
    public String testJavaTimeParam(LocalDate localDate, LocalDateTime localDateTime, LocalTime localTime, OffsetDateTime offsetDateTime, OffsetTime offsetTime, Time time) {
        StringBuilder builder = new StringBuilder(localDate.toString()).append(",");
        builder.append(localDateTime.toString()).append(",");
        builder.append(localTime.toString()).append(",");
        builder.append(offsetDateTime.toString()).append(",");
        builder.append(offsetTime.toString()).append(",");
        builder.append(time.toString());
        return builder.toString();
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
    public int validatedMethod(String code) {
        return 0;
    }

    @Override
    public String validatedMethodResult(String code) {
        if ("100".equals(code)) {
            throw new CustomValidationException("Epic fail!");
        }
        return null;
    }

    @Override
    public ModelEntity notPersistedEntity() {
        ModelEntity model = metadata.create(ModelEntity.class);
        model.setTitle("model 1");
        return model;
    }

    @Override
    public List<String> methodWithPrimitiveListArguments(List<String> stringList, int[] intArray, int intArgument) {
        return stringList;
    }

    @Override
    public List<PojoWithNestedEntity> getPojosWithNestedEntity() {
        List<Car> cars = dataManager.load(Car.class)
                .query("select c from ref_Car c order by c.vin")
                .fetchPlan(FetchPlan.LOCAL)
                .list();
        final int[] counter = {1};
        return cars.stream()
                .map(car -> new PojoWithNestedEntity(car, counter[0]++))
                .collect(Collectors.toList());
    }

    @Override
    public List<PojoWithNestedEntity> getPojosWithNestedEntityWithView() {
        List<Car> cars = dataManager.load(Car.class)
                .query("select c from ref_Car c order by c.vin")
                .fetchPlan("car-with-colour")
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

    @Override
    public Map<String, String> methodWithOptionalArgs(String arg1, String arg2, String arg3) {
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("arg1", arg1);
        stringMap.put("arg2", arg2);
        stringMap.put("arg3", arg3);
        return stringMap;
    }

    @Override
    public String overloadedMethodWithOptionalArgs(String arg1) {
        return "one arg";
    }

    @Override
    public String overloadedMethodWithOptionalArgs(String arg1, String arg2) {
        return "two args";
    }
}
