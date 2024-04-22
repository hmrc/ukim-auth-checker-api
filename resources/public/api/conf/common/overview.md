# Overview
This is a placeholder overview.md for a new API Microservice repository

## Errors
We use standard HTTP status codes to show whether an API request succeeded or not. They are usually in the range:

200 to 299 if it succeeded, including code 202 if it was accepted by an API that needs to wait for further action
400 to 499 if it failed because of a client error by your application
500 to 599 if it failed because of an error on our server
Errors specific to each API are shown in the Endpoints section, under Response. See our reference guide for more on errors.

## Testing
You can use the HMRC Developer Sandbox to test the API. The Sandbox is an enhanced testing service that functions as a simulator of HMRC’s production environment.

## Versioning
When an API changes in a way that is backwards-incompatible, we increase the version number of the API. See our reference guide for more on versioning.
