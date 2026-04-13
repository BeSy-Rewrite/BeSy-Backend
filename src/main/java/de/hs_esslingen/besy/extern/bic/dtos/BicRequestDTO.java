package de.hs_esslingen.besy.extern.bic.dtos;

import lombok.Value;

import java.io.Serializable;

@Value
public class BicRequestDTO implements Serializable {
    BicVariablesDTO variables;
}
