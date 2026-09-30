package com.fudn.orderservice;

import com.fudn.orderservice.stub.InventoryStubs;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceApplicationTests {

    @ServiceConnection
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.3.0");

    // Spring Cloud Contract 5.x (Spring Boot 4) da bo @AutoConfigureWireMock,
    // nen tu khoi dong WireMock o port ngau nhien (tuong duong port = 0)
    static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        wireMockServer.resetAll();
    }

    static {
        mySQLContainer.start();
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    // Expose port cua WireMock ra property wiremock.server.port
    // de src/test/resources/application.properties reference
    @DynamicPropertySource
    static void wireMockProperties(DynamicPropertyRegistry registry) {
        registry.add("wiremock.server.port", wireMockServer::port);
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void shouldSubmitOrder() {
        String submitOrderJson = """
                {
                     "skuCode": "iphone_15",
                     "price": 1000,
                     "quantity": 1
                }
                """;

        InventoryStubs.stubInventoryCall("iphone_15", 1);

        var responseBodyString = RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .body().asString();

        assertThat(responseBodyString, Matchers.is("Order Placed Successfully"));
    }

    @Test
    void shouldFailOrderWhenProductIsNotInStock() {
        String submitOrderJson = """
                {
                     "skuCode": "iphone_15",
                     "price": 1000,
                     "quantity": 1000
                }
                """;

        InventoryStubs.stubInventoryCall("iphone_15", 1000, false);

        RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(500);
    }
}
