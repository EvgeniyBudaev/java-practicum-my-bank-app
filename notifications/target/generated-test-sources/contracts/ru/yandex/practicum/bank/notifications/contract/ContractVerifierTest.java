package ru.yandex.practicum.bank.notifications.contract;

import ru.yandex.practicum.bank.notifications.contract.BaseContractTest;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import io.restassured.response.ResponseOptions;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;

@SuppressWarnings("rawtypes")
public class ContractVerifierTest extends BaseContractTest {

	@Test
	public void validate_notifications_cash_in() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"type\":\"CASH_IN\",\"amount\":500,\"actorLogin\":\"user1\",\"targetLogin\":null,\"occurredAt\":\"2026-01-01T12:00:00Z\"}");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/api/notifications");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
	}

}
