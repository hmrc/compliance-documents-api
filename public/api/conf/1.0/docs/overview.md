VAT Repayments Info API will support the information transfer of VAT repayment data between SAS and Electronic Folders for use by Case Workers and Contact Centre staff.

## Project Summary

The Protect Connect Project aims to reduce the load and dependency on the current Connect Platform which is inflexible in meeting changing business requirements due to its bespoke design. Also, because of growth of data volumes and restrictions on expanding the current platform, system performance has degraded over time and is directly impacting HMRC's ability to identify non-compliance and respond effectively to business demands due to latency and capacity issues. Therefore the Protect Connect Project will migrate the TRUCE service and parts of Connect to a new service provider.


The chosen provider is SAS Software Inc and the service will be provided as a PaaS hosted on AWS. The SAS solution includes SAS Visual Investigator which will be used by operators to manage risk alerts raised as the result of risk rules. Risk alerts which are deemed to require further, more complex investigation &/or contact with the customer will be transferred to Caseflow.


###Protect Connect documents
In the Protect Connect project, the property values required are:
  - In the URI, the dynamic path parameter `{document-id}` must be a numeric string starting at 810000000
  - Only the object `ef` within the `classIndex` must be supplied; do not use the alternative objects `nReg` and `pReg`
  - The property `doctype` must be set to `UCRE`
  - The property `docBinaryRef` must be the same as the `{document-id}` URI path parameter
  - Only `docBinaryType` values `pdf` and `html` are to be used
  - The property `creatingUser` must be set to `PROTCONN`
  - The property `locationCode` must be set to `250`
