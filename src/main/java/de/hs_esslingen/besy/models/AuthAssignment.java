package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "auth_assignment")
public class AuthAssignment {
    @EmbeddedId
    private AuthAssignmentId id;

    @MapsId("itemname")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "itemname", nullable = false)
    private de.hs_esslingen.besy.models.AuthItem itemname;

    @MapsId("userid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "userid", nullable = false)
    private de.hs_esslingen.besy.models.User userid;

    @Lob
    @Column(name = "bizrule")
    private String bizrule;

    @Lob
    @Column(name = "data")
    private String data;

}