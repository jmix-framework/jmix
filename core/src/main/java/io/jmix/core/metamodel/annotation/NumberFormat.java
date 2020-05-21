/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.metamodel.annotation;

import java.lang.annotation.*;

/**
 * Specifies a format {@link #pattern()} and optional {@link #decimalSeparator()} and {@link #groupingSeparator()} for
 * an entity attribute of the {@link Number} type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Inherited
public @interface NumberFormat {

	/**
	 * Pattern as described for {@link java.text.DecimalFormat}.
	 */
	String pattern();

	/**
	 * Decimal separator. If not specified, will be obtained from {@link io.jmix.core.metamodel.datatype.FormatStrings}
     * for locale-dependent formatting, or from server default locale for locale-independent formatting.
	 */
	String decimalSeparator() default "";

    /**
   	 * Grouping separator. If not specified, will be obtained from {@link io.jmix.core.metamodel.datatype.FormatStrings}
     * for locale-dependent formatting, or from server default locale for locale-independent formatting.
   	 */
	String groupingSeparator() default "";
}
