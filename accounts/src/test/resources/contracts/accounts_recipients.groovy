import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Get list of transfer recipients"
    request {
        method GET()
        urlPath("/api/accounts/recipients")
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
                [
                        login: $(consumer("oleg"), producer(anyNonBlankString())),
                        name : $(consumer("Олег"), producer(anyNonBlankString()))
                ]
        ])
    }
}
