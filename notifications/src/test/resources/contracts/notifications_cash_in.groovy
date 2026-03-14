import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Send CASH_IN notification"
    request {
        method POST()
        urlPath("/api/notifications")
        headers {
            contentType(applicationJson())
        }
        body(
                type: "CASH_IN",
                amount: 500,
                actorLogin: "user1",
                targetLogin: null,
                occurredAt: $(consumer(anyNonBlankString()), producer("2026-01-01T12:00:00Z"))
        )
    }
    response {
        status OK()
    }
}
