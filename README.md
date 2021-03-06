![Test Status](https://github.com/ralscha/piwik-tracking/workflows/test/badge.svg)


piwik-tracking is a library that helps sending tracking requests to a [Piwik](https://piwik.org/) server 
from a Java application.   


## Usage

First create an instance of PiwikConfig to configure the tracker. Mandatory input
is the name of the server that hosts Piwik. 

```
  PiwikConfig config = PiwikConfig.builder().addIdSite("1")
	    			.host("mypiwik.host.com")
	    			.authToken("ffffffffffffffffffffff")
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
  tracker.sendAsync(request);
```

When the application sends asynchronous requests the http library ([OkHttp](http://square.github.io/okhttp/)) 
internally starts an ExecutorService. To properly shutdown this service an application can call the 
shutdown method: ```tracker.shutdown();```


### Tracking HTTP API
For more information about the supported request parameters see the official documentation:    
https://developer.piwik.org/api-reference/tracking-api



## Maven

The library is hosted on the Central Maven Repository

```
	<dependency>
		<groupId>ch.rasc</groupId>
		<artifactId>piwik-tracking</artifactId>
		<version>1.0.3</version>
	</dependency>
```

## Changelog

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
