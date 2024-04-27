package com.edu.szu.entity;

import entity.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectPoint implements Serializable {

    private String objId;

    private String name;

    private Coordinate coordinate;

    private List<String> keywords;
}
