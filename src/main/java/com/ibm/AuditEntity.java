package com.ibm;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(AuditingEntityListener.class)
public class AuditEntity {
    @Id
    @GeneratedValue
    private long id;

    @CreatedDate
    @JsonSerialize(using = MyInstantSerializer.class) //pattern = "yyyy-MM-dd HH:mm a z"
    private Instant createAt;

    @LastModifiedDate
    @JsonSerialize(using = MyInstantSerializer.class)
    private Instant lastModifiedAt;
}
