package hhplus.concert.reservation.presentation.controller.token;

import hhplus.concert.reservation.application.token.dto.TokenDTO;
import hhplus.concert.reservation.application.token.usecase.TokenUsecase;
import hhplus.concert.reservation.presentation.controller.token.request.TokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tokens")
@Tag(name = "대기열 토큰 Controller", description = "토큰 발급(콘서트 대기열 참가) API, 콘서트 대기열 조회 API")
public class TokenController {

    private final TokenUsecase tokenUsecase;

    public TokenController(TokenUsecase tokenUsecase) {
        this.tokenUsecase = tokenUsecase;
    }

    /**
     * 토큰 발급(콘서트 대기열 참가) API
     *
     * @return 생성된 토큰(대기열) 정보를 포함한 응답
     */
    @Operation(summary = "토큰 발급(콘서트 대기열 참가)")
    @PostMapping("/generate")
    public ResponseEntity<TokenDTO> generateNewToken(@RequestBody TokenRequest request) {
        TokenDTO tokenDTO = tokenUsecase.generateNewToken(request.getCustomerId(), request.getConcertId());
        return ResponseEntity.ok(tokenDTO);
    }

    /**
     * 콘서트 대기열 조회 API
     *
     * @param customerId 고객 ID
     * @param concertId 콘서트 ID
     * @return 토큰 정보(대기열) 를 포함한 응답
     */
    @Operation(summary = "콘서트 대기열 조회")
    @GetMapping("/check")
    public ResponseEntity<TokenDTO> checkToken(@RequestParam("customerId") long customerId, @RequestParam("concertId") long concertId) {
        TokenDTO tokenDTO = tokenUsecase.checkToken(customerId, concertId);
        return ResponseEntity.ok(tokenDTO);
    }
}
