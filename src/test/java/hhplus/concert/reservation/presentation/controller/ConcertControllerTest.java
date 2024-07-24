package hhplus.concert.reservation.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.concert.reservation.application.concert.dto.ConcertScheduleDTO;
import hhplus.concert.reservation.application.concert.usecase.ConcertUsecase;
import hhplus.concert.reservation.application.token.usecase.TokenUsecase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class ConcertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConcertUsecase concertUsecase;

    @MockBean
    private TokenUsecase tokenUsecase;

    @Test
    public void getConcertSchedule_success() throws Exception {

        // Mocking concertUsecase의 getAvailableDates 메서드
        long tokenId = 1L;
        List<ConcertScheduleDTO> mockConcertScheduleList = new ArrayList<>();
        mockConcertScheduleList.add(new ConcertScheduleDTO(1L, 300L, LocalDate.of(2024,8,1)));
        mockConcertScheduleList.add(new ConcertScheduleDTO(2L, 500L, LocalDate.of(2024,8,12)));
        when(concertUsecase.getAvailableDates(anyLong())).thenReturn(mockConcertScheduleList);

        // 인터셉터 헤더 검증 통과
        when(tokenUsecase.isActiveToken(anyLong())).thenReturn(true);

        // 테스트 요청
        mockMvc.perform(MockMvcRequestBuilders.get("/api/concerts/1")
                        .header("Authorization", tokenId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].concertScheduleId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].seatCount").value(300))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].concertDate").value("2024-08-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].concertScheduleId").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].seatCount").value(500))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].concertDate").value("2024-08-12"));
    }

    @Test
    public void getConcertSchedule_fail() throws Exception {

        // Mocking concertUsecase의 getAvailableDates 메서드
        long tokenId = 1L;
        List<ConcertScheduleDTO> mockConcertScheduleList = new ArrayList<>();
        mockConcertScheduleList.add(new ConcertScheduleDTO(1L, 300L, LocalDate.of(2024,8,1)));
        mockConcertScheduleList.add(new ConcertScheduleDTO(2L, 500L, LocalDate.of(2024,8,12)));
        when(concertUsecase.getAvailableDates(anyLong())).thenReturn(mockConcertScheduleList);

        // 인터셉터 헤더 검증 실패
        when(tokenUsecase.isActiveToken(anyLong())).thenReturn(false);

        // 테스트 요청
        mockMvc.perform(MockMvcRequestBuilders.get("/api/concerts/1")
                        .header("Authorization", tokenId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}
