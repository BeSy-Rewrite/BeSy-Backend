package de.hs_esslingen.besy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class AuthAssignmentId implements java.io.Serializable {
    private static final long serialVersionUID = 5973210363735206238L;
    @Column(name = "itemname", nullable = false, length = 64)
    private String itemname;

    @Column(name = "userid", nullable = false)
    private String userid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AuthAssignmentId entity = (AuthAssignmentId) o;
        return Objects.equals(this.itemname, entity.itemname) &&
                Objects.equals(this.userid, entity.userid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemname, userid);
    }

}