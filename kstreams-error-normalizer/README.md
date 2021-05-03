# Normalizer Application

An example application which will consume records from multiple application(s) topic(s), normalizing those to a single enterprise-wide schema which can be consumed more easily be a wide range of applications.  

# Challenge

A typical organization has dozens (or hundreds) of applications and microservices which are independently designed and implemented.
In a greenfield, it may be possible to align the entire organize to have a unified error schema. 
However, most scenarios are more brownfield, and will require integrating many systems with newly introduced Kafka Patterns.

Each system is likely to have its own error structure, semantics, and details.
It is much simpler to achieve the ideal of a "Single Pane of Glass" where   

# Design

Every design pattern and its practical implementation has tradeoffs, functional and non-functional requirements.
In Kafka, there are fundamental design and tuning choices which create necessary tradeoffs.
In general, there are tradeoffs between 
 * Throughput vs Latency
 * Strict vs Loose vs No Ordering guarantees
 * At-least-once vs At-most-once delivery guarantees 
 * Reprocessing

## Considerations

### Technical 

What are the non-functional requirements for:
 * End-to-end latency?
 * Throughput?
 * Ordering Guarantees?

What are the functional requirements for:

### Organizational

Who owns the common / platform code?
Is common infrastructure developed in a federated (or loosely-governed) way, or in a centralized (or tightly controlled) way?

Early in the adoption of this pattern there may be only one downstream consumer of errors, which can create headwinds for budgetary support.
One possible approach is to also apply this to "business logic errors", not just technical errors.  

### Operational

Having a single point of failure in any system can be an anti-pattern, depending on SLOs and SLAs.
Generally, monitoring solutions need to have the highest availability to avoid false-positive errors.

There may be concerns about monitoring-the-monitor system.

Multi-region and disaster recovery solutions can make it difficult to create a globally consistent view.
It may be necessary to design for partial segmentation of the error solution.

Error traffic should (generally) be very low volume; however in disaster scenarios, traffic will spike.
The solution will need to be provisioned for worst case situations, but will likely be utilized at a very small percentage of allocated resources.

Consuming records from Kafka is non-destructive, care must be taken to avoid generating false alerts when there are temporary outages of some consumers.
For example, it would be annoying for an alerting system to play catch up on the last hour of error.

## Preconditions

There are N topics which provided error messages.
Error producers leverage the [Dead Letter Queue Pattern](https://www.enterpriseintegrationpatterns.com/patterns/messaging/DeadLetterChannel.html) to publish error records to a Kafka Topic.

## Patterns

The solution applies the [Normalizer Pattern](https://www.enterpriseintegrationpatterns.com/patterns/messaging/Normalizer.html).
This solution takes many forms of data and normalizes the data into a common form.

Secondarily, this applies the [Aggregator Pattern](https://www.enterpriseintegrationpatterns.com/patterns/messaging/Aggregator.html).
This solution takes data from many sources, and produces them to a single destination. 

Upstream solutions should implement the [Dead Letter Queue Pattern](https://www.enterpriseintegrationpatterns.com/patterns/messaging/DeadLetterChannel.html).
This simplest form of this solution relies on upstream producers to provide errors in topics, which the solution consumes and normalizes.

This solution applies something similar to [Correlation Identifier Pattern](https://www.enterpriseintegrationpatterns.com/patterns/messaging/CorrelationIdentifier.html).


## Benefits

The implementation of the producer of the error messages is irrelevant.
The implementation can provide a single source of truth and better manage quality of data for downstream consumers.
Generally speaking, error schemas are pretty stable; the development burden of keeping up with changing upstream should be pretty 

Having a unified view of errors across the entire enterprise makes it possible to gain global perspective on the root causes of errors or problems in individual components of the system.

Onboarding error sources has a low (and diminishing) incremental cost as more systems are onboarded.

Limits and manages complexity of the overall enterprise solution by avoiding many peer-to-peer connections which leads to the "Spaghetti Architecture" anti-pattern.

## Drawbacks

The implementation provides a single point of failure, from a non-technical perspective.

The implementation can be sensitive to changing error schemas/implementation from upstream systems.

In a federated approach, it can be difficult to coordinate the development of the transformation code among many teams.
In a more centralized approach, the challenge of implementation

Monitoring, Logging, and Observability, and alerting solutions are usually optimized for time-series storage of data as it arrives.
This makes it impractical to restate or replay errors in most cases.
Specialized solutions can

If component cannot access a Kafka Cluster, this can lead to false-negatives in error detection.
Upon reconnection, delivery of old errors may lead to false-positives for alerting conditions based on error rates.
Special care in record design (and processing) may be required to avoid such problems.

# About the Implementation

# Setup / Installation

See https://docs.microsoft.com/en-us/dotnet/core/tools/global-tools
```shell
$> brew install nuget
$> nuget
```