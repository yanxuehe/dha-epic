package com.ibm;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@Entity
@Table(name = "patients")
//@NamedQuery(name = "", query = "from Patient")
@Data
@With
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Patient {

    @Id
    @GeneratedValue
    private long id;

    //@Column(unique = true)
    private String pid;

    private String lastName;
    private String firstName;

    private String facility;
    @With private String app;

    @CreatedDate
    //pattern = "yyyy-MM-dd HH:mm a z"
    @JsonSerialize(using = MyInstantSerializer.class)
    private Instant createAt;

}
