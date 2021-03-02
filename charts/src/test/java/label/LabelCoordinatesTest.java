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

package label;

import io.jmix.charts.model.label.Label;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class LabelCoordinatesTest {

    private Label label = new Label();

    @Test
    public void testSetNull() {
        label.setX(null);
        assertNull(label.getX());
    }

    @Test
    public void testSetIncorrectNumber() {
        assertThrows(IllegalArgumentException.class, () -> label.setX("10x13"));
    }

    @Test
    public void testSetCorrectNumber() {
        String value = "200.5";
        label.setX(value);
        assertEquals(value, label.getX());
    }

    @Test
    public void testSetIncorrectNumberBeforePercentage() {
        assertThrows(IllegalArgumentException.class, () -> label.setX("10px13%%"));
    }

    @Test
    public void testSetCorrectNumberBeforePercentage() {
        String value = "6.0%";
        label.setX(value);
        assertEquals(value, label.getX());
    }
}
