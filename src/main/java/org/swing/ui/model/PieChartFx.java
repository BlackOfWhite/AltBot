package org.swing.ui.model;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;

public class PieChartFx extends Application {

    private Group view;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Wallets Chart");
        Group root = new Group();
        this.view = root;

        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("January", 100),
                        new PieChart.Data("February", 200),
                        new PieChart.Data("March", 50),
                        new PieChart.Data("April", 75),
                        new PieChart.Data("May", 110),
                        new PieChart.Data("June", 300),
                        new PieChart.Data("July", 111),
                        new PieChart.Data("August", 30),
                        new PieChart.Data("September", 75),
                        new PieChart.Data("October", 55),
                        new PieChart.Data("November", 225),
                        new PieChart.Data("December", 99));

        final PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Wallets Chart");

        root.getChildren().add(pieChart);

        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
    }

    public Parent getView() {
        return view;
    }
}
