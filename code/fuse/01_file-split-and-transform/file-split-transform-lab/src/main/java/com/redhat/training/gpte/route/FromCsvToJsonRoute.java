package com.redhat.training.gpte.route;

import static org.apache.camel.LoggingLevel.INFO;

import org.acme.Customer;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;

public class FromCsvToJsonRoute extends RouteBuilder {

  @EndpointInject(ref = "inputFile")
  Endpoint sourceUri;

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
    //From CSV to Customer
    from(sourceUri).routeId("dataformat")
      .convertBodyTo(String.class)
      .log(">> Reading ${file:onlyname} : ${body}")
      .split(bodyAs(String.class).tokenize("\n"))
      .log(">> Processing line : ${body}")
      .unmarshal(df)
      .log(">> Parsing line to Customer : ${body}")
      .to("direct:csv2json");
    //From Customer to Json
    from("direct:csv2json")
      .to("dozer:csv2json?mappingFile=transformation.xml&targetModel=org.globex.Account")
      .log(">> Transformation Customer to Account ${body}")
      .marshal().json(JsonLibrary.Jackson)
      .to("file://src/data/outbox?fileName=account-${property.CamelSplitIndex}.json");
    //@formatter:on
  }

}
