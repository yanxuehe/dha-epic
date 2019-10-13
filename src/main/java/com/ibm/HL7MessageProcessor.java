package com.ibm;


import ca.uhn.hl7v2.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HL7MessageProcessor {

    public Message newName(Message msg) throws Exception {
        if ("2.5".equals(msg.getVersion())) {
            ca.uhn.hl7v2.model.v25.message.ADT_A01 adt_a01 = (ca.uhn.hl7v2.model.v25.message.ADT_A01) msg;
            ca.uhn.hl7v2.model.v25.segment.PID pid = adt_a01.getPID();
            ca.uhn.hl7v2.model.v25.datatype.XPN[] xpns = pid.getPatientName();

            String patientID = pid.getPatientID().getIDNumber().getValue();
            if (null != xpns && 0 < xpns.length) {

                String firstname = null,
                        lastname = null;

                for (ca.uhn.hl7v2.model.v25.datatype.XPN xpn : xpns) {
                    firstname = xpn.getGivenName().getValue();
                    lastname = xpn.getFamilyName().getSurname().getValue();
                    log.info("the original name of Patient[{}] is {}, {}", patientID, lastname, firstname);

                    xpn.getGivenName().setValue(lastname);
                    xpn.getFamilyName().getSurname().setValue(firstname);


                    firstname = xpn.getGivenName().getValue();
                    lastname = xpn.getFamilyName().getSurname().getValue();
                    log.info("the new name of Patient[{}] is {}, {}", patientID, lastname, firstname);
                }
            }

        }

        return msg;
    }

    public Patient getPatient(Message msg) throws Exception {
        Patient p = null;

        if ("2.5".equals(msg.getVersion())) {
            ca.uhn.hl7v2.model.v25.message.ADT_A01 adt_a01 = (ca.uhn.hl7v2.model.v25.message.ADT_A01) msg;
            ca.uhn.hl7v2.model.v25.segment.PID pid = adt_a01.getPID();
            ca.uhn.hl7v2.model.v25.datatype.XPN[] xpns = pid.getPatientName();

            String facility = adt_a01.getMSH().getSendingFacility().getNamespaceID().getValue(),
                        app = adt_a01.getMSH().getSendingApplication().getNamespaceID().getValue();

            String patientID = pid.getPatientID().getIDNumber().getValue();
            if (null != xpns && 0 < xpns.length) {

                String firstname = null,
                        lastname = null;

                ca.uhn.hl7v2.model.v25.datatype.XPN xpn = xpns[0];
                firstname = xpn.getGivenName().getValue();
                lastname = xpn.getFamilyName().getSurname().getValue();
                log.info("the name of Patient[{}] is {}, {}", patientID, lastname, firstname);

                p = new Patient();
                p.setFirstName(firstname);
                p.setLastName(lastname);
                p.setPid(patientID);
                p.setFacility(facility);
                p.setApp(app);

            }
        }

        return p;
    }
}
