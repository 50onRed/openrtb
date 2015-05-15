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

import static com.google.openrtb.json.OpenRtbJsonUtils.endObject;
import static com.google.openrtb.json.OpenRtbJsonUtils.startObject;

import com.google.protobuf.GeneratedMessage.ExtendableBuilder;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Set;

/**
 * Desserializes OpenRTB messages from JSON.
 */
public abstract class AbstractOpenRtbJsonReader {
  private final OpenRtbJsonFactory factory;

  protected AbstractOpenRtbJsonReader(OpenRtbJsonFactory factory) {
    this.factory = factory;
  }

  public final OpenRtbJsonFactory factory() {
    return factory;
  }

  protected <EB extends ExtendableBuilder<?, EB>>
  void readOther(EB msg, JsonParser par, String fieldName) throws IOException {
    if ("ext".equals(fieldName)) {
      readExtensions(msg, par);
    } else {
      par.skipChildren();
    }
  }

  /**
   * Read any extensions that may exist in a message.
   *
   * @param msg Builder of a message that may contain extensions
   * @param par The JSON parser, positioned at the "ext" field
   * @throws IOException any parsing error
   */
  protected <EB extends ExtendableBuilder<?, EB>>
  void readExtensions(EB msg, JsonParser par) throws IOException {
    startObject(par);
    @SuppressWarnings("unchecked")
    Set<OpenRtbJsonExtReader<EB, ?>> extReaders =
        factory.getReaders((Class<EB>) msg.getClass());
    JsonToken tokLast = par.getCurrentToken();
    JsonLocation locLast = par.getCurrentLocation();

    while (true) {
      boolean extRead = false;
      for (OpenRtbJsonExtReader<EB, ?> extReader : extReaders) {
        extReader.read(msg, par);
        JsonToken tokNew = par.getCurrentToken();
        JsonLocation locNew = par.getCurrentLocation();
        boolean advanced = tokNew != tokLast || !locNew.equals(locLast);
        extRead |= advanced;

        if (!endObject(par)) {
          return;
        } else if (advanced && par.getCurrentToken() != JsonToken.FIELD_NAME) {
          par.nextToken();
          tokLast = par.getCurrentToken();
          locLast = par.getCurrentLocation();
        } else {
          tokLast = tokNew;
          locLast = locNew;
        }
      }

      if (!extRead) {
        par.nextToken();
        par.skipChildren();
      }
      // Else loop, try all readers again
    }
  }
}
