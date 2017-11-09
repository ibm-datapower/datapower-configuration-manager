/**
 * Copyright 2014 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
 package com.urbancode.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

final class GsonToObject {

    //******************************************************************************************************************
    // CLASS
    //******************************************************************************************************************
    final static private BigInteger LONG_MAX_BIGINT = BigInteger.valueOf(Long.MAX_VALUE);
    final static private BigInteger LONG_MIN_BIGINT = BigInteger.valueOf(Long.MIN_VALUE);
    final static private BigInteger INT_MAX_BIGINT = BigInteger.valueOf(Integer.MAX_VALUE);
    final static private BigInteger INT_MIN_BIGINT = BigInteger.valueOf(Integer.MIN_VALUE);
    final static private Long INT_MAX_LONG = Long.valueOf(Integer.MAX_VALUE);
    final static private Long INT_MIN_LONG = Long.valueOf(Integer.MIN_VALUE);

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Recursively convert a GSON {@link JsonElement} to an equivalent Java map,
     * list, string, or numeric type.
     */
    static Object toObject(JsonElement element) {
        Object result;
        if (element == null) {
            result = null;
        }
        else if (element.isJsonNull()) {
            result = null;
        }
        else if (element.isJsonArray()) {
            ArrayList<Object> list = new ArrayList<Object>();
            JsonArray array = element.getAsJsonArray();
            for (JsonElement e : array) {
                list.add(toObject(e));
            }
            result = list;
        }
        else if (element.isJsonObject()) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                map.put(entry.getKey(), toObject(entry.getValue()));
            }
            result = map;
        }
        else {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                result = element.getAsBoolean();
            }
            else if (primitive.isNumber()) {
                result = toSmallestIntegerType(element.getAsNumber());
            }
            else {
                result = element.getAsString();
            }
        }
        return result;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Coerce a {@link Number} down to smallest integer type of
     * {@link BigInteger}, {@link Long}, or {@link Integer} if the conversion is
     * exact, or return the same value.
     */
    static private Number toSmallestIntegerType(Number value) {
        Number result = value;
        if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) value;
            try {
                result = bigDecimal.intValueExact();
            }
            catch (ArithmeticException e) {
                try {
                    result = bigDecimal.longValueExact();
                }
                catch (ArithmeticException e2) {
                    try {
                        result = bigDecimal.toBigIntegerExact();
                    }
                    catch (ArithmeticException e3) {
                        // result will be used unmodified
                    }
                }
            }
        }
        else if (value instanceof BigInteger) {
            BigInteger bigInteger = (BigInteger) value;
            if (inIntegerRange(bigInteger)) {
                result = bigInteger.intValue();
            }
            else if (inLongRange(bigInteger)) {
                result = bigInteger.longValue();
            }
        }
        else if (value instanceof Long) {
            Long longObj = (Long) value;
            if (inIntegerRange(longObj)) {
                result = longObj.intValue();
            }
        }
        return result;
    }

    //------------------------------------------------------------------------------------------------------------------
    static private boolean inLongRange(BigInteger value) {
        return value.compareTo(LONG_MIN_BIGINT) >= 0 || value.compareTo(LONG_MAX_BIGINT) <= 0;
    }

    //------------------------------------------------------------------------------------------------------------------
    static private boolean inIntegerRange(BigInteger value) {
        return value.compareTo(INT_MIN_BIGINT) >= 0 || value.compareTo(INT_MAX_BIGINT) <= 0;
    }

    //------------------------------------------------------------------------------------------------------------------
    static private boolean inIntegerRange(Long value) {
        return value.compareTo(INT_MIN_LONG) >= 0 || value.compareTo(INT_MAX_LONG) <= 0;
    }
}
