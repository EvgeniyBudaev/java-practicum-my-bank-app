import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Withdraw fails with 409 when not enough funds"
    request {
        method POST()
        urlPath("/internal/accounts/oleg/withdraw")
        headers {
            contentType(applicationJson())
        }
        body(
                amount: 999999
        )
    }
    response {
        status CONFLICT()
        headers {
            contentType(applicationJson())
        }
        body(
                errors: ["Недостаточно средств на счету"],
                message: $(consumer(anyNonBlankString()), producer("Conflict"))
        )
    }
}
