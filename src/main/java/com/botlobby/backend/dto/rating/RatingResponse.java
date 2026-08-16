package com.botlobby.backend.dto.rating;

import com.botlobby.backend.dto.user.UserSummaryResponse;
import com.botlobby.backend.entity.Rating;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record RatingResponse(
        UUID id,
        UUID lobbyId,
        UserSummaryResponse rater,
        UserSummaryResponse ratedUser,
        Integer stars,
        List<String> tags,
        String comment,
        Boolean noShow,
        Instant createdAt
) {

    public static RatingResponse from(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getLobby().getId(),
                UserSummaryResponse.from(rating.getRater()),
                UserSummaryResponse.from(rating.getRatedUser()),
                rating.getStars(),
                new ArrayList<>(rating.getTags()),
                rating.getComment(),
                rating.getNoShow(),
                rating.getCreatedAt()
        );
    }
}
