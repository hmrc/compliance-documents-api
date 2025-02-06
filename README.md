
# compliance-documents-api
Part of the MDTP Investigation & Detection Risking Service (IDRS) APIs

## Running the service
To run the service, in the repository's main directory, run `sbt run` - 7053 is the port the service is intended to run on.
You need to be a standard application to be able to use this API.

### Routes
    POST /vat-repayment-info/{document-id}
    
Validates payload with the schema provided; sends it to the IF if payload matches schema. Requires header:

Path | Content
-----|--------
document-id | a long number, from 0 to 18439999999999999999 inclusive

Header | Content
-------|--------
CorrelationId | UUID v4

    
### Possible responses:
   
  
#### 202

The payload was accepted - the IF returned a 202 status. No body is returned.
  
#### 400

Possible messages:

   * `INVALID_CORRELATION_ID` -> The correlation ID provided with the request was invalid
   
   *  `MISSING_CORRELATION_ID` -> No correlation ID was found in request 
   *  `INVALID_DOCUMENT_ID` -> The document ID provided in the path was invalid
   *  `INVALID_JSON_TYPE` -> The provided payload is Json, but is not a Json object
   *  `INVALID_PAYLOAD` -> The provided payload does not match the schema
      * `MISSING_FIELD` -> An expected field was missing
      * `UNEXPECTED_FIELD` -> An unexpected field was found
      * `INVALID_FIELD` -> A provided, expected field has an invalid value
      
      
#### 401

The request was unable to be authorized - are you a standard application?

#### 500

Internal server error
   
    

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
