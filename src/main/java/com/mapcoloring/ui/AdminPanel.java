package com.mapcoloring.ui;

import com.mapcoloring.io.MapFileHandler;
import com.mapcoloring.model.MapData;
import com.mapcoloring.model.MyArrayList;
import com.mapcoloring.model.Point;
import com.mapcoloring.model.Province;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class AdminPanel extends BorderPane {

    private MapData mapData;
    private String currentFilePath;
    private ListView<String> provinceList;
    private ListView<String> connectedList;
    private ListView<String> unconnectedList;
    private Label editTargetLabel;

    public AdminPanel() {
        VBox leftPanel = createLeftPanel();
        setLeft(leftPanel);

        VBox centerPanel = createCenterPanel();
        setCenter(centerPanel);

        BorderPane.setMargin(leftPanel, new Insets(10));
        BorderPane.setMargin(centerPanel, new Insets(10));
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(8);
        panel.setPrefWidth(220);

        Label titleLabel = new Label("省份列表");
        titleLabel.setFont(new Font(14));

        provinceList = new ListView<>();
        provinceList.setPrefHeight(300);
        provinceList.getSelectionModel().selectedIndexProperty()
            .addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.intValue() >= 0) {
                    onProvinceSelected(newVal.intValue());
                }
            });

        HBox btnRow = new HBox(8);
        Button addBtn = new Button("添加");
        addBtn.setOnAction(e -> addProvince());
        Button delBtn = new Button("删除");
        delBtn.setOnAction(e -> deleteProvince());
        btnRow.getChildren().addAll(addBtn, delBtn);

        Separator sep = new Separator();

        Label fileLabel = new Label("文件操作");
        fileLabel.setFont(new Font(12));

        Button loadBtn = new Button("加载文件");
        loadBtn.setMaxWidth(Double.MAX_VALUE);
        loadBtn.setOnAction(e -> loadFile());

        Button saveBtn = new Button("保存到文件");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveFile());

        panel.getChildren().addAll(
            titleLabel, provinceList, btnRow, sep,
            fileLabel, loadBtn, saveBtn
        );

        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(8);

        editTargetLabel = new Label("邻接关系编辑 — 未选中省份");
        editTargetLabel.setFont(new Font(14));

        HBox listsRow = new HBox(16);

        VBox connectedBox = new VBox(4);
        Label connLabel = new Label("已连接的省份");
        connLabel.setFont(new Font(12));
        connectedList = new ListView<>();
        connectedList.setPrefHeight(180);
        connectedBox.getChildren().addAll(connLabel, connectedList);

        VBox unconnectedBox = new VBox(4);
        Label unconnLabel = new Label("未连接的省份");
        unconnLabel.setFont(new Font(12));
        unconnectedList = new ListView<>();
        unconnectedList.setPrefHeight(180);
        unconnectedBox.getChildren().addAll(unconnLabel, unconnectedList);

        listsRow.getChildren().addAll(connectedBox, unconnectedBox);
        HBox.setHgrow(connectedBox, Priority.ALWAYS);
        HBox.setHgrow(unconnectedBox, Priority.ALWAYS);

        HBox btnRow = new HBox(8);
        Button connectBtn = new Button("连接选中");
        connectBtn.setOnAction(e -> connectSelected());
        Button disconnectBtn = new Button("断开选中");
        disconnectBtn.setOnAction(e -> disconnectSelected());
        btnRow.getChildren().addAll(connectBtn, disconnectBtn);

        panel.getChildren().addAll(editTargetLabel, listsRow, btnRow);

        return panel;
    }

    public void setMapData(MapData data, String filePath) {
        this.mapData = data;
        this.currentFilePath = filePath;
        refreshProvinceList();
    }

    private void refreshProvinceList() {
        provinceList.getItems().clear();
        if (mapData == null) return;
        for (int i = 0; i < mapData.provinces.size(); i++) {
            provinceList.getItems().add(mapData.provinces.get(i).name);
        }
    }

    private void onProvinceSelected(int index) {
        editTargetLabel.setText("邻接关系编辑 — "
            + mapData.provinces.get(index).name);
        refreshAdjacencyLists(index);
    }

    private void refreshAdjacencyLists(int provinceIndex) {
        connectedList.getItems().clear();
        unconnectedList.getItems().clear();

        MyArrayList<Integer> neighbors = mapData.graph.getNeighbors(provinceIndex);
        for (int i = 0; i < mapData.provinces.size(); i++) {
            if (i == provinceIndex) continue;
            boolean isNeighbor = false;
            for (int j = 0; j < neighbors.size(); j++) {
                if (neighbors.get(j) == i) {
                    isNeighbor = true;
                    break;
                }
            }
            if (isNeighbor) {
                connectedList.getItems().add(mapData.provinces.get(i).name);
            } else {
                unconnectedList.getItems().add(mapData.provinces.get(i).name);
            }
        }
    }

    private void addProvince() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("添加省份");
        dialog.setHeaderText("输入省份名称");
        dialog.showAndWait().ifPresent(name -> {
            int id = mapData.provinces.size();
            Province p = new Province(id, name);
            p.polygon.add(new Point(10, 10));
            p.polygon.add(new Point(20, 10));
            p.polygon.add(new Point(15, 20));
            mapData.addProvince(p);
            refreshProvinceList();
        });
    }

    private void deleteProvince() {
        int idx = provinceList.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            showAlert("请先选择要删除的省份");
            return;
        }
        mapData.removeProvince(idx);
        refreshProvinceList();
    }

    private void connectSelected() {
        int provinceIdx = provinceList.getSelectionModel().getSelectedIndex();
        int targetIdx = unconnectedList.getSelectionModel().getSelectedIndex();
        if (provinceIdx < 0 || targetIdx < 0) {
            showAlert("请选择省份和要连接的省份");
            return;
        }
        String targetName = unconnectedList.getItems().get(targetIdx);
        int actualIdx = findProvinceIndex(targetName);
        if (actualIdx >= 0) {
            mapData.graph.addEdge(provinceIdx, actualIdx);
            refreshAdjacencyLists(provinceIdx);
        }
    }

    private void disconnectSelected() {
        int provinceIdx = provinceList.getSelectionModel().getSelectedIndex();
        int targetIdx = connectedList.getSelectionModel().getSelectedIndex();
        if (provinceIdx < 0 || targetIdx < 0) {
            showAlert("请选择省份和要断开的省份");
            return;
        }
        String targetName = connectedList.getItems().get(targetIdx);
        int actualIdx = findProvinceIndex(targetName);
        if (actualIdx >= 0) {
            mapData.graph.removeEdge(provinceIdx, actualIdx);
            refreshAdjacencyLists(provinceIdx);
        }
    }

    private int findProvinceIndex(String name) {
        for (int i = 0; i < mapData.provinces.size(); i++) {
            if (mapData.provinces.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    private void loadFile() {
        try {
            MapData data = MapFileHandler.load(currentFilePath);
            this.mapData = data;
            refreshProvinceList();
        } catch (Exception ex) {
            showAlert("加载失败：" + ex.getMessage());
        }
    }

    private void saveFile() {
        try {
            MapFileHandler.save(mapData, currentFilePath);
            showAlert("保存成功");
        } catch (Exception ex) {
            showAlert("保存失败：" + ex.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }
}
