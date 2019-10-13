package com.ibm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Database {

    @Autowired
    PatientRepository patients;

    public Iterable<Patient> allPatients() {
        return patients.findAll();
    }

    public Patient registry(Patient patient) {
        return patients.save(patient);
    }
}
