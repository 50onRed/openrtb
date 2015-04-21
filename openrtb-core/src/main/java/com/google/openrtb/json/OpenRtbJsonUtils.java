/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openrtb.json;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.protobuf.ProtocolMessageEnum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for writing JSON serialization code.
 */
public class OpenRtbJsonUtils {
  private static final Joiner CSV_JOINER = Joiner.on(",");
  private static final Splitter CSV_SPLITTER = Splitter.on(",");

  public static String getCurrentName(JsonParser par) throws JsonParseException, IOException {
    String name = par.getCurrentName();
    return name == null ? "" : name;
  }

  public static void startObject(JsonParser par) throws IOException {
    JsonToken token = par.getCurrentToken();
    if (token == null || token == JsonToken.FIELD_NAME) {
      token = par.nextToken();
    }
    if (token == JsonToken.START_OBJECT) {
      par.nextToken();
    } else {
      throw new JsonParseException("Expected start of object", par.getCurrentLocation());
    }
  }

  public static boolean endObject(JsonParser par) {
    JsonToken token = par.getCurrentToken();
    return token != null && token != JsonToken.END_OBJECT;
  }

  public static void startArray(JsonParser par) throws IOException {
    JsonToken token = par.getCurrentToken();
    if (token == null || token == JsonToken.FIELD_NAME) {
      token = par.nextToken();
    }
    if (token == JsonToken.START_ARRAY) {
      par.nextToken();
    } else {
      throw new JsonParseException("Expected start of array", par.getCurrentLocation());
    }
  }

  public static boolean endArray(JsonParser par) {
    JsonToken token = par.getCurrentToken();
    return token != null && token != JsonToken.END_ARRAY;
  }

  @Deprecated
  public static double nextDoubleValue(JsonParser par) throws IOException, JsonParseException {
    par.nextToken();
    return Double.parseDouble(par.getText());
  }

  public static double getDoubleValue(JsonParser par) throws IOException, JsonParseException {
    return Double.parseDouble(par.getText());
  }

  @Deprecated
  public static boolean nextIntBoolValue(JsonParser par) throws IOException, JsonParseException {
    return par.nextIntValue(0) != 0;
  }

  public static boolean getIntBoolValue(JsonParser par) throws IOException, JsonParseException {
    return par.getIntValue() != 0;
  }

  /**
   * Reads from either a JSON Value String (containing CSV) or a JSON Array.
   * The dual input format is needed because some fields (e.g. keywords) were allowed
   * to be of either type in OpenRTB 2.2; now in 2.3 they are all CSV strings only.
   * TODO: Simplify this to only accept CSV strings after 2.2 compatibility is dropped.
   */
  public static Iterable<String> readCsvString(JsonParser par) throws IOException, JsonParseException {
    JsonToken currentToken = par.getCurrentToken();
    if (currentToken == JsonToken.START_ARRAY) {
      List<String> keywords = new ArrayList<>();
      for (startArray(par); endArray(par); par.nextToken()) {
        keywords.add(par.getText());
      }
      return keywords;
    } else if (currentToken == JsonToken.VALUE_STRING) {
      return CSV_SPLITTER.split(par.getText());
    } else {
      throw new JsonParseException("Expected string or array", par.getCurrentLocation());
    }
  }

  public static void writeCsvString(String fieldName, List<String> data, JsonGenerator gen)
      throws IOException {
    if (!data.isEmpty()) {
      gen.writeStringField(fieldName, CSV_JOINER.join(data));
    }
  }

  public static void writeIntBoolField(String fieldName, boolean data, JsonGenerator gen)
      throws IOException, JsonParseException {
    gen.writeNumberField(fieldName, data ? 1 : 0);
  }

  public static void writeStrings(String fieldName, List<String> data, JsonGenerator gen)
      throws IOException {
    if (!data.isEmpty()) {
      writeRequiredStrings(fieldName, data, gen);
    }
  }

  public static void writeRequiredStrings(String fieldName, List<String> data, JsonGenerator gen)
    throws IOException {
    gen.writeArrayFieldStart(fieldName);
    for (String d : data) {
      gen.writeString(d);
    }
    gen.writeEndArray();
  }

  public static void writeInts(String fieldName, List<Integer> data, JsonGenerator gen)
      throws IOException {
    if (!data.isEmpty()) {
      writeRequiredInts(fieldName, data, gen);
    }
  }

  public static void writeRequiredInts(String fieldName, List<Integer> data, JsonGenerator gen)
      throws IOException {
    gen.writeArrayFieldStart(fieldName);
    for (Integer d : data) {
      gen.writeNumber(d);
    }
    gen.writeEndArray();
  }

  public static void writeEnums(
      String fieldName, List<? extends ProtocolMessageEnum> enums, JsonGenerator gen)
      throws IOException {
    if (!enums.isEmpty()) {
      writeRequiredEnums(fieldName, enums, gen);
    }
  }

  public static void writeRequiredEnums(
      String fieldName, List<? extends ProtocolMessageEnum> enums, JsonGenerator gen)
      throws IOException {
    gen.writeArrayFieldStart(fieldName);
    for (ProtocolMessageEnum e : enums) {
      gen.writeNumber(e.getNumber());
    }
    gen.writeEndArray();
  }
}
