/*
 * Copyright (C) 2015 Ferid Cafer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ferid.app.cleaner.enums;

/**
 * Created by Ferid Cafer on 11/30/2015.
 */
public enum SortingType {
    ALPHABET(0),
    SIZE(1);

    private final int value;

    private SortingType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }

    public SortingType next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
