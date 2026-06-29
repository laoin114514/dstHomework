package com.mapcoloring.io;

import com.mapcoloring.model.MapData;
import com.mapcoloring.model.Point;
import com.mapcoloring.model.Province;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MapFileHandler {

    public static void save(MapData data, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            for (int i = 0; i < data.provinces.size(); i++) {
                Province p = data.provinces.get(i);
                writer.write("PROVINCE " + p.id + " " + p.name);
                for (int j = 0; j < p.polygon.size(); j++) {
                    Point pt = p.polygon.get(j);
                    writer.write(" " + pt.x + "," + pt.y);
                }
                writer.newLine();
            }
            for (int i = 0; i < data.graph.getVertexCount(); i++) {
                var neighbors = data.graph.getNeighbors(i);
                if (neighbors.size() > 0) {
                    writer.write("EDGES " + i);
                    for (int j = 0; j < neighbors.size(); j++) {
                        writer.write(" " + neighbors.get(j));
                    }
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save map: " + e.getMessage(), e);
        }
    }

    public static MapData load(String filePath) {
        MapData data = new MapData();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 3) continue;

                if ("PROVINCE".equals(parts[0])) {
                    Province p = new Province();
                    p.id = Integer.parseInt(parts[1]);

                    // Build name from tokens before the first coordinate (has comma)
                    int coordStart = 2;
                    while (coordStart < parts.length
                            && !parts[coordStart].contains(",")) {
                        coordStart++;
                    }
                    StringBuilder nameBuilder = new StringBuilder();
                    for (int i = 2; i < coordStart; i++) {
                        if (i > 2) nameBuilder.append(" ");
                        nameBuilder.append(parts[i]);
                    }
                    p.name = nameBuilder.toString();

                    for (int i = coordStart; i < parts.length; i++) {
                        String[] coord = parts[i].split(",");
                        Point pt = new Point(
                            Double.parseDouble(coord[0]),
                            Double.parseDouble(coord[1]));
                        p.polygon.add(pt);
                    }
                    data.addProvince(p);
                } else if ("EDGES".equals(parts[0])) {
                    int from = Integer.parseInt(parts[1]);
                    for (int i = 2; i < parts.length; i++) {
                        String[] subParts = parts[i].split(",");
                        for (String s : subParts) {
                            if (!s.isEmpty()) {
                                int to = Integer.parseInt(s);
                                data.addAdjacency(from, to);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load map: " + e.getMessage(), e);
        }
        return data;
    }
}
