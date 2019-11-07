package com.ibm;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "t_patients")
//@NamedQuery(name = "", query = "from Patient")
@Data
@With
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Patient extends AuditEntity {
    //@Column(unique = true)
    private String hisid;
    private String pid;
    private String lastName;
    private String firstName;
    private String facility;
    private String app;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<OBX> obxes;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<OBR> obrs;

    public void addOBX(OBX obx) {
        if (null == this.obxes) {
            this.obxes = Sets.newHashSet();
        }
        this.obxes.add(obx);
    }

    public void addOBR(OBR obr) {
        if (null == this.obrs) {
            this.obrs = Sets.newHashSet();
        }
        this.obrs.add(obr);
    }

    public boolean isNew() {
        return hisid == null && pid == null && lastName == null && firstName == null && facility == null && app == null && obxes == null && obrs == null;
    }
}
