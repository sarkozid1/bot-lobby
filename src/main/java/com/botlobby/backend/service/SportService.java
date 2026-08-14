package com.botlobby.backend.service;

import com.botlobby.backend.dto.sport.SportResponse;
import com.botlobby.backend.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SportService {

    private final SportRepository sportRepository;

    public List<SportResponse> findAll() {
        return sportRepository.findAll().stream()
                .map(SportResponse::from)
                .toList();
    }
}
