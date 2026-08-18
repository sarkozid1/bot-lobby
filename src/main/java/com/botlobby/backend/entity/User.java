package com.botlobby.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// JPA entitas - ez az osztaly kozvetlenul a "users" tablat kepezi le. A @Getter/@Setter/
// @NoArgsConstructor/@AllArgsConstructor/@Builder Lombok-annotaciok forditaskor generaljak
// a getter/setter metodusokat, egy ures es egy mindent-parameterkent-varo konstruktort, valamint
// egy builder-mintat (User.builder().name(...).email(...).build()), hogy ne kelljen ezeket kezzel megirni.
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // Elsodleges kulcs. GenerationType.UUID azt jelenti, hogy Java oldalon (Hibernate) generalodik
    // egy veletlenszeru UUID INSERT elott, nem az adatbazis auto-increment mechanizmusaval.
    // updatable = false: ha ez a mezo egyszer be van allitva, kesobbi UPDATE SQL mar nem irhatja felul.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // A felhasznalo megjeleniteni kivant neve (pl. "Kovacs Peter").
    @Column(nullable = false)
    private String name;

    // Bejelentkezesi azonosito is egyben - unique = true miatt az adatbazis maga is kikenyszeriti,
    // hogy ne lehessen ket felhasznalo ugyanazzal az email cimmel (lasd V1 migracio UNIQUE constraint-je).
    @Column(nullable = false, unique = true)
    private String email;

    // SOSEM a nyers jelszo - ez mar BCrypt-tel hash-elt ertek (lasd AuthService.register,
    // ahol a PasswordEncoder.encode(...) hivas allitja elo). Bejelentkezeskor a
    // DaoAuthenticationProvider ezt hasonlitja ossze a bekuldott jelszoval.
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Opcionalis, kulso URL a profilkephez (nincs sajat fajlfeltoltes/tarolas ebben a projektben).
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    // Opcionalis lakhely-varos, szabad szoveg.
    private String city;

    // 1-5 csillag alapu megbizhatosagi pontszam (mint a Rating.stars), alapertelmezetten 5.0. NoShow-nal csokken.
    @Column(name = "reliability_score", nullable = false)
    @Builder.Default
    private Double reliabilityScore = 5.0;

    // Regisztracio idopontja - a lenti @PrePersist metodus tolti fel automatikusan, ha meg ures.
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Egy felhasznalohoz tobb UserSportProfile is tartozhat (pl. "foci - halado", "tenisz - kezdo").
    // mappedBy = "user": a kapcsolat tulajdonosa a UserSportProfile oldalon van (ott van a foreign key
    // oszlop), itt csak a forditott iranyu, kenyelmi navigacio. cascade = ALL + orphanRemoval = true azt
    // jelenti: ha a listabol kiveszunk egy elemet vagy torlodik a User, a hozza tartozo sportprofilok is
    // torlodnek - nincs "arva" UserSportProfile sor a felhasznalo nelkul.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserSportProfile> sportProfiles = new ArrayList<>();

    // Azok a lobbyk, amiket ez a felhasznalo hozott letre (Lobby.creator forditottja).
    // FetchType.LAZY: ez a lista csak akkor tolt be az adatbazisbol, ha ténylegesen hozzaferunk -
    // igy egy sima User lekérdezes nem huz be feleslegesen szaz lobbyt.
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Lobby> createdLobbies = new ArrayList<>();

    // JPA "eletciklus hook": Hibernate automatikusan meghivja kozvetlenul az INSERT elott.
    // Igy nem kell minden hivo helyen (service-ekben) kulon Instant.now()-t beallitani.
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // JPA entitasoknal fontos, hogy az equals/hashCode NE a Lombok @Data altal generalt,
    // MINDEN mezot figyelembe vevo valtozat legyen (az lassu es hibas lehet lazy-betoltott
    // kapcsolatokkal). Itt csak az azonosito (id) szamit - ket User akkor egyenlo, ha ugyanaz az id-juk.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    // A hashCode szandekosan konstans (osztaly alapu, nem mezo alapu) - ez a Hibernate altal
    // ajanlott biztonsagos megoldas, hogy a hashCode ne valtozzon meg azutan, hogy egy objektum
    // mar bekerult egy HashSet/HashMap-be (pl. mert kozben megkapta a generalt id-jat).
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
