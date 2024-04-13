package com.edu.szu.dto;

import lombok.*;

import java.io.Serializable;

/**
 *  DataCoordinateRangeDTO
 * @author Whitence
 * @date 2024/4/8 19:14
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataCoordinateRangeDTO implements Serializable {

    private Double hypotenuseLength;

    private Integer clusterNumber;

    private Double midLongitude;

    private Double midLatitude;
}
