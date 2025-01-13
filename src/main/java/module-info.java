module com.teamb.textprocessingtoolteamproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.teamb.textprocessingtoolteamproject to javafx.fxml;
    opens com.teamb.textprocessingtoolteamproject.controller to javafx.fxml;
    opens com.teamb.textprocessingtoolteamproject.model to javafx.base;
    exports com.teamb.textprocessingtoolteamproject;
    exports com.teamb.textprocessingtoolteamproject.controller;
}