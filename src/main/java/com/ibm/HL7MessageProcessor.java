package com.ibm;


import ca.uhn.hl7v2.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HL7MessageProcessor {

    public Message newName(Message msg) throws Exception {
        if ("2.6".equals(msg.getVersion())) {
            ca.uhn.hl7v2.model.v26.message.ADT_A01 adt_a01 = (ca.uhn.hl7v2.model.v26.message.ADT_A01) msg;
            ca.uhn.hl7v2.model.v26.segment.PID pid = adt_a01.getPID();
            ca.uhn.hl7v2.model.v26.datatype.XPN[] xpns = pid.getPatientName();

            String patientID = pid.getPatientID().getIDNumber().getValue();
            if (null != xpns && 0 < xpns.length) {

                String firstname = null,
                        lastname = null;

                for (ca.uhn.hl7v2.model.v26.datatype.XPN xpn : xpns) {
                    firstname = xpn.getGivenName().getValue();
                    lastname = xpn.getFamilyName().getSurname().getValue();
                    log.info("the original name of Patient[{}] is {}, {}", patientID, lastname, firstname);

                    xpn.getGivenName().setValue(lastname);
                    xpn.getFamilyName().getSurname().setValue(firstname);


                    xpn.getPrefixEgDR().setValue("TEST_PREFIX");

                    firstname = xpn.getGivenName().getValue();
                    lastname = xpn.getFamilyName().getSurname().getValue();
                    log.info("the new name of Patient[{}] is {}, {}", patientID, lastname, firstname);
                }
            }

        } else {
            throw new Exception("Not Supported HL7 version.");
        }

        return msg;
    }

    public Patient getPatient(Message msg) throws Exception {
        Patient p = null;

        if ("2.6".equals(msg.getVersion())) {
            ca.uhn.hl7v2.model.v26.message.ADT_A01 adt_a01 = (ca.uhn.hl7v2.model.v26.message.ADT_A01) msg;
            ca.uhn.hl7v2.model.v26.segment.PID pid = adt_a01.getPID();
            ca.uhn.hl7v2.model.v26.datatype.XPN[] xpns = pid.getPatientName();

            String facility = adt_a01.getMSH().getSendingFacility().getNamespaceID().getValue(),
                        app = adt_a01.getMSH().getSendingApplication().getNamespaceID().getValue();

            String hisId = pid.getRace(0).getAlternateIdentifier().getValue(),
               patientId = pid.getPatientID().getIDNumber().getValue();

            if (null != xpns && 0 < xpns.length) {

                String firstname = null,
                        lastname = null;

                ca.uhn.hl7v2.model.v26.datatype.XPN xpn = xpns[0];
                firstname = xpn.getGivenName().getValue();
                lastname = xpn.getFamilyName().getSurname().getValue();
                log.info("the name of Patient[{}] is {}, {}", hisId, lastname, firstname);

                p = new Patient()
                        .withApp(app)
                        .withFacility(facility)
                        .withFirstName(firstname)
                        .withLastName(lastname)
                        .withHisid(hisId).withPid(patientId);

            }
        }

        return p;
    }
}
