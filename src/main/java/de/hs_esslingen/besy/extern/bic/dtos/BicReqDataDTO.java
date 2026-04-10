package de.hs_esslingen.besy.extern.bic.dtos;

import lombok.Value;

import java.io.Serializable;

@Value
public class BicReqDataDTO implements Serializable {
    Boolean autoRun;
    String title;
    String id;
    String email;
    String intranetUrl;
    String responseUrlData;
    String responseUrlFile;
    String responseSendWith;
    BicReqDataFlagsDTO flags;
}

