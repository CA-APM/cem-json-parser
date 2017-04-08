# Generic JSON Plugin for CEM 4.5.6 or higher

## Purpose

Parse nested JSON Objects passed in HTTP requests and/or responses and create parameters which CEM can match/record.
Note: HTTP Header "Content-type" must contain the keyword "json" or the plugin will ignore it.

This code is meant as an example. You can use it as a template and adapt it to your needs, e.g. only promote the json fields that you need for transaction identification as plugin parameters.

## Installation

See [CEM documentation on installation of CEM Http Analyzer plugin](https://docops.ca.com/ca-apm/10-5/en/extending/transaction-definition/identifying-transactions-using-the-http-analyzer-plug-in/process-for-deploying-the-http-analyzer-plug-in/configuring-an-http-analyzer-plug-in).
Note: It is recommended in very high load environments that the URL Path filter is used to limit the plugin only to those pages which will contain the JSON posts.

## Setting up your development environment
* Clone this github project to your desktop
* (Optional) Import it into your IDE
* [Download the HTTP Analyzer Plug-in SDK](https://docops.ca.com/ca-apm/10-5/en/extending/transaction-definition/identifying-transactions-using-the-http-analyzer-plug-in/process-for-deploying-the-http-analyzer-plug-in/download-the-http-analyzer-plug-in-sdk),  [install it as maven dependency](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html) and update `json-parser-pom/pom.xml` with the dependency information
* Run `mvn clean package` from the command line or your IDE

## Verifying Functionality
Perform a "Recording" of the page containing the JSON objects in the HTTP requests. There will be "Plugin" parameter created for each object with a value.
Optionally: Turn on the TIM log to trace HTTP components and HTTP parameters.  You can use a URL Path filter that matches the Plugin setting to eliminate noise in the Log.


## Version History

Version | Author | Comment
--------|--------|--------
1.0 | Corey Cohen | Initial Release
1.1 | Corey Cohen | Created three different jars for request, response or both.

## Disclaimer

This document and associated tools are made available from the
CA/Wily Community Site as examples and provided at no charge as a
courtesy to the CA/Wily Community at large. This resource may require
modification for use in your environment. However, please note that
this resource is not supported by CA/Wily Technology, Inc. and
inclusion in this site should not be construed to be an endorsement
or recommendation by CA/Wily. These utilities are not covered by the
CA/Wily Technology software license agreement and there is no explicit
or implied warranty from Wily Technology, Inc. They can be used and
distributed freely amongst the CA/Wily Community, but not sold. As
such, they are unsupported software, provided as is without warranty
of any kind, express or implied, including but not limited to
warranties of merchantability and fitness for a particular purpose.
CA/Wily does not warrant that this resource will meet your requirements
or that the operation of the resource will be uninterrupted or
error free or that any defects will be corrected. The use of this
resource implies that you understand and agree to the terms listed
herein.
Although these utilities are unsupported, please let us know if you
have any problems or questions by adding a comment to the Community
Site area where the resource is located so that the Author(s) may
attempt to address the issue or question.  Any requests for assistance
to CA/Wily Support regarding this tool may be routed to the original
author if known at the time, with no guarantees of notification,
delivery, closure, or answer by CA/Wily Support
