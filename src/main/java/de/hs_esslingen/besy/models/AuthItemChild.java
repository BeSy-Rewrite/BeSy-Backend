package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "auth_item_child")
public class AuthItemChild {
    @EmbeddedId
    private AuthItemChildId id;

    @MapsId("parent")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parent", nullable = false)
    private AuthItem parent;

    @MapsId("child")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "child", nullable = false)
    private AuthItem child;

}