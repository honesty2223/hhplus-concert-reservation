package hhplus.concert.reservation.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "고객 Controller", description = "잔액 조회 API, 잔액 충전 API")
public class CustomerController {

    Map<String, Object> response = new HashMap<>();

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomer(@PathVariable int customerId) {

        // Check if user exists in mock data
        if (customerId != 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        response.put("customerId", 1);
        response.put("customerName", "스프링");
        response.put("point", 1500);
        response.put("tokenId", "");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/customer/point")
    public ResponseEntity<?> chargePoint(@RequestBody Map<String, Object> request) {

        int customerId = (int) request.get("customerId");
        int amount = (int) request.get("amount");

        // Validate request data
        if (customerId != 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        // Update user balance
        response.put("customerId", 1);
        response.put("point", 1500 + amount);

        return ResponseEntity.ok(response);
    }
}
