package com.botlobby.backend.repository;

import com.botlobby.backend.entity.Lobby;
import com.botlobby.backend.entity.enums.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Lasd UserRepository.java a "derived query" minta magyarazataert az egyszeru findBy... metodusokhoz.
// A search(...) metodus viszont, mivel tobb OPCIONALIS szurofeltetelt kell tudnia kombinalni,
// mar nem irhato le egyszeru metodusnevvel - ott kezzel irt (native) SQL kell, lasd lent.
public interface LobbyRepository extends JpaRepository<Lobby, UUID> {

    // Egy adott felhasznalo altal letrehozott/szervezett osszes lobby.
    List<Lobby> findByCreatorId(UUID creatorId);

    // Egy adott varosban, egy adott sporthoz tartozo osszes lobby (jelenleg nincs kozvetlenul
    // hasznalva egyetlen vegpont mogott sem sem - a tenyleges szures a search(...) metoduson at megy).
    List<Lobby> findByCityAndSportId(String city, UUID sportId);

    /**
     * GET /api/lobbies?sport=&city=&date=&skillLevel= mogotti rugalmas szures.
     * Minden parameter opcionalis (null eseten az adott feltetel nem szűr).
     * A "date" (nap) szures ket hatarra (startOfDay/endOfDay) van bontva, ezt a service reteg
     * szamolja ki a kapott LocalDate-bol.
     * <p>
     * Natic SQL + explicit CAST minden parameteren: sima JPQL-lel a PostgreSQL driver
     * "could not determine data type of parameter" (42P18) hibaval elszallt, amikor egy
     * opcionalis parameter (pl. skillLevel enum) mindig null-kent erkezett - Postgres ilyenkor
     * nem tudja kitalalni a tipusat cast nelkul, meg a JPQL-generalta prepared statementben sem.
     */
    @Query(
            value = """
                    SELECT * FROM lobbies l
                    WHERE l.status <> 'CANCELLED'
                      AND (CAST(:sportId AS uuid) IS NULL OR l.sport_id = CAST(:sportId AS uuid))
                      AND (CAST(:city AS varchar) IS NULL OR l.city = CAST(:city AS varchar))
                      AND (CAST(:startOfDay AS timestamp) IS NULL OR l.start_time >= CAST(:startOfDay AS timestamp))
                      AND (CAST(:endOfDay AS timestamp) IS NULL OR l.start_time < CAST(:endOfDay AS timestamp))
                      AND (CAST(:skillLevel AS varchar) IS NULL OR l.min_skill_level = CAST(:skillLevel AS varchar))
                    ORDER BY l.start_time ASC
                    """,
            nativeQuery = true)
    List<Lobby> search(@Param("sportId") UUID sportId,
                        @Param("city") String city,
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay,
                        @Param("skillLevel") String skillLevel);
}
