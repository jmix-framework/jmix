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

package test_support.entity;

import java.util.Objects;

public class TestPhone {

    public final String countryCode;
    public final String phoneNumber;

    public TestPhone(String countryCode, String phoneNumber) {
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return countryCode + " " + phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestPhone testPhone = (TestPhone) o;
        return Objects.equals(countryCode, testPhone.countryCode) &&
                Objects.equals(phoneNumber, testPhone.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, phoneNumber);
    }
}
