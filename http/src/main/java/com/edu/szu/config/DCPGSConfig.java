package com.edu.szu.config;

import com.edu.szu.DCPGSManager;
import com.edu.szu.util.EdgeReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class DCPGSConfig {

    @Value("${DCPGS.resourcePath:DCPGS/src/main/resources/}")
    public String resourcePath;

    @Bean
    public DCPGSManager dcpgsManager() throws IOException {
        var dcpgsManager = new DCPGSManager(resourcePath);
        Map<String, Map<Long, Set<Long>>> edgeMapSet = new HashMap<>();
        var edgeMap = EdgeReader.getEdges("gowalla/loc-gowalla_edges.txt");
        edgeMapSet.put("gowalla",edgeMap);
        edgeMap = EdgeReader.getEdges("brightkite/loc-brightkite_edges.txt");
        edgeMapSet.put("brightkite",edgeMap);
        dcpgsManager.setEdgeMapSet(edgeMapSet);
        return dcpgsManager;
    }
}
