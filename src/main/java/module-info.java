module com.example.messanger {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens com.example.messanger to javafx.fxml;
    exports com.example.messanger;
}