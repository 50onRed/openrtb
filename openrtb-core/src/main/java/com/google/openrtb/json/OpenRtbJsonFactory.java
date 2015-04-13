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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.protobuf.GeneratedMessage.ExtendableBuilder;
import com.google.protobuf.Message;

import com.fasterxml.jackson.core.JsonFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Factory that will create JSON serializer objects:
 * <ul>
 *   <li>Core model: {@link OpenRtbJsonWriter} and {@link OpenRtbJsonReader}</li>
 *   <li>Native model: {@link OpenRtbNativeJsonWriter} and {@link OpenRtbNativeJsonReader}</li>
 * </ul>
 * <p>
 * This class is NOT threadsafe. You should use only to configure and create the
 * reader/writer objects, which will be threadsafe.
 */
public class OpenRtbJsonFactory {
  private static final String FIELDNAME_ALL = "*";

  private JsonFactory jsonFactory;
  private final Multimap<String, OpenRtbJsonExtReader<?, ?>> extReaders;
  private final Map<String, Map<String, Map<String, OpenRtbJsonExtWriter<?>>>> extWriters;

  /**
   * Creates a new factory with default configuration.
   */
  public static OpenRtbJsonFactory create() {
    return new OpenRtbJsonFactory(null,
        LinkedListMultimap.<String, OpenRtbJsonExtReader<?, ?>>create(),
        new LinkedHashMap<String, Map<String, Map<String, OpenRtbJsonExtWriter<?>>>>());
  }

  private OpenRtbJsonFactory(
      JsonFactory jsonFactory,
      Multimap<String, OpenRtbJsonExtReader<?, ?>> extReaders,
      Map<String, Map<String, Map<String, OpenRtbJsonExtWriter<?>>>> extWriters) {
    this.jsonFactory = jsonFactory;
    this.extReaders = checkNotNull(extReaders);
    this.extWriters = checkNotNull(extWriters);
  }

  private OpenRtbJsonFactory immutableClone() {
    return new OpenRtbJsonFactory(
        getJsonFactory(),
        ImmutableMultimap.copyOf(extReaders),
        ImmutableMap.copyOf(Maps.transformValues(extWriters, new Function<
            Map<String, Map<String, OpenRtbJsonExtWriter<?>>>,
            Map<String, Map<String, OpenRtbJsonExtWriter<?>>>>() {
          @Override public Map<String, Map<String, OpenRtbJsonExtWriter<?>>> apply(
              Map<String, Map<String, OpenRtbJsonExtWriter<?>>> map) {
            return ImmutableMap.copyOf(Maps.transformValues(map, new Function<
                Map<String, OpenRtbJsonExtWriter<?>>, Map<String, OpenRtbJsonExtWriter<?>>>() {
              @Override public Map<String, OpenRtbJsonExtWriter<?>> apply(
                  Map<String, OpenRtbJsonExtWriter<?>> map) {
                return ImmutableMap.copyOf(map);
              }}));
          }})));
  }

  /**
   * Use a specific {@link JsonFactory}. A default factory will created if this is never called.
   */
  public final OpenRtbJsonFactory setJsonFactory(JsonFactory jsonFactory) {
    this.jsonFactory = checkNotNull(jsonFactory);
    return this;
  }

  /**
   * Register a desserializer extension.
   *
   * @param extReader code to desserialize some extension properties
   * @param msgKlass class of container message's builder, e.g. {@code MyImpression.Builder.class}
   */
  public final <EB extends ExtendableBuilder<?, EB>> OpenRtbJsonFactory register(
      OpenRtbJsonExtReader<EB, ?> extReader, Class<EB> msgKlass) {
    extReaders.put(msgKlass.getName(), extReader);
    return this;
  }

  /**
   * Register a serializer extension. Each of these is registered for a specific
   * "path" inside the OpenRTB model; for example, "BidRequest.Geo" registers
   * extensions for the {@code Geo} object inside the OpenRTB bid request.
   *
   * @param extWriter code to serialize some {@code extKlass}'s properties
   * @param extKlass class of container message, e.g. {@code MyImpression.class}
   * @param msgKlass Path in the OpenRTB model
   * @param <T> Type of value for the extension
   */
  public final <T> OpenRtbJsonFactory register(OpenRtbJsonExtWriter<T> extWriter,
    Class<T> extKlass, Class<? extends Message> msgKlass, String fieldName) {
    Map<String, Map<String, OpenRtbJsonExtWriter<?>>> mapMsg = extWriters.get(msgKlass.getName());
    if (mapMsg == null) {
      extWriters.put(msgKlass.getName(), mapMsg = new LinkedHashMap<>());
    }
    Map<String, OpenRtbJsonExtWriter<?>> mapKlass = mapMsg.get(extKlass.getName());
    if (mapKlass == null) {
      mapMsg.put(extKlass.getName(), mapKlass = new LinkedHashMap<>());
    }
    mapKlass.put(fieldName == null ? FIELDNAME_ALL : fieldName, extWriter);
    return this;
  }

  public final <T> OpenRtbJsonFactory register(OpenRtbJsonExtWriter<T> extWriter,
      Class<T> extKlass, Class<? extends Message> msgKlass) {
    return register(extWriter, extKlass, msgKlass, FIELDNAME_ALL);
  }

  /**
   * Creates an {@link OpenRtbJsonWriter}, configured to the current state of this factory.
   */
  public OpenRtbJsonWriter newWriter() {
    return new OpenRtbJsonWriter(immutableClone());
  }

  /**
   * Creates an {@link OpenRtbJsonWriter}, configured to the current state of this factory.
   */
  public OpenRtbJsonReader newReader() {
    return new OpenRtbJsonReader(immutableClone());
  }

  /**
   * Creates an {@link OpenRtbNativeJsonWriter}, configured to the current state of this factory.
   */
  public OpenRtbNativeJsonWriter newNativeWriter() {
    return new OpenRtbNativeJsonWriter(new OpenRtbJsonFactory(
        getJsonFactory(),
        ImmutableMultimap.copyOf(extReaders),
        ImmutableMap.copyOf(extWriters)));
  }

  /**
   * Creates an {@link OpenRtbNativeJsonWriter}, configured to the current state of this factory.
   */
  public OpenRtbNativeJsonReader newNativeReader() {
    return new OpenRtbNativeJsonReader(new OpenRtbJsonFactory(
        getJsonFactory(),
        ImmutableMultimap.copyOf(extReaders),
        ImmutableMap.copyOf(extWriters)));
  }

  @SuppressWarnings("unchecked")
  final <EB extends ExtendableBuilder<?, EB>>
  Collection<OpenRtbJsonExtReader<EB, ?>> getReaders(Class<EB> msgClass) {
    return (Collection<OpenRtbJsonExtReader<EB, ?>>) (Collection<?>)
        extReaders.get(msgClass.getName());
  }

  @SuppressWarnings("unchecked")
  final <T> OpenRtbJsonExtWriter<T> getWriter(
      Class<? extends Message> msgClass, Class<?> extClass, @Nullable String fieldName) {
    Map<String, Map<String, OpenRtbJsonExtWriter<?>>> mapMsg = extWriters.get(msgClass.getName());
    if (mapMsg == null) {
      return null;
    }
    Map<String, OpenRtbJsonExtWriter<?>> mapKlass = mapMsg.get(extClass.getName());
    if (mapKlass == null) {
      return null;
    }
    if (fieldName != null && !FIELDNAME_ALL.equals(fieldName)) {
      OpenRtbJsonExtWriter<T> writer = (OpenRtbJsonExtWriter<T>) mapKlass.get(fieldName);
      if (writer != null) {
        return writer;
      }
    }
    return (OpenRtbJsonExtWriter<T>) mapKlass.get(FIELDNAME_ALL);
  }

  /**
   * Returns the {@link JsonFactory} configured for this {@link OpenRtbJsonFactory}.
   * If you didn't set any value with {@link #setJsonFactory(JsonFactory)},
   * will create a default factory.
   */
  public final JsonFactory getJsonFactory() {
    if (jsonFactory == null) {
      jsonFactory = new JsonFactory();
    }
    return jsonFactory;
  }

  private static String fieldKey(String fieldName) {
    return fieldName == null ? FIELDNAME_ALL : fieldName;
  }
}
