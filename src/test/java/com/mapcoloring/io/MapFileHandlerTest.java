package com.mapcoloring.io;

import com.mapcoloring.model.MapData;
import com.mapcoloring.model.Province;
import com.mapcoloring.model.Point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class MapFileHandlerTest {

    @Test
    void testSaveAndLoad(@TempDir Path tempDir) throws Exception {
        MapData original = new MapData();

        Province p0 = new Province(0, "北京");
        p0.polygon.add(new Point(100, 50));
        p0.polygon.add(new Point(110, 55));
        p0.polygon.add(new Point(105, 60));
        original.addProvince(p0);

        Province p1 = new Province(1, "天津");
        p1.polygon.add(new Point(110, 55));
        p1.polygon.add(new Point(115, 58));
        p1.polygon.add(new Point(112, 60));
        original.addProvince(p1);

        Province p2 = new Province(2, "河北");
        p2.polygon.add(new Point(95, 50));
        p2.polygon.add(new Point(120, 50));
        p2.polygon.add(new Point(120, 65));
        p2.polygon.add(new Point(95, 65));
        original.addProvince(p2);

        original.addAdjacency(0, 1);
        original.addAdjacency(0, 2);

        String filePath = tempDir.resolve("test.map").toString();
        MapFileHandler.save(original, filePath);

        MapData loaded = MapFileHandler.load(filePath);
        assertEquals(3, loaded.provinces.size());
        assertEquals("北京", loaded.provinces.get(0).name);
        assertEquals("天津", loaded.provinces.get(1).name);
        assertEquals(3, loaded.provinces.get(0).polygon.size());
        assertEquals(2, loaded.graph.getDegree(0));
        assertEquals(1, loaded.graph.getDegree(1));
    }

    @Test
    void testLoadNonexistentFile() {
        assertThrows(RuntimeException.class, () -> {
            MapFileHandler.load("nonexistent.map");
        });
    }
}
