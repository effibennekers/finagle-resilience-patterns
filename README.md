# jfall-finagle

TODO:

* Create slide deck
* User story for failover [Effi]
* User story for load balancing [Eggie]
* User story for retry


## User story load balancing

Create a GET endpoint /loadbalancing

Should return "<counter> loadbalance example from <instancename>"

Question: simulate some processing by a (randomized) sleep?


Scenario:
* Run at two instances (effi and eggie)
* with naive http client, there is no loadbalancing. Should we implement a simple one? (choose random
which URL/port to use?)
* Create/show finagle client. This should be much easier, and show 




## Sample code

* Make code compile again [Eggie](done)

Our API's shoul return a service name in their response, so it is clear by which instance a request
was handled. Suggestion: use our own names for services to make it more lively.

## Slides [Effi]

* Intro: who are we, manage expectations
* Challenges in large scale API/API ..
* What doe we mean with resilience 
* Show error case (should be solved by dealing with failover, loadbalancing, retry)
* Create Finagle pattern to fix one, show solves (part of) error case
* Describe Finagle filter/service 'architecture'
* Afsluiter: drain all power: show fail fast



