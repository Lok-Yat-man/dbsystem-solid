package com.edu.szu.config;

import com.edu.szu.DCPGSEndpoint;
import com.edu.szu.DCPGSManager;
import com.edu.szu.util.EdgeReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class DCPGSConfig {

    @Bean
    public DCPGSManager dcpgsManager() throws IOException {
        var dcpgsManager = new DCPGSManager();
        var edgeMap = EdgeReader.getEdges("gowalla/loc-gowalla_edges.txt");
        dcpgsManager.setEdgeMap(edgeMap);
        return dcpgsManager;
    }
}
