package com.botlobby.backend.entity.enums;

// Egy LobbyParticipant (jelentkezes) lehetseges allapotai, az eletciklus sorrendjeben:
//  - PENDING:  a felhasznalo jelentkezett, meg vart a szervezo dontesere.
//  - ACCEPTED: a szervezo elfogadta - o mar tenyleges reszvevo (szamit a letszamba, lathatja
//              a chatet, ertekelheto/ertekelhet a lobby lezarasa utan).
//  - DECLINED: a szervezo elutasitotta.
//  - LEFT:     korabban ACCEPTED volt, de a reszvevo utolag kilepett.
public enum ParticipantStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    LEFT
}
