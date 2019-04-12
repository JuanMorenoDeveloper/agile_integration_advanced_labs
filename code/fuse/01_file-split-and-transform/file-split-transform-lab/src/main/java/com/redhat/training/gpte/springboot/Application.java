/*
 * Copyright 2016 Red Hat, Inc. <p> Red Hat licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at <p> http://www.apache.org/licenses/LICENSE-2.0 <p> Unless
 * required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.redhat.training.gpte.springboot;


import java.util.HashMap;
import java.util.Map;

import org.acme.Customer;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.redhat.training.gpte.*")
// load regular Spring XML file from the classpath that contains the Camel XML DSL
// @ImportResource({"classpath:spring/camel-context.xml"})
public class Application {

  /**
   * A main method to start this application.
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public CamelContext camelContext() throws Exception {
    SpringCamelContext context = new SpringCamelContext();
    DataFormatDefinition csv = new DataFormatDefinition(bindyDataFormat());
    DataFormatDefinition json = new DataFormatDefinition(new JacksonDataFormat());
    Map<String, DataFormatDefinition> dataFormats = new HashMap<>();
    dataFormats.put("csv", csv);
    dataFormats.put("json", json);
    context.setDataFormats(dataFormats);
    return context;
  }

  @Bean
  public DataFormat bindyDataFormat() {
    BindyCsvDataFormat df = new BindyCsvDataFormat(Customer.class);
    return df;
  }

  // @Bean
  // public Endpoint getDozerEndpoint() throws Exception {
  // DozerConfiguration config = new DozerConfiguration();
  // config.setSourceModel(Customer.class.getCanonicalName());
  // config.setTargetModel(Account.class.getCanonicalName());
  // config.setMarshalId("json");
  // config.setUnmarshalId("csv");
  // config.setMappingFile("transformation.xml");
  // DozerEndpoint endpoint =
  // new DozerEndpoint("dozer:csv2json2", new DozerComponent(camelContext()), config);
  // endpoint.getId();
  // return endpoint;
  // }

}
