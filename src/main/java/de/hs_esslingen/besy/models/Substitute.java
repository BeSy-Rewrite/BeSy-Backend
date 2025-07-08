package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "substitute")
public class Substitute {
    @EmbeddedId
    private SubstituteId id;

    @MapsId("substitutedUserName")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substituted_user_name", nullable = false)
    private de.hs_esslingen.besy.models.User substitutedUserName;

    @MapsId("substituteUserName")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substitute_user_name", nullable = false)
    private de.hs_esslingen.besy.models.User substituteUserName;

    @Column(name = "substitute_comment")
    private String substituteComment;

}