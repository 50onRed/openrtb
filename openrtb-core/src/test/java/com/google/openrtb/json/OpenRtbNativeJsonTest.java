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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.google.openrtb.OpenRtbNative.NativeRequest;
import com.google.openrtb.OpenRtbNative.NativeRequest.AdunitID;
import com.google.openrtb.OpenRtbNative.NativeRequest.Asset.Data.DataAssetType;
import com.google.openrtb.OpenRtbNative.NativeRequest.Asset.Image.ImageAssetType;
import com.google.openrtb.OpenRtbNative.NativeRequest.LayoutID;
import com.google.openrtb.OpenRtbNative.NativeResponse;
import com.google.openrtb.Test.Test1;
import com.google.openrtb.Test.Test2;
import com.google.openrtb.TestNExt;

import com.fasterxml.jackson.core.JsonFactory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Tests for {@link OpenRtbJsonWriter}.
 */
public class OpenRtbNativeJsonTest {
  private static final Logger logger = LoggerFactory.getLogger(OpenRtbNativeJsonTest.class);
  private static final Test1 test1 = Test1.newBuilder().setTest1("test1").build();
  private static final Test2 test2 = Test2.newBuilder().setTest2("test2").build();

  @Test
  public void testRequest() throws IOException {
    testRequest(newJsonFactory(), newNativeRequest().build());
  }

  @Test
  public void testResponse() throws IOException {
    testResponse(newJsonFactory(), newNativeResponse().build());
  }

  static void testRequest(OpenRtbJsonFactory jsonFactory, NativeRequest req) throws IOException {
    String jsonReq = jsonFactory.newNativeWriter().writeNativeRequest(req);
    logger.info(jsonReq);
    NativeRequest req2 = jsonFactory.newNativeReader().readNativeRequest(jsonReq);
    assertEquals(req, req2);
  }

  static void testResponse(OpenRtbJsonFactory jsonFactory, NativeResponse resp) throws IOException {
    String jsonResp = jsonFactory.newNativeWriter().writeNativeResponse(resp);
    logger.info(jsonResp);
    NativeResponse resp2 = jsonFactory.newNativeReader().readNativeResponse(jsonResp);
    assertEquals(resp, resp2);
  }

  static OpenRtbJsonFactory newJsonFactory() {
    return OpenRtbJsonFactory.create()
        .setJsonFactory(new JsonFactory())
        // NativeRequest Readers
        .register(new Test1Reader<NativeRequest.Builder>(TestNExt.testNRequest1), "NativeRequest")
        .register(new Test2Reader<NativeRequest.Builder>(TestNExt.testNRequest2), "NativeRequest")
        .register(new Test1Reader<NativeRequest.Asset.Builder>(TestNExt.testNReqAsset), "NativeRequest.asset")
        .register(new Test1Reader<NativeRequest.Asset.Title.Builder>(TestNExt.testNReqTitle), "NativeRequest.asset.title")
        .register(new Test1Reader<NativeRequest.Asset.Image.Builder>(TestNExt.testNReqImage), "NativeRequest.asset.img")
        .register(new Test1Reader<NativeRequest.Asset.Video.Builder>(TestNExt.testNReqVideo), "NativeRequest.asset.video")
        .register(new Test1Reader<NativeRequest.Asset.Data.Builder>(TestNExt.testNReqData), "NativeRequest.asset.data")
        // NativeResponse Readers
        .register(new Test1Reader<NativeResponse.Builder>(TestNExt.testNResponse1), "NativeResponse")
        .register(new Test2Reader<NativeResponse.Builder>(TestNExt.testNResponse2), "NativeResponse")
        .register(new Test1Reader<NativeResponse.Link.Builder>(TestNExt.testNRespLink), "NativeResponse.link")
        .register(new Test1Reader<NativeResponse.Asset.Builder>(TestNExt.testNRespAsset), "NativeResponse.asset")
        .register(new Test1Reader<NativeResponse.Link.Builder>(TestNExt.testNRespLink), "NativeResponse.asset.link")
        .register(new Test1Reader<NativeResponse.Asset.Title.Builder>(TestNExt.testNRespTitle), "NativeResponse.asset.title")
        .register(new Test1Reader<NativeResponse.Asset.Image.Builder>(TestNExt.testNRespImage), "NativeResponse.asset.img")
        .register(new Test1Reader<NativeResponse.Asset.Video.Builder>(TestNExt.testNRespVideo), "NativeResponse.asset.video")
        .register(new Test1Reader<NativeResponse.Asset.Data.Builder>(TestNExt.testNRespData), "NativeResponse.asset.data")
        // Writers
        .register(new Test1Writer(), Test1.class,
            "NativeRequest", "NativeRequest.asset",
            "NativeRequest.asset.title", "NativeRequest.asset.img",
            "NativeRequest.asset.video", "NativeRequest.asset.data",
            "NativeResponse", "NativeResponse.link",
            "NativeResponse.asset", "NativeResponse.asset.link",
            "NativeResponse.asset.title", "NativeResponse.asset.img",
            "NativeResponse.asset.video", "NativeResponse.asset.data")
        .register(new Test2Writer(), Test2.class, "NativeRequest", "NativeResponse");
  }

  static NativeRequest.Builder newNativeRequest() {
    return NativeRequest.newBuilder()
        .setVer("1")
        .setLayout(LayoutID.APP_WALL)
        .setAdunit(AdunitID.PROMOTED_LISTING)
        .setPlcmtcnt(4)
        .setSeq(5)
        .addAssets(NativeRequest.Asset.newBuilder()
            .setId(1)
            .setRequired(true)
            .setTitle(NativeRequest.Asset.Title.newBuilder()
                .setLen(100)
                .setExtension(TestNExt.testNReqTitle, test1))
            .setImg(NativeRequest.Asset.Image.newBuilder()
                .setType(ImageAssetType.ICON)
                .setW(2)
                .setWmin(2)
                .setH(3)
                .setHmin(4)
                .addAllMimes(asList("a", "b"))
                .setExtension(TestNExt.testNReqImage, test1))
            .setVideo(NativeRequest.Asset.Video.newBuilder()
                .addAllMimes(asList("a", "b"))
                .setMinduration(100)
                .setMaxduration(200)
                .addAllProtocols(asList(1, 2, 3))
                .setExtension(TestNExt.testNReqVideo, test1))
            .setData(NativeRequest.Asset.Data.newBuilder()
                .setType(DataAssetType.SPONSORED)
                .setLen(10)
                .setExtension(TestNExt.testNReqData, test1))
            .setExtension(TestNExt.testNReqAsset, test1))
        .setExtension(TestNExt.testNRequest1, test1)
        .setExtension(TestNExt.testNRequest2, test2);
  }

  static NativeResponse.Builder newNativeResponse() {
    return NativeResponse.newBuilder()
        .setVer("1")
        .addAssets(NativeResponse.Asset.newBuilder()
            .setId(1)
            .setReq(true)
            .setTitle(NativeResponse.Asset.Title.newBuilder()
                .setText("title")
                .setExtension(TestNExt.testNRespTitle, test1))
            .setImg(NativeResponse.Asset.Image.newBuilder()
                .setUrl("url")
                .setW(2)
                .setH(3)
                .setExtension(TestNExt.testNRespImage, test1))
            .setVideo(NativeResponse.Asset.Video.newBuilder()
                .addAllVasttag(asList("a", "b"))
                .setExtension(TestNExt.testNRespVideo, test1))
            .setData(NativeResponse.Asset.Data.newBuilder()
                .setLabel("l")
                .setValue("v")
                .setExtension(TestNExt.testNRespData, test1))
            .setLink(NativeResponse.Link.newBuilder()
                .setUrl("url")
                .addAllClicktrackers(asList("a", "b"))
                .setFallback("f")
                .setExtension(TestNExt.testNRespLink, test1))
            .setExtension(TestNExt.testNRespAsset, test1))
        .setLink(NativeResponse.Link.newBuilder()
            .setUrl("url")
            .addAllClicktrackers(asList("a", "b"))
            .setFallback("f")
            .setExtension(TestNExt.testNRespLink, test1))
        .addAllImptrackers(asList("a"))
        .setJstracker("b")
    .setExtension(TestNExt.testNResponse1, test1)
    .setExtension(TestNExt.testNResponse2, test2);
  }
}
