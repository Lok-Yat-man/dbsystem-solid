package com.edu.szu.util;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EdgeReader {
    public static Map<Long, Set<Long>> getEdges(String fileName) throws IOException {
        var result = new HashMap<Long, Set<Long>>();
        try(InputStream is = new ByteArrayInputStream(IOUtils.resourceToByteArray(fileName,EdgeReader.class.getClassLoader()))){
            List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_8);
            lines.forEach(line ->{
                String[] users = line.split("\t");
                long user1 = Long.parseLong(users[0]);
                long user2 = Long.parseLong(users[1]);
                result.computeIfAbsent(user1,key -> new HashSet<>());
                result.computeIfAbsent(user2,key -> new HashSet<>());
                result.get(user1).add(user2);
                result.get(user2).add(user1);
            });
        }
        return result;
    }
}
