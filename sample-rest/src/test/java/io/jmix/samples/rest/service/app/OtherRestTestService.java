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
public interface OtherRestTestService {

    String NAME = "jmix_OtherRestTestService";

    void testMethod();
}
