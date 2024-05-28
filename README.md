
# UKIM Auth Checker Api README

This is a microservice for a public API to allow software developers to integrate UKD checks into their software.

### Prerequisites
- Scala 2.13.12
- Java 11
- sbt > 1.9.7
- [Service Manager]("https://github.com/hmrc/service-manager")

### Development Setup
Run from the console using: sbt run

To run the whole stack, using service manager: sm --start UKIM_ALL

## Highlighted SBT Tasks
| Task | Description | Command |
| ----------- | ----------- | ----------- |
| Test | Runs the standard unit tests | `$ sbt test`|
| it/test | Runs the integration tests | `$ sbt it/test`|

### Helpful information
You can find helpful guides on the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/using-the-hub).

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
