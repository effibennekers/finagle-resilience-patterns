# Three resilience patterns out-of-the-box with Twitter's Finagle HTTP client
## Eggie van Buiten & Effi Bennekers

---

## Introduction

|  |  |
| - | - |
| <img src="images/Effi-Bennekers-2.0-280x280.jpg"> | <img src="images/Eggie-van-Buiten-280x280.jpg"> |
| Effi Bennekers | Eggie van Buiten |
| IT Chapter Lead<br/>Engineering @ ING | Developer @ ING |

^^^

<img src="images/brouw_effi.jpg">

^^^

<img src="images/guitar.jpg" width="40%">

---

<img src="images/24-7.jpg">

^^^

## The challenge

<img src="images/1103-Free-Clipart-Of-Chain-Links.jpg">

Success rate of 0.99 ^ 100 = 0.37

^^^

### Micro services  (logical)

<img src="images/microservice-landscape.jpeg">

^^^

### Micro services  (physical)

<img src="images/datacenter.jpeg">

^^^

### Resilience

Wikipedia: The ability to provide and maintain an acceptable level of service in the face of faults and challenges to normal operation.
 
Note:
- "I'm going to ask you a question. Show of hands please: who thinks a service level of 24/7 is feasible?"
- Het probleem schetsen dat 100% uptime niet te halen is

^^^

### Weather API use case

<img src="images/Setup_Initial.png">

* One instance of the client API
* Two instances of the weather service API
* One instance of the old weather service API

Note:
Beschrijf de case
1 Website
We hebben 1 instance van een client api. Mobiele telefoon, weerstation, in dit geval api voor web client. 
We hebben 3 instances van een server api die weerrapporten levert
RPI 1) Weather service 2.0
RPI 2) Weather service 2.0
RPI 3) Old Weather service 1.0 (no wind) we do not use this anymore, but it is still running.

^^^

### Client code and setup

```java
@GetMapping(value = "/api/weather")
public ResponseEntity<String> loadbalance() {
  HttpGet get = new HttpGet("http://weather1:8080/weather");
  CloseableHttpResponse response = httpClient.execute(get);
  int statusCode = response.getStatusLine().getStatusCode();
  if (statusCode == 200) {
    return ResponseEntity.ok(getContentString(response));
  } else {
    return ResponseEntity.status(statusCode).build();
  }
}
```

^^^

### Finagle

From https://twitter.github.io/finagle/:

Finagle is an extensible RPC system for the JVM, used to construct high-concurrency servers. Finagle implements uniform client and server APIs for several protocols, and is designed for high performance and concurrency. Most of Finagle’s code is protocol agnostic, simplifying the implementation of new protocols.

---

## Load balancing

Weather API 2.0:

```json
{
    "condition": "...",
    "temperature": "...",
    "windForce": "...",
    "windDirection": "..."
}
```

^^^

### Load balancing

<img src="images/Setup_Loadbalancing_1.png" width="500px">

^^^

### Load balancing

<img src="images/Setup_Loadbalancing_2.png" width="500px">

^^^

### Load balancing 
#### client setup

```java
    private Service<Request, Response> client;

    public FinagleLoadbalanceController() {
        String connectionString =
          "weather1:8080,weather2:8080";
        client =
          HostFilter$.MODULE$.client()
          .newService(connectionString, "loadbalancer");
    }
```

^^^

### Load balancing 
#### send request 

```java
@GetMapping(value = "/api/finagle/loadbalancing")
CompletableFuture<ResponseEntity<String>> loadbalance(){
    
    Request request = Request.apply(Method.Get(), "/weather");
    request.host("localhost");

    Future<Response> futureResponse = client.apply(request);
    return toSpringResponse(futureResponse);
}
```

---

### Retry

<img src="images/Setup_Retry.png" width="500px">

^^^

## Retry
#### client setup

```java
private Service<Request, Response> client;

public FinagleRetryController() {
  RetryFilter<Request, Response> retryFilter =
    new RetryFilter<>(
      createRetryPolicy(),
      DefaultTimer$.MODULE$.twitter(),
      NullStatsReceiver$.MODULE$,
      RetryBudget$.MODULE$.apply());

  client =
   retryFilter.andThen(
        HostFilter$.MODULE$.client()
          .newService("weather1:8080", "retry"));
}
```

^^^

## Retry
#### retry policy

```java
SimpleRetryPolicy<...> createPolicy() {
 return
   new SimpleRetryPolicy<Tuple2<Request, Try<Response>>>(){
     public Duration backoffAt(int retry) {
        return Duration.fromMilliseconds(retry * 10);
     }
     
     public boolean shouldRetry
       (Tuple2<Request, Try<Response>> requestTryResponse) {
         Try<Response> tryResponse = requestTryResponse._2;
         return tryResponse.isReturn() &&
                tryResponse.get().getStatusCode() == 404;
     }
   };
}
```

^^^

## Retry
#### send request
```java
@GetMapping("/api/finagle/retry")
CompletableFuture<ResponseEntity<String>> getRetry() {
    Request request = Request.apply(Method.Get(), "/weather");
    request.host("localhost");

    Future<Response> futureResponse = client.apply(request);
    return toSpringResponse(futureResponse, httpServletResponse);
}
```

---

## Failover

Weather API 1.0:

```json
{
    "condition": "...",
    "temperature": "..."
}
```

^^^

### Failover

<img src="images/Setup_Failover_1.png" width="500px">

^^^

### Failover

<img src="images/Setup_Failover_2.png" width="500px">

^^^

### Failover
#### client setup

```java
    private Service<Request, Response> primaryClient;
    private Service<Request, Response> secondaryClient;

    public FinagleFailoverController() {
        primaryClient = HostFilter$.MODULE$.client()
               .withSessionQualifier().noFailFast()
               .newService("weather1:8080,weather2:8080",
                           "primary");

        secondaryClient = HostFilter$.MODULE$.client()
                 .newService("oldweather:8080", "secondary");
    }
```

^^^

### Failover
#### send request

```java
@GetMapping("/api/finagle/failover")
CompletableFuture<ResponseEntity<String>> getFailover() {
  Request primaryRequest = Request.apply( // ...
  Future<Try<Response>> tryableFutureResponse =
    primaryClient.apply(primaryRequest).liftToTry();
  
  Future<Response> futureResponse =
    tryableFutureResponse.flatMap(tryResponse -> {
      if (isValidResponse(tryResponse)) {
        return Future.value(tryResponse.get());
      } else {
        Request secondaryRequest = Request.apply( / ...
        return secondaryClient.apply(secondaryRequest);
      }
   });
  return toSpringResponse(futureResponse);
}
```


---

## Recap

* Resilience
* Load balancing
* Retry
* Failover
* Finagle

Note:
So if you take your uptime seriously and want a solution fast, use Finagle! 

---

## Questions?

Effi Bennekers
Efraim.Bennekers@ing.nl
https://www.linkedin.com/in/effibennekers/
@ebennekers

Eggie van Buiten
Eggie.van.Buiten@ingbank.com
