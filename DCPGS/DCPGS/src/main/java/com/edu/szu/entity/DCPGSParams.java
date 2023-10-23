package com.edu.szu.entity;

import lombok.Data;

@Data
public class DCPGSParams {
    private double epsilon = 0.5;

    private double omega = 0.5;

    private double tau = 0.7;

    private double maxD = 120;

    public DCPGSParams(){}

    public DCPGSParams(DCPGSParams dcpgsParams){
        this.epsilon = dcpgsParams.getEpsilon();
        this.omega = dcpgsParams.getOmega();
        this.tau = dcpgsParams.getTau();
        this.maxD = dcpgsParams.getMaxD();
    }

}
