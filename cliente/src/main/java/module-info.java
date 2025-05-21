module co.com.cliente {
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
    requires com.jfoenix;
    requires javafx.media;
    requires opencv;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires org.json;
    requires redis.clients.jedis;

    opens co.com.cliente to javafx.fxml;
    exports co.com.cliente;
    exports co.com.cliente.controller;
    opens co.com.cliente.controller to javafx.fxml;
    exports co.com.cliente.httpRequest;
    exports co.com.cliente.dto;
    opens co.com.cliente.dto to com.fasterxml.jackson.databind;

    opens co.com.cliente.httpRequest to javafx.fxml;
}