package com.codecool.klondike;

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

public class AlertBox {

    static boolean restart;

    public static boolean display(String title, String message) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(100);

        Label label = new Label();
        label.setText(message);

        Button noButton = new Button("No");
        noButton.setOnAction(e -> {
            restart = false;
            window.close();
        });

        Button yesButton = new Button("Yes");
        yesButton.setOnAction(e -> {
            restart = true;
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, noButton, yesButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

        return restart;
    }
}

