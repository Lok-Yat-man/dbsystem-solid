package com.edu.szu.entity;

import cn.edu.szu.cs.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Marker implements Serializable {

    private String clusterId;

    private Coordinate coordinate;

    private Integer pointNum;

    private String description;
}
