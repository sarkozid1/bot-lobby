package com.botlobby.backend.entity.enums;

// Egy Lobby lehetseges allapotai. A LobbyService felelos az atmenetekert:
//  - OPEN:      letrehozaskor ez az alapertelmezett, meg lehet jelentkezni.
//  - FULL:      automatikusan beallitodik, amikor az elfogadott (ACCEPTED) reszvevok
//               szama eleri a maxParticipants-ot - onnantol nem lehet tovabb jelentkezni.
//  - CLOSED:    a lobby ideje mar elmult (lezart, tortenelmi allapot).
//  - CANCELLED: a szervezo (creator) manualisan lemondta.
public enum LobbyStatus {
    OPEN,
    FULL,
    CLOSED,
    CANCELLED
}
