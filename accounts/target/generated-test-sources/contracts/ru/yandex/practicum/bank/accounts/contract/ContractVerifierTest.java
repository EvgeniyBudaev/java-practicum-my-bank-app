package ru.yandex.practicum.bank.accounts.contract;

import ru.yandex.practicum.bank.accounts.contract.BaseContractTest;
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
	public void validate_accounts_deposit() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"amount\":500}");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/internal/accounts/testuser/deposit");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
	}

	@Test
	public void validate_accounts_me() throws Exception {
		// given:
			MockMvcRequestSpecification request = given();


		// when:
			ResponseOptions response = given().spec(request)
					.get("/api/accounts/me");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Content-Type")).matches("application/json.*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['name']").isEqualTo("testuser");
			assertThatJson(parsedJson).field("['birthdate']").matches("[0-9]{4}-[0-9]{2}-[0-9]{2}");
			assertThatJson(parsedJson).field("['sum']").matches("-?(\\d*\\.\\d+|\\d+)");
	}

	@Test
	public void validate_accounts_recipients() throws Exception {
		// given:
			MockMvcRequestSpecification request = given();


		// when:
			ResponseOptions response = given().spec(request)
					.get("/api/accounts/recipients");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Content-Type")).matches("application/json.*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).array().contains("['login']").matches("^\\s*\\S[\\S\\s]*");
			assertThatJson(parsedJson).array().contains("['name']").matches("^\\s*\\S[\\S\\s]*");
	}

	@Test
	public void validate_accounts_withdraw_not_enough() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"amount\":999999}");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/internal/accounts/oleg/withdraw");

		// then:
			assertThat(response.statusCode()).isEqualTo(409);
			assertThat(response.header("Content-Type")).matches("application/json.*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).array("['errors']").arrayField().isEqualTo("\u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0441\u0440\u0435\u0434\u0441\u0442\u0432 \u043D\u0430 \u0441\u0447\u0435\u0442\u0443").value();
			assertThatJson(parsedJson).field("['message']").isEqualTo("Conflict");
	}

}
