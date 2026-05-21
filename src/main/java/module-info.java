module org.example.tpv_angela {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.json;
    requires org.mongodb.driver.core;
    requires com.github.librepdf.openpdf;


    opens org.example.tpv_angela to javafx.fxml;
    exports org.example.tpv_angela;
    exports org.example.tpv_angela.controladores;
    opens org.example.tpv_angela.controladores to javafx.fxml;
    exports org.example.tpv_angela.controladores.camarero;
    opens org.example.tpv_angela.controladores.camarero to javafx.fxml;
    exports org.example.tpv_angela.controladores.admin;
    opens org.example.tpv_angela.controladores.admin to javafx.fxml;
    exports org.example.tpv_angela.controladores.cocinero;
    opens org.example.tpv_angela.controladores.cocinero to javafx.fxml;
}