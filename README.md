# jfall-finagle

TODO:

* Create slide deck
* User story for failover [Effi]
* User story for load balancing [Eggie]
* User story for retry


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



