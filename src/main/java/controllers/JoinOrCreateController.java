package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


public class JoinOrCreateController {

    @FXML
    public Button btnJoin;
    @FXML
    public Button btnCreate;
    @FXML
    public TextField textField;

    @FXML
    void initialize() {
        assert btnCreate != null : "fx:id=\"btnCreate\" was not injected: check your FXML file 'joinOrCreate.fxml'.";
        assert btnJoin != null : "fx:id=\"btnJoin\" was not injected: check your FXML file 'joinOrCreate.fxml'.";
        assert textField != null : "fx:id=\"textField\" was not injected: check your FXML file 'joinOrCreate.fxml'.";
    }

}
