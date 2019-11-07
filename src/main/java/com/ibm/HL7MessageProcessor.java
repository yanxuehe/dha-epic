package com.ibm;


import ca.uhn.hl7v2.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class HL7MessageProcessor {

    public Message newName(Message msg) throws Exception {
        if ("2.6".equals(msg.getVersion())) {
            if (msg instanceof ca.uhn.hl7v2.model.v26.message.ADT_A01) {
                ca.uhn.hl7v2.model.v26.message.ADT_A01 adt_a01 = (ca.uhn.hl7v2.model.v26.message.ADT_A01) msg;
                ca.uhn.hl7v2.model.v26.segment.PID pid = adt_a01.getPID();
                ca.uhn.hl7v2.model.v26.datatype.XPN[] xpns = pid.getPatientName();

                String patientID = pid.getPatientID().getIDNumber().getValueOrEmpty();
                if (null != xpns && 0 < xpns.length) {

                    String firstname = null,
                            lastname = null;

                    for (ca.uhn.hl7v2.model.v26.datatype.XPN xpn : xpns) {
                        firstname = xpn.getGivenName().getValueOrEmpty();
                        lastname = xpn.getFamilyName().getSurname().getValueOrEmpty();
                        log.info("the original name of Patient[{}] is {}, {}", patientID, lastname, firstname);

                        xpn.getGivenName().setValue(lastname);
                        xpn.getFamilyName().getSurname().setValue(firstname);

                        firstname = xpn.getGivenName().getValueOrEmpty();
                        lastname = xpn.getFamilyName().getSurname().getValueOrEmpty();
                        log.info("the new name of Patient[{}] is {}, {}", patientID, lastname, firstname);
                    }
                } else {
                    // throw new Exception("Not Implemented Message Type: " + msg.getClass().getSimpleName());
                    log.warn("Not Implemented Message Type: {}", msg.getClass().getSimpleName());
                }
            }
        } else {
            //throw new Exception("Not Implemented version.");
            log.warn("Not Implemented version.");
        }

        return msg;
    }

    public Patient getPatient(Message msg) throws Exception {
        Patient p = null;

        switch (msg.getVersion()) {
            case "2.2":
                p = getPatient_v22(msg);
                break;
            case "2.3.1":
                p = getPatient_v231(msg);
                break;
            case "2.4":
                p = getPatient_v24(msg);
                break;
            case "2.6":
                p = getPatient_v26(msg);
                break;
            default:
                //throw new Exception("Not Implemented version.");
                log.warn("Not Implemented version.");
        }

        return p;
    }

    private Patient getPatient_v26(Message msg) throws Exception {
        Patient p = null;

        if (msg instanceof ca.uhn.hl7v2.model.v26.message.ADT_A01) {
            ca.uhn.hl7v2.model.v26.message.ADT_A01 adt_a01 = (ca.uhn.hl7v2.model.v26.message.ADT_A01) msg;
            ca.uhn.hl7v2.model.v26.segment.PID pid = adt_a01.getPID();
            ca.uhn.hl7v2.model.v26.datatype.XPN[] xpns = pid.getPatientName();

            String facility = adt_a01.getMSH().getSendingFacility().getNamespaceID().getValueOrEmpty(),
                    app = adt_a01.getMSH().getSendingApplication().getNamespaceID().getValueOrEmpty();

            String hisId = pid.getRace(0).getAlternateIdentifier().getValueOrEmpty(),
                    patientId = pid.getPatientID().getIDNumber().getValueOrEmpty();

            if (null != xpns && 0 < xpns.length) {

                String firstname = null,
                        lastname = null;

                ca.uhn.hl7v2.model.v26.datatype.XPN xpn = xpns[0];
                firstname = xpn.getGivenName().getValueOrEmpty();
                lastname = xpn.getFamilyName().getSurname().getValueOrEmpty();
                log.info("the name of Patient[{}] is {}, {}", hisId, lastname, firstname);

                p = new Patient()
                        .withApp(app)
                        .withFacility(facility)
                        .withFirstName(firstname)
                        .withLastName(lastname)
                        .withHisid(hisId).withPid(patientId);

            }
        } else {
            // throw new Exception("Not Implemented Message Type: " + msg.getClass().getSimpleName());
            log.warn("Not Implemented Message Type: {}", msg.getClass().getSimpleName());
        }
        return p;
    }

    private Patient getPatient_v24(Message msg) throws Exception {
        final Patient p = new Patient();

        if (msg instanceof ca.uhn.hl7v2.model.v24.message.ORU_R01) {
            ca.uhn.hl7v2.model.v24.message.ORU_R01 oru_r01 = (ca.uhn.hl7v2.model.v24.message.ORU_R01) msg;
            ca.uhn.hl7v2.model.v24.segment.MSH msh = oru_r01.getMSH();
            ca.uhn.hl7v2.model.v24.segment.PID pid = oru_r01.getPATIENT_RESULT().getPATIENT().getPID();
            ca.uhn.hl7v2.model.v24.segment.OBR obr = oru_r01.getPATIENT_RESULT().getORDER_OBSERVATION().getOBR();
            List<ca.uhn.hl7v2.model.v24.group.ORU_R01_OBSERVATION> observations = oru_r01.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATIONAll();

            setPaitentBasicInfo_v24(p, msh, pid);
            p.addOBR(new OBR(obr.getFillerOrderNumber().toString()));
            observations.forEach(obs -> {
                StringBuilder ov = new StringBuilder();
                Arrays.stream(obs.getOBX().getObservationValue()).forEach(v -> {
                    ov.append(v.getData());
                });
                String res = obs.getOBX().getObservationSubId().getValueOrEmpty() + "|" + ov.toString();
                p.addOBX(new OBX(res));
            });
        } else if (msg instanceof ca.uhn.hl7v2.model.v24.message.ORM_O01) {
            ca.uhn.hl7v2.model.v24.message.ORM_O01 orm_o01 = (ca.uhn.hl7v2.model.v24.message.ORM_O01) msg;
            ca.uhn.hl7v2.model.v24.segment.MSH msh = orm_o01.getMSH();
            ca.uhn.hl7v2.model.v24.segment.PID pid = orm_o01.getPATIENT().getPID();
            ca.uhn.hl7v2.model.v24.segment.OBR obr = orm_o01.getORDER().getORDER_DETAIL().getOBR();
            List<ca.uhn.hl7v2.model.v24.group.ORM_O01_OBSERVATION> observations = orm_o01.getORDER().getORDER_DETAIL().getOBSERVATIONAll();

            setPaitentBasicInfo_v24(p, msh, pid);
            // add OBR
            p.addOBR(new OBR(obr.getFillerOrderNumber().toString()));
            // add OBXs
            observations.forEach(obs -> {
                StringBuilder ov = new StringBuilder();
                Arrays.stream(obs.getOBX().getObservationValue()).forEach(v -> {
                    ov.append(v.getData());
                });
                String res = obs.getOBX().getObservationSubId() + "|" + ov.toString();

                p.addOBX(new OBX(res));
            });
        } else {
            // throw new Exception("Not Implemented Message Type: " + msg.getClass().getSimpleName());
            log.warn("Not Implemented Message Type: {}", msg.getClass().getSimpleName());
        }
        return p.isNew() ? null : p;
    }

    private Patient getPatient_v231(Message msg) throws Exception {
        Patient p = null;

        if (msg instanceof ca.uhn.hl7v2.model.v231.message.ADT_A04) {
            ca.uhn.hl7v2.model.v231.message.ADT_A04 adt_a04 = (ca.uhn.hl7v2.model.v231.message.ADT_A04) msg;
            ca.uhn.hl7v2.model.v231.segment.MSH msh = adt_a04.getMSH();
            ca.uhn.hl7v2.model.v231.segment.PID pid = adt_a04.getPID();

            p = new Patient().withPid(pid.getPatientID().getCx1_ID().getValueOrEmpty())
                    .withFirstName(pid.getPatientName()[0].getGivenName().getValueOrEmpty())
                    .withLastName(pid.getPatientName()[0].getFamilyLastName().getFamilyName().getValueOrEmpty())
                    .withFacility(msh.getSendingFacility().getNamespaceID().getValueOrEmpty())
                    .withApp(msh.getSendingApplication().getNamespaceID().getValueOrEmpty());
        } else {
            // throw new Exception("Not Implemented Message Type: " + msg.getClass().getSimpleName());
            log.warn("Not Implemented Message Type: {}", msg.getClass().getSimpleName());
        }
        return p;
    }

    private Patient getPatient_v22(Message msg) throws Exception {
        Patient p = null;

        if (msg instanceof ca.uhn.hl7v2.model.v22.message.ADT_A28) {
            ca.uhn.hl7v2.model.v22.message.ADT_A28 adt_a28 = (ca.uhn.hl7v2.model.v22.message.ADT_A28) msg;
            ca.uhn.hl7v2.model.v22.segment.MSH msh = adt_a28.getMSH();
            ca.uhn.hl7v2.model.v22.segment.PID pid = adt_a28.getPID();

            p = new Patient().withPid(pid.getPatientIDInternalID()[1].getCm_pat_id1_IDNumber().getValueOrEmpty())
                    .withFirstName(pid.getPatientName().getGivenName().getValueOrEmpty())
                    .withLastName(pid.getPatientName().getFamilyName().getValueOrEmpty())
                    .withFacility(msh.getSendingFacility().getValueOrEmpty())
                    .withApp(msh.getSendingApplication().getValueOrEmpty());
        } else {
            // throw new Exception("Not Implemented Message Type: " + msg.getClass().getSimpleName());
            log.warn("Not Implemented Message Type: {}", msg.getClass().getSimpleName());
        }
        return p;
    }

    private void setPaitentBasicInfo_v24(Patient p, ca.uhn.hl7v2.model.v24.segment.MSH msh, ca.uhn.hl7v2.model.v24.segment.PID pid) {
        p.setPid(pid.getPatientID().getCx1_ID().getValueOrEmpty());
        p.setFirstName(pid.getPatientName()[0].getGivenName().getValueOrEmpty());
        p.setLastName(pid.getPatientName()[0].getFamilyName().getSurname().getValueOrEmpty());
        p.setFacility(msh.getSendingFacility().getNamespaceID().getValueOrEmpty());
        p.setApp(msh.getSendingApplication().getNamespaceID().getValueOrEmpty());
    }
}
