import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Get current user's account"
    request {
        method GET()
        urlPath("/api/accounts/me")
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                name: $(consumer("testuser"), producer("testuser")),
                birthdate: $(consumer("1990-01-01"), producer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}'))),
                sum: $(consumer(0), producer(anyNumber()))
        )
    }
}
