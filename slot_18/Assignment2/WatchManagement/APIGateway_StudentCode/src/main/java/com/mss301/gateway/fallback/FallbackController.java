package com.mss301.gateway.fallback;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// [TODO-7.2] Target of the circuit breakers when a downstream service cannot answer
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/account")
    public ResponseEntity<Map<String, Object>> account() {
        return unavailable("MSAccount");
    }

    @RequestMapping("/brand")
    public ResponseEntity<Map<String, Object>> brand() {
        return unavailable("MSBrand");
    }

    @RequestMapping("/watch")
    public ResponseEntity<Map<String, Object>> watch() {
        return unavailable("MSWatch");
    }

    private ResponseEntity<Map<String, Object>> unavailable(String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("success", false, "message", service + " is temporarily unavailable"));
    }
}
