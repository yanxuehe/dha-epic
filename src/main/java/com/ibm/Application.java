/**
 * Copyright 2005-2018 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ibm;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.camel.Exchange;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;

import static org.apache.camel.component.hl7.HL7.convertLFToCR;
import static org.apache.camel.model.rest.RestParamType.body;
import static org.apache.camel.model.rest.RestParamType.query;

/**
 * A spring-boot application that includes a Camel route builder to setup the Camel routes
 */
@SpringBootApplication
@ImportResource({"classpath:spring/amq.xml"})
@EnableJpaAuditing
public class Application {

    @Autowired
    private Environment env;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            builder
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .indentOutput(true)
                    .simpleDateFormat("yyyy-MM-dd HH:mm:ss");
        };
    }

    // must have a main method spring-boot can run
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Component
    class RestApi extends SpringRouteBuilder {

        @Override
        public void configure() throws Exception {

            // getContext().setLogMask(true);
            // getContext().setMessageHistory(true);

            /*
            errorHandler(
                    defaultErrorHandler().logExhausted(true).logExhaustedMessageHistory(true)
            );
             */

            restConfiguration()
                    .contextPath("/dha").apiContextPath("/api-doc")
                    .apiProperty("api.title", "DHA EPIC Rest APIs")
                    .apiProperty("api.version", "1.0")
                    .apiProperty("cors", "true")
                    .apiProperty("host", "api-dha-epic.e4ff.pro-eu-west-1.openshiftapps.com")
                    .enableCORS(true)
                    .corsHeaderProperty("Access-Control-Allow-Origin", "*")
                    .corsHeaderProperty("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, CONNECT, PATCH")
                    .corsHeaderProperty("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers")
                    .host("0.0.0.0")
                    .port(8080);
            // .host("abc.com")
            // env.getProperty("server.port", "9001")
            //.bindingMode(RestBindingMode.auto);

            rest("/patients").description("Patients Service")
                    .get("/").description("The list of all the patients")
                    .outType(Patient[].class).produces("application/json, application/xml")
                    .param().name("user_key").type(query).defaultValue("Your_User_Key").required(true).description("the user key").endParam()
                    .responseMessage().code(200).endResponseMessage()
                    .route().to("direct:allPatients").endRest()
                    .post("/registry").description("Registry a Patient")
                    .outType(Patient.class).consumes("text/html").produces("application/json, application/xml").type(String.class)
                    .param().name("body").type(body).required(true).description("the patient in hl7 format").endParam()
                    .param().name("user_key").type(query).defaultValue("Your_User_Key").required(true).description("the user key").endParam()
                    .responseMessage().code(202).endResponseMessage().route()
                    .to("direct:registryPatient").endRest();

            from("direct:registryPatient").wireTap("amq:queue:PATIENTS_REGISTRY").to("direct:patientRegistered");

            from("direct:patientRegistered")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                    .setBody(constant("Patient Accepted"));

            from("direct:allPatients")
                    .log("headers : ${headers}")
                    .bean(Database.class, "allPatients")
                    .to("direct:response");

            from("direct:convertLFToCR")
                    //.log("delay 5 seconds")
                    //.delay(5000)
                    .log("Accepted body ${body}")
                    .log("converting LFT 2 CR")
                    .transform(convertLFToCR())
                    .marshal().string()
                    .log("headers ${headers}")
                    .log("Converted body ${body}")
                    .to("direct:processHL7Patient");

            from("direct:processHL7Patient")
                    .unmarshal().hl7()
                    .to("direct:newName");

            from("direct:newName")
                    .bean(HL7MessageProcessor.class, "newName(${body})")
                    .to("direct:getPatient");

            from("direct:getPatient")
                    .bean(HL7MessageProcessor.class, "getPatient")
                    .to("direct:persistPatient");

            from("direct:persistPatient")
                    //.bean(Database.class, "registry(${body})")
                    .to("jpa:com.ibm.Patient")
                    //.to("direct:response")
                    //.wireTap("direct:websocket")
                    .marshal().json(JsonLibrary.Jackson)
                    .log("Inserted new patient ${body}")
                    .to("amq:queue:NEW_PATIENTS")
                    .wireTap("direct:websocket");

            from("direct:websocket").log("put ${body} to websocket").to("websocket:newPatients?sendToAll=true");

            from("amq:queue:PATIENTS_REGISTRY").to("direct:convertLFToCR");

            from("direct:response")
                    .choice()
                    .when(header("Accept").contains("application/xml"))
                    .marshal().jacksonxml().endChoice()
                    .when(header("Accept").contains("application/json"))
                    .marshal().json(JsonLibrary.Jackson).endChoice()
                    .otherwise().marshal().json(JsonLibrary.Jackson);

        }
    }

}
