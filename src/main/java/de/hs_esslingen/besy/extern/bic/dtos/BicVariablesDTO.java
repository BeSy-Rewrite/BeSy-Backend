package de.hs_esslingen.besy.extern.bic.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.Serializable;

@Value
public class BicVariablesDTO implements Serializable {
    @JsonProperty("_case.name")
    String caseName;

    @JsonProperty("_case.dueDate")
    String caseDueDate;

    BicReqDataDTO reqData;
}
