package com.codecool.klondike;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

public class AlertBox {

    public static void display(String title, String message) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(200);

        Label label = new Label();
        label.setText(message);

        Button exitButton = new Button("Exit game");
        exitButton.setOnAction(e -> {
            System.exit(1);
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, exitButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.show();
    }
}

