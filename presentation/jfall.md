# Three resilience patterns out-of-the-box with Twitter's Finagle HTTP client
## Eggie van Buiten & Effi Bennekers

---

## Agenda
* Introductie
* The Challenge
* Load balancing
* Retry
* Failover
* Contact information and questions

*Dit laten we weg in de uiteindelijke presentatie*

---

## Introduction

|  |  |  |
|: |: |: |
| <img src="images/Effi-Bennekers-2.0-280x280.jpg"> | Effi Bennekers | IT Chapter Lead Engineering @ ING |
| <img src="images/Eggie-van-Buiten-280x280.jpg"> | Eggie van Buiten | Developer @ ING |

*Dit kan nog wel iets leuker!*

---

## The challenge
* Kaput
* No kaput

Note:
- Hier notities....

^^^

### Kaput
* Yesyes

^^^

### No kaput
* Nono

---

## Load balancing

```java
@RequestMapping("/loadbalancing")
public String getLoadbalancing() {
    final long number = counter.incrementAndGet();
    simulateHeavyProcessing();
    return String.format("%d loadbalancing example from %s",
        number, serviceName);
}
```

---

## Retry

---

## Failover

---

## Key takeaways
* Finagle is cool

---

## Contact information and questions
