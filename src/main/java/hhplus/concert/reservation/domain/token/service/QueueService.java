package hhplus.concert.reservation.domain.token.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.concert.reservation.domain.common.CoreException;
import hhplus.concert.reservation.domain.common.ErrorCode;
import hhplus.concert.reservation.domain.token.entity.RedisToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
    private static final String WAITING_QUEUE_NAME = "wait_tokens:";
    private static final String WORKING_QUEUE_NAME = "working_token:";

    private final RedisTemplate<String, Object> redisTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    // 콘서트 대기열 참가
    public RedisToken generateNewToken(long customerId) {
        RedisToken targetToken = null;
        RedisToken newToken = null;
        try{
            targetToken = checkToken(customerId);
        } catch(Exception e) {
            long now = System.currentTimeMillis();
            UUID uuid = UUID.randomUUID();
            // 대기열의 현재 마지막 순번을 Redis에서 가져옴
            ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
            newToken = new RedisToken(customerId, uuid);
            String jsonString = "";
            try {
                // 객체를 JSON 문자열로 변환
                jsonString = objectMapper.writeValueAsString(newToken);
            } catch (JsonProcessingException jpe) {
                log.error("예외 발생 오류 메시지: {}", jpe.getMessage());
            }

            // Redis 리스트에 새로운 토큰 추가
            zSetOps.add(WAITING_QUEUE_NAME, jsonString, now);

            return newToken;
        }

        if(targetToken != null) {
            throw new CoreException(ErrorCode.ALREADY_IN_QUEUE);
        }

        return newToken;
    }

    // 대기열 조회
    public RedisToken checkToken(long customerId) {

        // Redis의 ZSetOperations를 사용하여 ZSet 가져오기
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        log.info("Service == 고객 ID : {}", customerId);

        // 고객의 토큰을 ZSet에서 찾기
        Set<ZSetOperations.TypedTuple<Object>> zSet = zSetOps.rangeWithScores(WAITING_QUEUE_NAME, 0, -1);
        String targetValue = "";
        RedisToken targetToken = null;
        RedisToken newToken = null;
        log.info("zSet 크기 : {}", zSet.size());
        if (zSet != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : zSet) {
                String value = (String) tuple.getValue();
                log.info("value : {}", value);
                try {
                    // JSON 문자열을 파싱하여 원하는 필드를 추출
                    targetToken = objectMapper.readValue(value, RedisToken.class);
                    if (targetToken.getCustomerId() == customerId) {
                        log.info("Service == 찾은 고객 ID : {}", targetToken.getCustomerId());
                        targetValue =  value; // JSON 문자열 반환
                        newToken = targetToken;
                        log.info("target value : {}", targetValue);
                    }
                } catch (IOException e) {
                    log.error("예외 발생 오류 메시지 : {}", e.getMessage());
                }
            }
        }

        long rank = -1;
        if(targetValue.isEmpty()) {
            throw new CoreException(ErrorCode.TOKEN_NOT_FOUND);
        } else {
            rank = zSetOps.rank(WAITING_QUEUE_NAME, targetValue);
        }

        // 토큰 순서를 계산해서 리턴
        return new RedisToken(newToken.getCustomerId(), newToken.getTokenID(), rank);
    }

    // 토큰 활성화 여부 조회
    public boolean isActiveToken(UUID tokenId) {
        // Redis의 ZSetOperations를 사용하여 ZSet 가져오기
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        log.info("Service == 토큰 ID : {}", tokenId);

        // 고객의 토큰을 ZSet에서 찾기
        Set<ZSetOperations.TypedTuple<Object>> zSet = zSetOps.rangeWithScores(WORKING_QUEUE_NAME, 0, -1);
        String targetValue = "";
        RedisToken targetToken = null;
        log.info("zSet 크기 : {}", zSet.size());
        if (zSet != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : zSet) {
                String value = (String) tuple.getValue();
                log.info("value : {}", value);
                try {
                    // JSON 문자열을 파싱하여 원하는 필드를 추출
                    targetToken = objectMapper.readValue(value, RedisToken.class);
                    log.info("UUID : {}", tokenId);
                    log.info("tokenID : {}", targetToken.getTokenID());
                    if (targetToken.getTokenID().equals(tokenId)) {
                        log.info("Service == 찾은 토큰 ID : {}", targetToken.getTokenID());
                        targetValue =  value; // JSON 문자열 반환
                        log.info("target value : {}", targetValue);

                        return true;
                    }
                } catch (IOException e) {
                    log.error("예외 발생 오류 메시지 : {}", e.getMessage());
                }
            }
        }
        return false;
    }

    // 활성화
    public void activeToken(int size) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> zSetQueue = zSetOps.rangeWithScores(WAITING_QUEUE_NAME, 0, -1);
        int emptyActive = 0;
        if(zSetQueue != null) {
            Set<ZSetOperations.TypedTuple<Object>> zSetActive = zSetOps.rangeWithScores(WORKING_QUEUE_NAME, 0, -1);
            if(zSetActive != null) {
                emptyActive = size - zSetActive.size();
                for(int i = 0; i < emptyActive; i++) {
                    ZSetOperations.TypedTuple<Object> minScoreItem = zSetQueue.stream().min(Comparator.comparingDouble(ZSetOperations.TypedTuple::getScore)).orElse(null);
                    zSetOps.add(WORKING_QUEUE_NAME, minScoreItem.getValue(), System.currentTimeMillis());
                    log.info("[토큰 활성화] {}", minScoreItem.getValue());
                    zSetOps.remove(WAITING_QUEUE_NAME, minScoreItem.getValue());
                }
            }
        }
    }

    // 만료
    public void expireToken() {
        // Redis의 ZSetOperations를 사용하여 Active ZSet 가져오기
        long now = System.currentTimeMillis();
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<Object>> zSet = zSetOps.rangeWithScores(WORKING_QUEUE_NAME, 0, -1);

        // 10분 지난 토큰 만료
        if (zSet != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : zSet) {
                String value = (String) tuple.getValue();
                Double score = tuple.getScore() + 60000;
                if(score < now) {
                    zSetOps.remove(WORKING_QUEUE_NAME, value);
                    log.info("[토큰 만료] {}", value);
                }
            }
        }
    }
}
