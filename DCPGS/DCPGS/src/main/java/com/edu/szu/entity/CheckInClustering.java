package com.edu.szu.entity;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CheckInClustering {
    @Expose
    long clusterId;
    @Expose
    List<CheckIn> checkIns;
}
