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
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PiwikTrackerTest {

	@Test
	void shouldSendGetRequestForMatomoBackend() {
		RecordingHttpClient httpClient = new RecordingHttpClient(204);
		PiwikConfig config = PiwikConfig.builder().backend(TrackingBackend.MATOMO)
				.host("example.matomo.test").path("collect")
				.addIdSite("matomo-site")
				.build();

		PiwikRequest request = PiwikRequest.builder().url("https://example.com/page")
				.putParameter(QueryParameter.ACTION_NAME, "page view").build();

		boolean successful = new PiwikTracker(config, httpClient).send(request);

		assertTrue(successful);
		assertEquals(1, httpClient.requests().size());

		CapturedRequest capturedRequest = httpClient.requests().get(0);
		assertEquals("GET", capturedRequest.method());
		assertEquals("/collect", capturedRequest.path());
		assertEquals("matomo-site", capturedRequest.queryParameters().get("idsite"));
		assertEquals("1", capturedRequest.queryParameters().get("rec"));
		assertEquals("1", capturedRequest.queryParameters().get("apiv"));
		assertEquals("0", capturedRequest.queryParameters().get("send_image"));
		assertEquals("https://example.com/page",
				capturedRequest.queryParameters().get("url"));
		assertEquals("page view",
				capturedRequest.queryParameters().get("action_name"));
	}

	@Test
	void shouldSendPostRequestForPiwikProBackend() {
		RecordingHttpClient httpClient = new RecordingHttpClient(202);
		PiwikConfig config = PiwikConfig.builder().backend(TrackingBackend.PIWIK_PRO)
				.host("example.piwik.pro").addIdSite("piwik-pro-site").build();

		PiwikRequest request = PiwikRequest.builder().url("https://example.com/post")
				.build();

		boolean successful = new PiwikTracker(config, httpClient).send(request);

		assertTrue(successful);
		assertEquals(1, httpClient.requests().size());

		CapturedRequest capturedRequest = httpClient.requests().get(0);
		assertEquals("POST", capturedRequest.method());
		assertEquals("/ppms.php", capturedRequest.path());
		assertEquals("piwik-pro-site",
				capturedRequest.queryParameters().get("idsite"));
		assertEquals("https://example.com/post",
				capturedRequest.queryParameters().get("url"));
	}

	@Test
	void shouldSendOneRequestPerSite() {
		RecordingHttpClient httpClient = new RecordingHttpClient(204);
		PiwikConfig config = PiwikConfig.builder().backend(TrackingBackend.MATOMO)
				.host("example.matomo.test").path("fanout")
				.addAllIdSite(Arrays.asList("site-1", "site-2", "site-3"))
				.build();

		PiwikRequest request = PiwikRequest.builder().url("https://example.com/fanout")
				.build();

		boolean successful = new PiwikTracker(config, httpClient).send(request);

		assertTrue(successful);
		assertEquals(3, httpClient.requests().size());

		List<String> siteIds = new ArrayList<>();
		for (CapturedRequest capturedRequest : httpClient.requests()) {
			siteIds.add(capturedRequest.queryParameters().get("idsite"));
			assertEquals("/fanout", capturedRequest.path());
			assertEquals("GET", capturedRequest.method());
		}

		Collections.sort(siteIds);
		assertEquals(Arrays.asList("site-1", "site-2", "site-3"), siteIds);
	}

	private static Map<String, String> parseQuery(String query) {
		if (query == null || query.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, String> parameters = new LinkedHashMap<>();
		for (String part : query.split("&")) {
			String[] pieces = part.split("=", 2);
			String name = decode(pieces[0]);
			String value = pieces.length > 1 ? decode(pieces[1]) : "";
			parameters.put(name, value);
		}
		return parameters;
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	private static final class RecordingHttpClient extends HttpClient {

		private final int statusCode;

		private final List<CapturedRequest> requests = new ArrayList<>();

		private RecordingHttpClient(int statusCode) {
			this.statusCode = statusCode;
		}

		private List<CapturedRequest> requests() {
			return this.requests;
		}

		@Override
		public Optional<CookieHandler> cookieHandler() {
			return Optional.empty();
		}

		@Override
		public Optional<Duration> connectTimeout() {
			return Optional.empty();
		}

		@Override
		public Redirect followRedirects() {
			return Redirect.NEVER;
		}

		@Override
		public Optional<ProxySelector> proxy() {
			return Optional.empty();
		}

		@Override
		public SSLContext sslContext() {
			return null;
		}

		@Override
		public SSLParameters sslParameters() {
			return new SSLParameters();
		}

		@Override
		public Optional<Authenticator> authenticator() {
			return Optional.empty();
		}

		@Override
		public HttpClient.Version version() {
			return HttpClient.Version.HTTP_1_1;
		}

		@Override
		public Optional<Executor> executor() {
			return Optional.empty();
		}

		@Override
		public <T> HttpResponse<T> send(HttpRequest request,
				HttpResponse.BodyHandler<T> responseBodyHandler)
				throws IOException, InterruptedException {
			this.requests.add(CapturedRequest.from(request));
			return new StubHttpResponse<>(request, this.statusCode, null);
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
				HttpResponse.BodyHandler<T> responseBodyHandler) {
			this.requests.add(CapturedRequest.from(request));
			return CompletableFuture.completedFuture(
					new StubHttpResponse<>(request, this.statusCode, null));
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
				HttpResponse.BodyHandler<T> responseBodyHandler,
				HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
			return sendAsync(request, responseBodyHandler);
		}

		@Override
		public WebSocket.Builder newWebSocketBuilder() {
			throw new UnsupportedOperationException();
		}

	}

	private static final class StubHttpResponse<T> implements HttpResponse<T> {

		private final HttpRequest request;

		private final int statusCode;

		private final T body;

		private StubHttpResponse(HttpRequest request, int statusCode, T body) {
			this.request = request;
			this.statusCode = statusCode;
			this.body = body;
		}

		@Override
		public int statusCode() {
			return this.statusCode;
		}

		@Override
		public HttpRequest request() {
			return this.request;
		}

		@Override
		public Optional<HttpResponse<T>> previousResponse() {
			return Optional.empty();
		}

		@Override
		public HttpHeaders headers() {
			return HttpHeaders.of(Collections.emptyMap(), (name, value) -> true);
		}

		@Override
		public T body() {
			return this.body;
		}

		@Override
		public Optional<SSLSession> sslSession() {
			return Optional.empty();
		}

		@Override
		public URI uri() {
			return this.request.uri();
		}

		@Override
		public HttpClient.Version version() {
			return HttpClient.Version.HTTP_1_1;
		}

	}

	private record CapturedRequest(String method, String path,
			Map<String, String> queryParameters) {

		private static CapturedRequest from(HttpRequest request) {
			return new CapturedRequest(request.method(), request.uri().getPath(),
					parseQuery(request.uri().getRawQuery()));
		}

	}

}
