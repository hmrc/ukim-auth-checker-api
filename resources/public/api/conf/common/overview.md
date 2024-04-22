# Overview
This API allows Fast Parcel Operators (FPOs) and 3rd party users to check if the holders of provided EORI Numbers have a valid UKIM authorisation.
The aim is to avoid possible rejections of goods moving through from GB-NI, which could physically stop the B2B parcels.
The API is based on REST principles with a single POST method endpoint that returns data in JSON format. It uses standard HTTP error response codes. 

## Errors
We use standard HTTP status codes to show whether an API request succeeded or not. They are usually in the range:

200 if it succeeded

400 for Validation failure

405 for Method not allowed
        
406 for Not acceptable

429 for request in excess of rate limit
          
500 for Internal Server Error

Errors specific to each API are shown in the Endpoints section, under Response. See our reference guide for more on errors.

## Testing
You can use the HMRC Developer Sandbox to test the API. The Sandbox is an enhanced testing service that functions as a simulator of HMRC’s production environment.

## Versioning
When an API changes in a way that is backwards-incompatible, we increase the version number of the API. See our reference guide for more on versioning.
