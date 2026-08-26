package org.paginalibre.system;

// Archivo: Main.java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        Label label = new Label("Hola Mundo");


        Scene scene = new Scene(label, 300, 200);


        stage.setTitle("Mi primera ventana JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); 
    }
}


    

