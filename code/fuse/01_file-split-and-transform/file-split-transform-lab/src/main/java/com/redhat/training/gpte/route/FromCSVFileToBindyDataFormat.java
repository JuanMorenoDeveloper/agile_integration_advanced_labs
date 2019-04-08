package com.redhat.training.gpte.route;

import static org.apache.camel.LoggingLevel.INFO;

import org.acme.Customer;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;

public class FromCSVFileToBindyDataFormat extends RouteBuilder {

  @EndpointInject(ref = "inputFile")
  Endpoint sourceUri;

  // @EndpointInject(ref = "outputFile")
  // Endpoint outputUri;

  @EndpointInject(ref = "errorFile")
  Endpoint errorUri;

  @Override
  public void configure() throws Exception {
    //@formatter:off
    //Marshal exception handling
    onException(IllegalArgumentException.class).routeId("unmarshal-exception")
      .maximumRedeliveries(0).handled(true)
      .log(INFO, "%% marshal-exception handled.").to(errorUri);
    //Data format handler
    BindyCsvDataFormat df = new BindyCsvDataFormat(Customer.class);
    //From CSV to Object
    from(sourceUri).routeId("dataformat")
      .convertBodyTo(String.class)
      .log(">> Reading ${file:onlyname} : ${body}")
      .split(bodyAs(String.class).tokenize("\n"))
      .unmarshal(df)
      .log("Conversion : ${body}")
      .to("direct:csv2json");
    //From Object to Json
    from("direct:csv2json")
    .log("json-transformation")
    .to("dozer:csv2json?mappingFile=transformation.xml&targetModel=org.globex.Account")
    .log("After transformation ${body}")
    .marshal().json(JsonLibrary.Jackson)
    .to("file://src/data/outbox?fileName=account-${property.CamelSplitIndex}.json");
    //@formatter:on
  }

}
