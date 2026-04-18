![Test Status](https://github.com/ralscha/piwik-tracking/workflows/test/badge.svg)


piwik-tracking is a library that helps sending tracking requests to 
[Matomo](https://matomo.org/) and [Piwik PRO](https://piwik.pro/) tracking servers
from a Java application.

Version 2 requires Java 17 or newer. Use version 1.0.3 if you use an older Java version.


## Usage

First create an instance of PiwikConfig to configure the tracker. Mandatory input
is the tracking backend and the name of the Matomo or Piwik PRO server that hosts it.

```
  PiwikConfig config = PiwikConfig.builder()
                .backend(TrackingBackend.MATOMO)
                .host("mypiwik.host.com")
                .addIdSite("1")
                .authToken("ffffffffffffffffffffff")
                .build();
```

Supported backend profiles:

* `TrackingBackend.MATOMO` defaults to `matomo.php` and `GET`
* `TrackingBackend.PIWIK_PRO` defaults to `ppms.php` and `POST`

You can override the backend defaults when needed:

```java
PiwikConfig config = PiwikConfig.builder()
                .backend(TrackingBackend.PIWIK_PRO)
                .host("example.piwik.pro")
                .path("ppms.php")
                .httpMethod(TrackingHttpMethod.POST)
                .addIdSite("892d04bd-6e2b-4914-bfb4-bac721b37235")
                .build();
```

Next create an instance of the PiwikTracker.
```
  PiwikTracker tracker = new PiwikTracker(config);
```

The application then has to create an instance of PiwikRequest for every action it wants
to send a tracking request to Piwik.
```
  PiwikRequest request = PiwikRequest.builder().url("http://my.site.com/index.html")
		 		.putParameter(QueryParameter.ACTION_NAME, "anAction")
				.putParameter(QueryParameter.VISITOR_ID, 1)
				.build();
```

and send the request to Piwik either synchronous (blocking) or asynchronous (non-blocking). 
```
  //send blocking request
  tracker.send(request);

  //send non blocking request
  CompletableFuture<Boolean> result = tracker.sendAsync(request);
```

When the application sends asynchronous requests the tracker internally starts an ExecutorService.
To properly shutdown this service an application can call the 
shutdown method: ```tracker.shutdown();```

## Version 2 notes

Version 2 requires Java 17 or newer.

The asynchronous API changed with the switch from OkHttp to the JDK HTTP client:

* `PiwikTracker(HttpClient)` replaces `PiwikTracker(OkHttpClient)` for custom client injection.
* `sendAsync(PiwikRequest)` now returns `CompletableFuture<Boolean>`.
* The OkHttp-specific callback overload was removed.
* `PiwikConfig.backend(...)` is now required so Matomo and Piwik PRO behavior is explicit.
* When multiple `idSite` values are configured, the tracker now sends one request per site.

Migration example:

```java
import java.io.IOException;
import java.net.http.HttpClient;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

// 1.x
OkHttpClient okHttpClient = new OkHttpClient();
PiwikTracker tracker = new PiwikTracker(config, okHttpClient);
tracker.sendAsync(request, new Callback() {
  @Override
  public void onFailure(Call call, IOException e) {
    // handle error
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    if (response.isSuccessful()) {
      // handle success
    }
  }
});

// 2.x
HttpClient httpClient = HttpClient.newHttpClient();
PiwikConfig config = PiwikConfig.builder()
  .backend(TrackingBackend.MATOMO)
  .host("example.matomo.test")
  .addIdSite("1")
  .build();
PiwikTracker tracker = new PiwikTracker(config, httpClient);
tracker.sendAsync(request)
    .thenAccept(successful -> {
      if (successful) {
        // handle success
      }
      else {
        // handle non-2xx response
      }
    })
    .exceptionally(throwable -> {
      // handle transport error
      return null;
    });
```


### Tracking HTTP API
For more information about the supported request parameters see the official product documentation:

* [Matomo Tracking HTTP API](https://developer.matomo.org/api-reference/tracking-api)
* [Piwik PRO Tracking endpoint](https://developers.piwik.pro/reference/post_ppms-php)



## Maven

The library is hosted on the Central Maven Repository

```
	<dependency>
		<groupId>ch.rasc</groupId>
		<artifactId>piwik-tracking</artifactId>
		<version>2.0.0</version>
	</dependency>
```

## Changelog

### 2.0.0 - April 18, 2026
  * Raise the minimum Java version to 17
  * Replace OkHttp with the standard JDK HTTP client
  * Change the async API to return `CompletableFuture<Boolean>`
  * Replace custom `OkHttpClient` injection with `HttpClient`
  * Add explicit backend profiles for Matomo and Piwik PRO
  * Add configurable HTTP method support with backend-specific defaults
  * Send one tracking request per `idSite` instead of comma-joining site IDs

### 1.0.3 - August 12, 2019
  * [Issue 1](https://github.com/ralscha/piwik-tracking/issues/1): token_auth shouldn't be mandatory
  * [Issue 2](https://github.com/ralscha/piwik-tracking/issues/2): path and scheme cannot be changed
  * [Issue 3](https://github.com/ralscha/piwik-tracking/issues/3): Cannot add path wish slashes

### 1.0.2 - September 26, 2018
  * Replace `javax.xml.bind.DatatypeConverter` code. This package no longer exists in Java 11

### 1.0.1 - December 6, 2016
  * Remove unnecessary System.out.println call

### 1.0.0 - December 3, 2016
  * Initial release


## License
Code released under [the Apache license](http://www.apache.org/licenses/).
