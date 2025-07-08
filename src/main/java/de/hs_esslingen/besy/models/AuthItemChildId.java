package de.hs_esslingen.besy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class AuthItemChildId implements java.io.Serializable {
    private static final long serialVersionUID = -8799755669592716915L;
    @Column(name = "parent", nullable = false, length = 64)
    private String parent;

    @Column(name = "child", nullable = false, length = 64)
    private String child;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AuthItemChildId entity = (AuthItemChildId) o;
        return Objects.equals(this.parent, entity.parent) &&
                Objects.equals(this.child, entity.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, child);
    }

}