import utils.ConnectConfig;
import utils.DatabaseConnector;
import utils.RandomData;
import entities.*;
import java.util.logging.Logger;

import org.apache.commons.lang3.RandomUtils;
import java.io.*;
import java.util.*;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());
    private static LibraryManagementSystem library;
    public static void main(String[] args) {
        try {
            // parse connection config from "resources/application.yaml"
            ConnectConfig conf = new ConnectConfig();
            log.info("Success to parse connect config. " + conf.toString());
            // connect to database
            DatabaseConnector connector = new DatabaseConnector(conf);
            boolean connStatus = connector.connect();
            library = new LibraryManagementSystemImpl(connector);
            if (!connStatus) {
                log.severe("Failed to connect database.");
                System.exit(1);
            }
            /* do somethings */
            System.out.println("Hello library!");
            GUI gui = new GUI(library);
            gui.show(1);
            
            // release database connection handler
            // if (connector.release()) {
            //     log.info("Success to release connection.");
            // } else {
            //     log.warning("Failed to release connection.");
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
