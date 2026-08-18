package com.botlobby.backend.service;

import com.botlobby.backend.dto.sport.SportResponse;
import com.botlobby.backend.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// A legegyszerubb service a projektben - egyetlen olvaso muvelet, nincs uzleti szabaly.
@Service
@RequiredArgsConstructor
public class SportService {

    private final SportRepository sportRepository;

    // Az osszes sport lekerdezese, mindegyiket a publikus DTO-jara (SportResponse) alakitva -
    // ezt hasznalja a frontend a "Sport" legordulo mezok feltoltesehez (lobby letrehozasnal,
    // szuresnel, sportprofil felvetelnel).
    public List<SportResponse> findAll() {
        return sportRepository.findAll().stream()
                .map(SportResponse::from)
                .toList();
    }
}
