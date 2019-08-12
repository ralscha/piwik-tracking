/**
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.rasc.piwik.tracking;

import java.io.IOException;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PiwikTracker {

	private final PiwikConfig config;

	private final OkHttpClient httpClient;

	private final Random random;

	private final String idSite;

	public PiwikTracker(final PiwikConfig config) {
		this(config, new OkHttpClient());
	}

	public PiwikTracker(final PiwikConfig config, final OkHttpClient httpClient) {
		this.config = config;
		this.httpClient = httpClient;
		this.random = new Random();

		if (!config.idSite().isEmpty()) {
			this.idSite = config.idSite().stream().collect(Collectors.joining(","));
		}
		else {
			this.idSite = null;
		}
	}

	public void shutdown() {
		this.httpClient.dispatcher().executorService().shutdown();
	}

	public void shutdownNow() {
		this.httpClient.dispatcher().executorService().shutdownNow();
	}

	public void sendAsync(PiwikRequest trackingRequest) {
		sendAsync(trackingRequest, null);
	}

	/**
	 * Sends an asynchronious tracking request to Piwik
	 */
	public void sendAsync(PiwikRequest trackingRequest, Callback callback) {
		Request httpRequest = createHttpRequest(trackingRequest);

		if (callback != null) {
			this.httpClient.newCall(httpRequest).enqueue(callback);
		}
		else {
			this.httpClient.newCall(httpRequest).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					LoggerFactory.getLogger(getClass()).error("sendAsync", e);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					if (!response.isSuccessful()) {
						LoggerFactory.getLogger(getClass()).error(
								"asnyc request was not successful. http response code: {}",
								response.code());
					}
				}
			});
		}
	}

	/**
	 * Sends a synchronious tracking request to Piwik. The method returns true when the
	 * http get request is successful.
	 */
	public boolean send(PiwikRequest trackingRequest) {

		Request httpRequest = createHttpRequest(trackingRequest);

		try (Response response = this.httpClient.newCall(httpRequest).execute()) {
			return response.isSuccessful();
		}
		catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("send", e);
			return false;
		}

	}

	private Request createHttpRequest(PiwikRequest trackingRequest) {
		byte[] bytes = new byte[10];
		this.random.nextBytes(bytes);

		HttpUrl.Builder urlBuilder = new HttpUrl.Builder().scheme(this.config.scheme())
				.host(this.config.host()).addPathSegment(this.config.path())
				.addQueryParameter("rec", "1")
				.addQueryParameter("url", trackingRequest.url())
				.addQueryParameter("rand", printHexBinary(bytes))
				.addQueryParameter("apiv", "1").addQueryParameter("send_image", "0");

		if (this.config.authToken().isPresent()) {
			urlBuilder.addQueryParameter("token_auth", this.config.authToken().get());
		}

		if (trackingRequest.idSite().isEmpty()) {
			if (this.idSite != null) {
				urlBuilder.addQueryParameter("idsite", this.idSite);
			}
			else {
				throw new IllegalArgumentException("idSite is a required parameter");
			}
		}
		else {
			urlBuilder.addQueryParameter("idsite",
					trackingRequest.idSite().stream().collect(Collectors.joining(",")));
		}

		if (!trackingRequest.parameters().isEmpty()) {
			trackingRequest.parameters().forEach((k, v) -> urlBuilder
					.addQueryParameter(k.getValue(), String.valueOf(v)));
		}

		Request httpRequest = new Request.Builder().url(urlBuilder.build()).build();
		return httpRequest;
	}

	private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

	private static String printHexBinary(byte[] data) {
		StringBuilder r = new StringBuilder(data.length * 2);
		for (byte b : data) {
			r.append(hexCode[(b >> 4) & 0xF]);
			r.append(hexCode[(b & 0xF)]);
		}
		return r.toString();
	}

}
