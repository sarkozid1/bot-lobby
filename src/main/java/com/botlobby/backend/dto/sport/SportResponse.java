package com.botlobby.backend.dto.sport;

import com.botlobby.backend.entity.Sport;

import java.util.UUID;

public record SportResponse(UUID id, String name, String iconUrl) {

    public static SportResponse from(Sport sport) {
        return new SportResponse(sport.getId(), sport.getName(), sport.getIconUrl());
    }
}
