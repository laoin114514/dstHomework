package com.mapcoloring.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts Natural Earth GeoJSON to .map format.
 * Self-contained: parses GeoJSON with basic string operations (no JSON lib).
 *
 * Usage: java GeoJsonConverter <input.geojson> <country> <output.map>
 *   country: "China" or "United States of America"
 */
public class GeoJsonConverter {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: GeoJsonConverter <input.geojson> <country> <output.map>");
            System.exit(1);
        }
        convert(args[0], args[1], args[2]);
    }

    public static void convert(String inputPath, String targetCountry,
                                String outputPath) throws Exception {
        String text = readAll(inputPath);
        List<Region> regions = extractRegions(text, targetCountry);
        detectAdjacency(regions);
        writeMapFile(regions, outputPath);
        System.out.println("Generated " + outputPath + " with " + regions.size() + " regions");
    }

    private static String readAll(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static List<Region> extractRegions(String json, String targetCountry) {
        List<Region> regions = new ArrayList<>();

        // Find features for target country (GeoJSON has space after colon)
        String searchCountry = "\"admin\": \"" + targetCountry + "\"";
        int pos = 0;
        while ((pos = json.indexOf(searchCountry, pos)) >= 0) {
            // Find the feature object containing this match
            int featStart = json.lastIndexOf("{", pos);
            int featEnd = findMatchingBrace(json, featStart);

            if (featStart < 0 || featEnd < 0) {
                pos++;
                continue;
            }

            String feature = json.substring(featStart, featEnd + 1);

            // Extract name (GeoJSON uses space after colon in keys)
            String name = extractString(feature, "\"name\": \"");
            if (name == null) name = extractString(feature, "\"name_en\": \"");
            if (name == null) {
                pos = featEnd + 1;
                continue;
            }

            // Extract geometry type
            String geomType = extractString(feature, "\"type\":\"", 1);
            // Look for coordinates array
            int coordIdx = feature.indexOf("\"coordinates\":");
            if (coordIdx < 0) {
                pos = featEnd + 1;
                continue;
            }

            List<double[]> polygon = extractPolygon(feature, coordIdx + 14);
            if (polygon.isEmpty()) {
                pos = featEnd + 1;
                continue;
            }

            // Simplify: keep every Nth point
            polygon = simplify(polygon, 4);

            Region r = new Region();
            r.name = name;
            r.polygon = polygon;
            regions.add(r);

            pos = featEnd + 1;
        }

        return regions;
    }

    private static List<double[]> extractPolygon(String s, int start) {
        List<double[]> points = new ArrayList<>();

        // Skip whitespace
        while (start < s.length() && Character.isWhitespace(s.charAt(start))) start++;
        if (start >= s.length() || s.charAt(start) != '[') return points;

        // Find the innermost coordinate pairs: [x, y]
        int i = start;
        while (i < s.length()) {
            if (s.charAt(i) == '[' && (i + 1 < s.length()) &&
                (Character.isDigit(s.charAt(i + 1)) || s.charAt(i + 1) == '-')) {
                // This is a coordinate pair
                int pairEnd = s.indexOf(']', i);
                if (pairEnd < 0) break;
                String pair = s.substring(i + 1, pairEnd);
                String[] parts = pair.split(",");
                if (parts.length >= 2) {
                    try {
                        double x = Double.parseDouble(parts[0].trim());
                        double y = Double.parseDouble(parts[1].trim());
                        points.add(new double[]{x, y});
                    } catch (NumberFormatException ignored) {}
                }
                i = pairEnd + 1;
            } else {
                i++;
            }
        }
        return points;
    }

    private static List<double[]> simplify(List<double[]> points, int keepEvery) {
        if (points.size() <= 20) return points;
        List<double[]> result = new ArrayList<>();
        for (int i = 0; i < points.size(); i += keepEvery) {
            result.add(points.get(i));
        }
        // Always include last point
        if (result.get(result.size() - 1) != points.get(points.size() - 1)) {
            result.add(points.get(points.size() - 1));
        }
        return result;
    }

    private static void detectAdjacency(List<Region> regions) {
        double tolerance = 0.01; // ~1km in decimal degrees
        for (int i = 0; i < regions.size(); i++) {
            for (int j = i + 1; j < regions.size(); j++) {
                if (areAdjacent(regions.get(i), regions.get(j), tolerance)) {
                    regions.get(i).neighbors.add(j);
                    regions.get(j).neighbors.add(i);
                }
            }
        }
    }

    private static boolean areAdjacent(Region a, Region b, double tolerance) {
        List<double[]> pa = a.polygon;
        List<double[]> pb = b.polygon;
        for (int i = 0; i < pa.size(); i++) {
            double ax = pa.get(i)[0], ay = pa.get(i)[1];
            for (int j = 0; j < pb.size(); j++) {
                double bx = pb.get(j)[0], by = pb.get(j)[1];
                double dist = Math.sqrt((ax - bx) * (ax - bx) + (ay - by) * (ay - by));
                if (dist < tolerance) return true;
            }
        }
        return false;
    }

    private static void writeMapFile(List<Region> regions, String outputPath)
            throws IOException {
        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8))) {
            w.write("# Generated from Natural Earth GeoJSON data");
            w.newLine();

            // Find bounding box for coordinate normalization
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
            for (Region r : regions) {
                for (double[] p : r.polygon) {
                    if (p[0] < minX) minX = p[0];
                    if (p[1] < minY) minY = p[1];
                    if (p[0] > maxX) maxX = p[0];
                    if (p[1] > maxY) maxY = p[1];
                }
            }

            double scaleX = 700.0 / (maxX - minX);
            double scaleY = 500.0 / (maxY - minY);
            double scale = Math.min(scaleX, scaleY);

            // Write provinces
            for (int i = 0; i < regions.size(); i++) {
                Region r = regions.get(i);
                w.write("PROVINCE " + i + " " + r.name);
                for (double[] p : r.polygon) {
                    int px = (int)((p[0] - minX) * scale + 20);
                    int py = (int)((maxY - p[1]) * scale + 20); // flip Y
                    w.write(" " + px + "," + py);
                }
                w.newLine();
            }

            // Write edges
            for (int i = 0; i < regions.size(); i++) {
                Region r = regions.get(i);
                w.write("EDGES " + i);
                for (int n : r.neighbors) {
                    w.write(" " + n);
                }
                w.newLine();
            }
        }
    }

    private static String extractString(String src, String key) {
        return extractString(src, key, 0);
    }

    private static String extractString(String src, String key, int skip) {
        int idx = src.indexOf(key);
        for (int s = 0; s < skip && idx >= 0; s++) {
            idx = src.indexOf(key, idx + 1);
        }
        if (idx < 0) return null;

        int start = idx + key.length();
        int end = src.indexOf('"', start);
        if (end < 0) return null;

        String val = src.substring(start, end);
        // Skip GeoJSON wrapper keys like "coordinates", "type", "Feature"
        if (val.equals("coordinates") || val.equals("type") ||
            val.equals("Feature") || val.equals("FeatureCollection") ||
            val.equals("Polygon") || val.equals("MultiPolygon") ||
            val.equals("properties") || val.equals("geometry") ||
            val.equals("features") || val.length() < 1) {
            return extractString(src, key, skip + 1);
        }
        return val;
    }

    private static int findMatchingBrace(String s, int openIdx) {
        int depth = 0;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    static class Region {
        String name;
        List<double[]> polygon;
        List<Integer> neighbors = new ArrayList<>();
    }
}
