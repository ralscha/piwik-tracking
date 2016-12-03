[![Build Status](https://api.travis-ci.org/ralscha/piwik-tracking.png)](https://travis-ci.org/ralscha/piwik-tracking)

piwik-tracking is a library that helps sending tracking requests to a [Piwik](https://piwik.org/) server 
from a Java application.   


## Usage

First create an instance of PiwikConfig to configure the tracker. Mandatory inputs 
are the name of the server that hosts Piwik and the authentication token. 

```
  PiwikConfig config = PiwikConfig.builder().addIdSite("1")
	    			.host("mypiwik.host.com").authToken("ffffffffffffffffffffffffffffffff")
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
		<version>1.0.0</version>
	</dependency>
```

## Changelog

### 1.0.0 - December 3, 2016
  * Initial release


## License
Code released under [the Apache license](http://www.apache.org/licenses/).
