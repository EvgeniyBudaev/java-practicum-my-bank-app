import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Deposit to account succeeds"
    request {
        method POST()
        urlPath("/internal/accounts/testuser/deposit")
        headers {
            contentType(applicationJson())
        }
        body(
                amount: 500
        )
    }
    response {
        status OK()
    }
}
