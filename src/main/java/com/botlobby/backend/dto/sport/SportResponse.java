package com.botlobby.backend.dto.sport;

import com.botlobby.backend.entity.Sport;

import java.util.UUID;

// A Sport entitas publikus API-valasz alakja - 1:1 megfelel az entitas mezoinek, mert itt
// nincs semmi erzekeny/belso adat, amit el kellene rejteni.
public record SportResponse(UUID id, String name, String iconUrl) {

    // "Mapper" metodus: entitasbol DTO-t general. Ezt a mintat (from(entitas) -> DTO) szinte
    // minden *Response record kovet ebben a projektben - igy a controller/service reteg
    // sosem ad vissza kozvetlenul JPA entitast a kliensnek.
    public static SportResponse from(Sport sport) {
        return new SportResponse(sport.getId(), sport.getName(), sport.getIconUrl());
    }
}
