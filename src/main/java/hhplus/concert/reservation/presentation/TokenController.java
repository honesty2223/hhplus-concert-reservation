package hhplus.concert.reservation.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "대기열 토큰 Controller", description = "토큰 발급 API, 대기열 조회 API")
public class TokenController {

    Map<String, Object> response = new HashMap<>();

    @Operation(summary = "토큰 발급")
    @PostMapping("/token")
    public ResponseEntity<?> issueToken(@RequestBody Map<String, Object> request) {

        int customerId = (int) request.get("customerId");
        int concertId = (int) request.get("concertId");

        // Validate user ID (for simplicity, assume any non-zero integer is valid)
        if (customerId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid user ID");
        }


        // Validate concert ID (for simplicity, assume any non-zero integer is valid)
        if (concertId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid concert ID");
        }

        // Check if a token already exists for this user
        if (customerId == 2) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Token already exists for this user");
        }

        // Generate token
        LocalDateTime now = LocalDateTime.now();
        response.put("tokenId", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");

        // Prepare response
        return ResponseEntity.ok(response);
    }
}
