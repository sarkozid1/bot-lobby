package com.botlobby.backend.entity.enums;

// Egy felhasznalo tudasszintje egy adott sportban (UserSportProfile.skillLevel), illetve
// egy lobby minimum-kovetelmenye (Lobby.minSkillLevel). Novekvo sorrendben: kezdo -> profi.
// Adatbazisban STRING-kent tarolodik (lasd @Enumerated(EnumType.STRING) a hasznalati helyeken),
// tehat pontosan ezek a nevek jelennek meg a "skill_level"/"min_skill_level" oszlopokban.
public enum SkillLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    PRO
}
