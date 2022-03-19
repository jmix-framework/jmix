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

package io.jmix.ui.component;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.Nullable;
import java.util.Arrays;

public class KeyCombination {
    private static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];

    private final Key key;
    private final Modifier[] modifiers;

    /**
     * Creates a new <code>KeyCombination</code> instance from a string representation.
     *
     * @param keyString string of type "Modifiers-Key", e.g. "Alt-N". Case-insensitive.
     * @return new instance
     */
    @Nullable
    public static KeyCombination create(@Nullable String keyString) {
        if (keyString == null) {
            return null;
        }
        keyString = keyString.toUpperCase();

        Key key;
        Modifier[] modifiers = null;

        if (keyString.contains("-")) {
            String[] keys = keyString.split("-", -1);

            int modifiersCnt = keys.length;

            key = Key.valueOf(keys[modifiersCnt - 1]);
            --modifiersCnt;
            modifiers = new Modifier[modifiersCnt];
            for (int i = 0; i < modifiersCnt; i++) {
                modifiers[i] = Modifier.valueOf(keys[i]);
            }
        } else {
            key = Key.valueOf(keyString);

        }
        return new KeyCombination(key, modifiers);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        if (modifiers != null) {
            for (Modifier modifier : modifiers) {
                if (sb.length() > 0) {
                    sb.append("+");
                }
                sb.append(modifier.getName());
            }
        }
        if (sb.length() > 0) {
            sb.append("+");
        }
        if (key != null) {
            sb.append(key.getName());
        }
        return sb.toString();
    }

    public KeyCombination(@Nullable Key key, @Nullable Modifier... modifiers) {
        if (key == null && modifiers == null) {
            throw new IllegalArgumentException("Combination is empty");
        }
        this.key = key;
        this.modifiers = modifiers;
    }

    @Nullable
    public Key getKey() {
        return key;
    }

    @Nullable
    public Modifier[] getModifiers() {
        return modifiers;
    }

    public enum Key {
        ENTER(13, '\n', "Enter"),
        ESCAPE(27, "Esc"),
        PAGE_UP(33, "Page Up"),
        PAGE_DOWN(34, "Page Dn"),
        TAB(9, '\t', "Tab"),
        ARROW_LEFT(37, "Left"),
        ARROW_UP(38, "Up"),
        ARROW_RIGHT(39, "Right"),
        ARROW_DOWN(40, "Down"),
        BACKSPACE(8, "Backspace"),
        BACKSLASH(220, 0x5C, "\\"),
        DELETE(46, 0x7F, "Del"),
        INSERT(45, 0x9B, "Ins"),
        END(35, "End"),
        HOME(36, "Home"),
        SPACEBAR(32, "Space"),

        PLUS(107, "Num +"),
        MINUS(109, "Num -"),
        DIVIDE(111, "Num /"),
        MULTIPLY(106, "Num *"),

        F1(112),
        F2(113),
        F3(114),
        F4(115),
        F5(116),
        F6(117),
        F7(118),
        F8(119),
        F9(120),
        F10(121),
        F11(122),
        F12(123),

        A(65),
        B(66),
        C(67),
        D(68),
        E(69),
        F(70),
        G(71),
        H(72),
        I(73),
        J(74),
        K(75),
        L(76),
        M(77),
        N(78),
        O(79),
        P(80),
        Q(81),
        R(82),
        S(83),
        T(84),
        U(85),
        V(86),
        W(87),
        X(88),
        Y(89),
        Z(90),

        KEY0(48, "0"),
        KEY1(49, "1"),
        KEY2(50, "2"),
        KEY3(51, "3"),
        KEY4(52, "4"),
        KEY5(53, "5"),
        KEY6(54, "6"),
        KEY7(55, "7"),
        KEY8(56, "8"),
        KEY9(57, "9"),

        NUM0(96, "Num 0"),
        NUM1(97, "Num 1"),
        NUM2(98, "Num 2"),
        NUM3(99, "Num 3"),
        NUM4(100, "Num 4"),
        NUM5(101, "Num 5"),
        NUM6(102, "Num 6"),
        NUM7(103, "Num 7"),
        NUM8(104, "Num 8"),
        NUM9(105, "Num 9");

        private int code;
        private int virtualKey;
        private String name;

        Key(int code) {
            this(code, code);
        }

        Key(int code, int virtualKey) {
            this.code = code;
            this.virtualKey = virtualKey;
        }

        Key(int code, String name) {
            this(code);
            this.name = name;
        }

        Key(int code, int virtualKey, String name) {
            this(code, virtualKey);
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public int getVirtualKey() {
            return virtualKey;
        }

        public String getName() {
            return name != null ? name : this.name();
        }
    }

    public enum Modifier {
        SHIFT(16, "Shift"),
        CTRL(17, "Ctrl"),
        ALT(18, "Alt");

        private int code;

        private String name;

        Modifier(int code) {
            this.code = code;
        }

        Modifier(int code, String name) {
            this(code);
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name != null ? name : this.name();
        }

        public static int[] codes(@Nullable Modifier... modifiers) {
            if (modifiers == null) {
                return new int[0];
            }
            int[] codes = new int[modifiers.length];
            for (int i = 0; i < modifiers.length; i++) {
                codes[i] = modifiers[i].code;
            }
            return codes;
        }
    }

    @Nullable
    public static int[] getShortcutModifiers(@Nullable KeyCombination.Modifier[] modifiers) {
        if (modifiers == null) {
            return null;
        }
        int[] res = new int[modifiers.length];
        for (int i = 0; i < modifiers.length; i++) {
            res[i] = modifiers[i].getCode();
        }
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        KeyCombination other = (KeyCombination) obj;

        KeyCombination.Modifier[] modifiers = this.modifiers != null ? this.modifiers : EMPTY_MODIFIERS;
        KeyCombination.Modifier[] otherModifiers = other.modifiers != null ? other.modifiers : EMPTY_MODIFIERS;

        return this.key == other.key && Arrays.equals(modifiers, otherModifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key, modifiers);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", key)
                .add("modifiers", modifiers)
                .toString();
    }
}