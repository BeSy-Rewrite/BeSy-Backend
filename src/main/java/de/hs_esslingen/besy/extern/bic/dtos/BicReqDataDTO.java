package de.hs_esslingen.besy.extern.bic.dtos;

import lombok.Value;

import java.io.Serializable;

@Value
public class BicReqDataDTO implements Serializable {
    Boolean autoRun;
    String title;
    String ID;
    String email;
    String intranetUrl;
    String responseUrl;
    Boolean flag_edv_permission;
    Boolean flag_furniture_permission;
    Boolean flag_furniture_room;
    Boolean flag_investment_room;
    Boolean flag_investment_structural_measures;
    Boolean flag_media_permission;
}
