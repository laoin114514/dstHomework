package com.mapcoloring.ui;

import com.mapcoloring.model.MapData;
import com.mapcoloring.model.Point;
import com.mapcoloring.model.Province;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MapCanvas extends Pane {

    private static final Color[] PALETTE = {
        Color.rgb(255, 179, 186),
        Color.rgb(186, 255, 201),
        Color.rgb(186, 225, 255),
        Color.rgb(255, 255, 186),
        Color.rgb(255, 210, 140),
        Color.rgb(220, 186, 255),
        Color.rgb(186, 255, 255),
        Color.rgb(255, 200, 220),
    };

    private final Canvas canvas;
    private MapData mapData;
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double lastMouseX, lastMouseY;
    private int highlightedIndex = -1;

    public MapCanvas() {
        canvas = new Canvas();
        getChildren().add(canvas);
        setupMouseHandlers();
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        if (w > 0 && h > 0) {
            canvas.setWidth(w);
            canvas.setHeight(h);
            fitToCanvas();
            draw();
        }
    }

    public void setMapData(MapData data) {
        this.mapData = data;
        fitToCanvas();
        draw();
    }

    private void fitToCanvas() {
        if (mapData == null || mapData.provinces.size() == 0) return;
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (int i = 0; i < mapData.provinces.size(); i++) {
            Province p = mapData.provinces.get(i);
            for (int j = 0; j < p.polygon.size(); j++) {
                Point pt = p.polygon.get(j);
                if (pt.x < minX) minX = pt.x;
                if (pt.y < minY) minY = pt.y;
                if (pt.x > maxX) maxX = pt.x;
                if (pt.y > maxY) maxY = pt.y;
            }
        }

        double dataW = maxX - minX;
        double dataH = maxY - minY;
        double padding = 30;
        scale = Math.min((w - padding * 2) / dataW, (h - padding * 2) / dataH);
        offsetX = -minX * scale + padding;
        offsetY = -minY * scale + padding;
    }

    public void draw() {
        if (mapData == null) return;
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        for (int i = 0; i < mapData.provinces.size(); i++) {
            Province p = mapData.provinces.get(i);
            int colorIdx = mapData.graph.getColor(i);
            drawProvince(gc, p, colorIdx, i == highlightedIndex);
        }
    }

    private void drawProvince(GraphicsContext gc, Province p,
                              int colorIdx, boolean highlighted) {
        if (p.polygon.size() < 3) return;

        double[] xs = new double[p.polygon.size()];
        double[] ys = new double[p.polygon.size()];
        for (int j = 0; j < p.polygon.size(); j++) {
            Point pt = p.polygon.get(j);
            xs[j] = pt.x * scale + offsetX;
            ys[j] = pt.y * scale + offsetY;
        }

        if (colorIdx > 0) {
            gc.setFill(PALETTE[(colorIdx - 1) % PALETTE.length]);
        } else {
            gc.setFill(Color.LIGHTGRAY);
        }
        gc.fillPolygon(xs, ys, p.polygon.size());

        gc.setStroke(highlighted ? Color.RED : Color.DARKGRAY);
        gc.setLineWidth(highlighted ? 2.0 : 1.0);
        gc.strokePolygon(xs, ys, p.polygon.size());

        if (p.polygon.size() > 0) {
            double cx = 0, cy = 0;
            for (int j = 0; j < p.polygon.size(); j++) {
                cx += xs[j];
                cy += ys[j];
            }
            cx /= p.polygon.size();
            cy /= p.polygon.size();
            gc.setFont(new Font("Microsoft YaHei", Math.max(10, scale * 0.18)));
            gc.setFill(Color.BLACK);
            gc.fillText(p.name, cx - 15, cy + 4);
        }
    }

    private void setupMouseHandlers() {
        canvas.setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            double mx = e.getX();
            double my = e.getY();
            offsetX = mx - (mx - offsetX) * factor;
            offsetY = my - (my - offsetY) * factor;
            scale *= factor;
            draw();
        });

        canvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        canvas.setOnMouseDragged(e -> {
            offsetX += e.getX() - lastMouseX;
            offsetY += e.getY() - lastMouseY;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            draw();
        });

        canvas.setOnMouseMoved(e -> {
            if (mapData == null) return;
            int newHighlight = findProvinceAt(e.getX(), e.getY());
            if (newHighlight != highlightedIndex) {
                highlightedIndex = newHighlight;
                draw();
            }
        });
    }

    private int findProvinceAt(double mx, double my) {
        for (int i = mapData.provinces.size() - 1; i >= 0; i--) {
            Province p = mapData.provinces.get(i);
            if (p.polygon.size() < 3) continue;
            if (pointInPolygon(mx, my, p)) return i;
        }
        return -1;
    }

    private boolean pointInPolygon(double px, double py, Province p) {
        boolean inside = false;
        int n = p.polygon.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = p.polygon.get(i);
            Point pj = p.polygon.get(j);
            double xi = pi.x * scale + offsetX;
            double yi = pi.y * scale + offsetY;
            double xj = pj.x * scale + offsetX;
            double yj = pj.y * scale + offsetY;
            if ((yi > py) != (yj > py)
                    && px < (xj - xi) * (py - yi) / (yj - yi) + xi) {
                inside = !inside;
            }
        }
        return inside;
    }

    /**
     * 给单个省份着色并重绘画布。
     * 供 ColoringAnimator 在动画播放时调用。
     */
    public void colorProvince(int index, int color) {
        if (mapData == null || index < 0 || index >= mapData.graph.getVertexCount()) {
            return;
        }
        mapData.graph.setColor(index, color);
        draw();
    }

    /**
     * 取消单个省份颜色（恢复灰色/未着色状态）并重绘画布。
     * 供 ColoringAnimator 在回溯撤销时调用。
     */
    public void uncolorProvince(int index) {
        if (mapData == null || index < 0 || index >= mapData.graph.getVertexCount()) {
            return;
        }
        mapData.graph.setColor(index, 0);
        draw();
    }

    public MapData getMapData() {
        return mapData;
    }

    public double getHighlightedIndex() {
        return highlightedIndex;
    }
}
