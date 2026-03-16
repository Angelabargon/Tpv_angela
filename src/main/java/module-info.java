module org.example.tpv_angela {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.json;
    requires org.mongodb.driver.core;


    opens org.example.tpv_angela to javafx.fxml;
    exports org.example.tpv_angela;
    exports org.example.tpv_angela.controladores;
    opens org.example.tpv_angela.controladores to javafx.fxml;
}