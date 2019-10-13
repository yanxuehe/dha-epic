package com.ibm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "patients")
//@NamedQuery(name = "", query = "from Patient")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private String app;
}
