module dbms_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;

    exports dbms_project.Main;
    exports dbms_project.controller;
    exports dbms_project.database;

    opens dbms_project.controller to javafx.fxml;
    opens dbms_project.model to javafx.base;

    requires mysql.connector.j;
    requires jbcrypt;
    requires java.desktop;

}