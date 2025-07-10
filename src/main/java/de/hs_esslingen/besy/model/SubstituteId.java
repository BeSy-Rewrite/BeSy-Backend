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
public class SubstituteId implements java.io.Serializable {
    private static final long serialVersionUID = -8007874255405988915L;
    @Column(name = "substituted_user_name", nullable = false)
    private String substitutedUserName;

    @Column(name = "substitute_user_name", nullable = false)
    private String substituteUserName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SubstituteId entity = (SubstituteId) o;
        return Objects.equals(this.substituteUserName, entity.substituteUserName) &&
                Objects.equals(this.substitutedUserName, entity.substitutedUserName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(substituteUserName, substitutedUserName);
    }

}