package de.hs_esslingen.besy.model;

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
    private de.hs_esslingen.besy.model.AuthItem itemname;

    @MapsId("userid")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "userid", nullable = false)
    private de.hs_esslingen.besy.model.User userid;

    @Column(name = "bizrule", length = Integer.MAX_VALUE)
    private String bizrule;

    @Column(name = "data", length = Integer.MAX_VALUE)
    private String data;

}