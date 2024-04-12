---
title: UKIMS API Service Guide
weight: 1
description: Software developers, designers, product owners or business analysts. Processes involved in passing EORI numbers to check UKIM validity
---

# API Service Guide
This initial discovery document shows developers and other users how to use UK Internal Market Scheme (UKIMS) API with their software. Learn about the processes involved in passing EORI numbers to check UKIM validity for Fast Parcel Operators (FPOs) and other third party operators.

## API Overview 

This API allows Fast Parcel Operators (FPOs) and 3rd party users to check if the holders of provided EORI Numbers have a valid UKIM authorisation.
The aim is to avoid possible rejections of goods moving through from GB-NI, which could physically stop the B2B parcels.
    
The API is based on REST principles with a single GET method endpoint that returns data in JSON format. It uses standard HTTP error response codes. Use the API to request the UKIM Authorization Status of between 1 to 3000 EORIs passed as an array. You can use the API to send both small (up to 5MB in size) and large (up to 8MB in size) messages. 
    
**Note:** The API endpoint relates only to Great Britain and Northern Ireland.

### What is an EORI?
The acronym EORI stands for Economic Operators Registration and Identification. It is a unique identification number used by customs authorities throughout the European Union (EU) 12. This system, instituted on July 1, 2009, replaced the older Trader’s Unique Reference Number (TURN). The EORI number plays a critical role in facilitating the import and export of goods both within the EU and with countries outside of it. Whether you’re a business or an individual, understanding the EORI system is crucial if you plan to engage in international trade. For those based in the UK, HM Revenue and Customs (HMRC) are responsible for allocating these numbers.

A breakdown of the EORI number format for UK VAT-registered businesses:

- GB: Indicates that the business is based in the UK.
- 205672212: Represents the business’s VAT Registration Number.
- 000: These three zeros are always added to the end of a UK EORI number 1.

In summary, having an EORI number is essential for anyone involved in international trade, as it allows customs authorities to monitor and track shipments effectively. You’ll need an EORI if you:

## API Status

This version of the UKIMS API:

- supports only the  UKIMS API v1.0
- is currently not ready for testing
- will not be ready for use in production until the service goes live 

### Use the API to:

- Request the UKIM Authorization Status of 1-3000 EORIs passed as an array.
- You can use the API to send both small (up to 5MB in size) and large (up to 8MB in size) messages.

The API endpoint relates only to Great Britain and Northern Ireland.  Eventually, you can also use the HMRC sandbox environment to run tests for Great Britain and Northern Ireland transit movements.

## Developer Setup

To develop using the UKIMS API you must:

- be familiar with HTTP, RESTful services, XML and OAuth2
- be registered as a developer on the HMRC Developer Hub
- add at least one sandbox application on the Developer Hub
- Each application you register will be assigned an HMRC ApplicationId.

You can view all the applications you have registered on the Developer Hub Applications page where you can administer API subscriptions and application credentials.

## Getting started

Making API requests
Before sending any requests to UKIMS API v1.0, you should ensure that you are using in your software:
- the correct URL for the environment and API version number (see below)
- the correct header contents and payload information - see  UKIMS API v1.0 reference
- The base URLs of the sandbox and production environments are as follows:


Sandbox	https://test-api.service.hmrc.gov.uk/customs/uk-internal-market/authorisations

Production	https://api.service.hmrc.gov.uk/customs/uk-internal-market/authorisations

### Validating a collection of EORI numbers
Link to GET method in UKIMS v1.0 Reference Guide here
### Example of a GET request
Example of Curl script with URL and query parameters

```curl

curl --location --request GET 'https://test-api.service.hmrc.gov.uk/customs/uk-internal-market/authorisations' \

--header 'Content-Type: application/text/csv' \
--data-urlencode 'eori=GB123123123123' \
--data-urlencode 'date=2024-02-31' \

```
Example of a succesful response

```code
{
  "date": "2024-02-31",
  "eoris": [
    {
      "eori": "GB123123123123",
      "authorised": true
    }
  ]
}

```
## Error Responses

An introduction to the expected Error Reponses
200
400
404


## Process flows

### Basic Sequence diagrams
Basic sequence diagrams here 


## API rate limiting
Each software house should register a single application with HMRC. This application will be used to identify the software house during the OAuth 2.0 grant flow and will also be used in subsequent per user API calls. We limit the number of requests that each application can make. This protects our backend service against excessive load and encourages real-time API calls over batch processing.

We set limits based on anticipated loads and peaks. Our standard limit is 3 requests per second per application. If you believe that your application will sustain traffic load above this value, contact the SDS Team.
