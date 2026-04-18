/*
 * Copyright the original author or authors.
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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PiwikTracker {

	private static final Logger logger = LoggerFactory.getLogger(PiwikTracker.class);

	private final PiwikConfig config;

	private final HttpClient httpClient;

	private final ExecutorService executorService;

	private final Random random;

	public PiwikTracker(final PiwikConfig config) {
		this(config, Executors.newCachedThreadPool());
	}

	private PiwikTracker(final PiwikConfig config,
			final ExecutorService executorService) {
		this(config, HttpClient.newBuilder().executor(executorService).build(),
				executorService);
	}

	public PiwikTracker(final PiwikConfig config, final HttpClient httpClient) {
		this(config, httpClient, null);
	}

	private PiwikTracker(final PiwikConfig config, final HttpClient httpClient,
			final ExecutorService executorService) {
		this.config = config;
		this.httpClient = httpClient;
		this.executorService = executorService;
		this.random = new Random();
	}

	public void shutdown() {
		if (this.executorService != null) {
			this.executorService.shutdown();
		}
	}

	public void shutdownNow() {
		if (this.executorService != null) {
			this.executorService.shutdownNow();
		}
	}

	/**
	 * Sends an asynchronious tracking request to Piwik
	 */
	public CompletableFuture<Boolean> sendAsync(PiwikRequest trackingRequest) {
		List<String> siteIds = getSiteIds(trackingRequest);
		List<CompletableFuture<Boolean>> futures = new ArrayList<>(siteIds.size());

		for (String siteId : siteIds) {
			HttpRequest httpRequest = createHttpRequest(trackingRequest, siteId);
			CompletableFuture<Boolean> future = this.httpClient
					.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
					.thenApply(PiwikTracker::isSuccessful)
					.whenComplete((successful, throwable) -> {
						if (throwable != null) {
							logger.error("sendAsync for site {}", siteId, throwable);
						}
						else if (!successful) {
							logger.error("async request for site {} was not successful",
									siteId);
						}
					});
			futures.add(future);
		}

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
				.thenApply(ignored -> futures.stream().allMatch(CompletableFuture::join));
	}

	/**
	 * Sends a synchronious tracking request to Piwik. The method returns true when the
	 * http get request is successful.
	 */
	public boolean send(PiwikRequest trackingRequest) {

		for (String siteId : getSiteIds(trackingRequest)) {
			HttpRequest httpRequest = createHttpRequest(trackingRequest, siteId);

			try {
				HttpResponse<Void> response = this.httpClient.send(httpRequest,
						HttpResponse.BodyHandlers.discarding());
				if (!isSuccessful(response)) {
					logger.error("request for site {} was not successful", siteId);
					return false;
				}
			}
			catch (IOException e) {
				logger.error("send for site {}", siteId, e);
				return false;
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("send for site {}", siteId, e);
				return false;
			}
		}

		return true;
	}

	private HttpRequest createHttpRequest(PiwikRequest trackingRequest,
			String siteId) {
		HttpRequest.Builder builder = HttpRequest.newBuilder(
				createUri(trackingRequest, siteId));

		if (this.config.httpMethod() == TrackingHttpMethod.POST) {
			return builder.POST(HttpRequest.BodyPublishers.noBody()).build();
		}

		return builder.GET().build();
	}

	private URI createUri(PiwikRequest trackingRequest, String siteId) {
		byte[] bytes = new byte[10];
		this.random.nextBytes(bytes);

		List<String> queryParameters = new ArrayList<>();
		addQueryParameter(queryParameters, "rec", "1");
		addQueryParameter(queryParameters, "url", trackingRequest.url());
		addQueryParameter(queryParameters, "rand", printHexBinary(bytes));
		addQueryParameter(queryParameters, "apiv", "1");
		addQueryParameter(queryParameters, "send_image", "0");

		if (this.config.authToken().isPresent()) {
			addQueryParameter(queryParameters, "token_auth",
					this.config.authToken().get());
		}

		addQueryParameter(queryParameters, "idsite", siteId);

		if (!trackingRequest.parameters().isEmpty()) {
			trackingRequest.parameters().forEach((k, v) -> addQueryParameter(
					queryParameters, k.getValue(), String.valueOf(v)));
		}

		StringBuilder uriBuilder = new StringBuilder();
		uriBuilder.append(this.config.scheme()).append("://")
				.append(this.config.host());

		String normalizedPath = normalizePath(this.config.path());
		if (!normalizedPath.isEmpty()) {
			uriBuilder.append('/').append(normalizedPath);
		}

		uriBuilder.append('?').append(String.join("&", queryParameters));
		return URI.create(uriBuilder.toString());
	}

	private List<String> getSiteIds(PiwikRequest trackingRequest) {
		List<String> siteIds = trackingRequest.idSite().isEmpty() ? this.config.idSite()
				: trackingRequest.idSite();

		if (siteIds.isEmpty()) {
			throw new IllegalArgumentException("At least one idSite is required");
		}

		for (String siteId : siteIds) {
			if (siteId == null || siteId.trim().isEmpty()) {
				throw new IllegalArgumentException("idSite must not be blank");
			}
		}

		return Collections.unmodifiableList(new ArrayList<>(siteIds));
	}

	private static boolean isSuccessful(HttpResponse<?> response) {
		int statusCode = response.statusCode();
		return statusCode >= 200 && statusCode < 300;
	}

	private static void addQueryParameter(List<String> queryParameters,
			String name, String value) {
		queryParameters.add(encode(Objects.requireNonNull(name, "name")) + "="
				+ encode(Objects.requireNonNull(value, "value")));
	}

	private static String normalizePath(String path) {
		String normalizedPath = path;
		while (normalizedPath.startsWith("/")) {
			normalizedPath = normalizedPath.substring(1);
		}
		return normalizedPath;
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8)
				.replace("+", "%20");
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
