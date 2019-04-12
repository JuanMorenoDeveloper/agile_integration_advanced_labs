package org.fuse.usecase;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.training.gpte.springboot.Application;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisableJmx(true)
@MockEndpoints
public class ValidateTransformationTest extends CamelTestSupport {

  @Autowired
  private CamelContext context;

  @EndpointInject(uri = "mock:csv2json-test-output")
  private MockEndpoint resultEndpoint;

  @Produce(uri = "direct:csv2json-test-input")
  private ProducerTemplate startEndpoint;

  @Test
  public void transform() throws Exception {
    resultEndpoint.expectedMessageCount(1);
    resultEndpoint
        .expectedBodiesReceived(jsonUnprettyPrint(readFile("src/test/data/account.json")));

    startEndpoint.sendBody(readFile("src/test/data/customer.csv"));

    resultEndpoint.assertIsSatisfied();
  }

  @Override
  protected RouteBuilder createRouteBuilder() throws Exception {
    return new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        //@formatter:off
        from("direct:csv2json-test-input")
          .log(">>> Before transformation ${body}")
          .to("direct:csv2json")
          .to("mock:csv2json-test-output")
          .log(">>> After transformation ${body}");
        //@formatter:on
      }
    };
  }

  private String readFile(String filePath) throws Exception {
    String content;
    try (FileInputStream fis = new FileInputStream(filePath)) {
      content = context.getTypeConverter().convertTo(String.class, fis);
    }
    return content;
  }

  private String jsonUnprettyPrint(String jsonString) throws JsonProcessingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
    JsonNode node = mapper.readTree(jsonString);
    return node.toString();
  }

  @Override
  public boolean isUseDebugger() {
    return true;
  }
}
