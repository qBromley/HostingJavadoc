package gui.database;

import java.io.FileInputStream;
import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextInputDialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.*;
import java.util.Properties;

/**
 * @author Quinn Bromley
 * @author Kaghan Odom
 * @author Ethan Woods
 * @author Chance Hughes
 * @author Jacob Krivulla
 * @author Haden O'Keef
 * 
 */

 /**
  * <p>The GUI class represents a JavaFX application for database interaction.<p>
  */
public class GUI extends Application {
	
    /**
     * Start method to launch the JavaFX application.
     * 
     * @param stage The primary stage for this application.
     */
	@Override
	public void start(Stage stage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("pinpad.fxml"));
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();

			Connection conn = null;

			// try to connect to the database. If connection failed, pop up an alert window
			try {
				conn = this.connect();
			} catch (Exception e) {
				showAlert(AlertType.ERROR, "Database Access Error", "Error Connecting to Database.");
				e.printStackTrace();
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
				System.exit(0); // Exit the method
			}
			showAlert(AlertType.INFORMATION, "Database Connection", "Opened database successfully");

			String name = "";
			try {
				// Create a SQL Statement Object
				Statement stmt = conn.createStatement();

				// Create the SQL statement you want
				String sqlStatement = "SELECT * FROM inventory";

				// Send the statement to DBMS
				ResultSet result = stmt.executeQuery(sqlStatement);

				while (result.next()) {
					name += result.getString("name") + "\n";
				}

			} catch (Exception e) {
				showAlert(AlertType.ERROR, "Database Access Error", "Error accessing Database.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
     * The main method to launch the JavaFX application.
     * 
     * @param args Command-line arguments.
     */
	public static void main(String[] args) {
		launch(args);
	}

	
	/**
     * Shows an alert message.
     * 
     * @param alertType The type of alert (e.g., ERROR, INFORMATION).
     * @param title     The title of the alert.
     * @param message   The message to be displayed in the alert.
     */
	private void showAlert(AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	// Make a function to connect to our database and return the connection variable
	/**
     * Make a function to connect to our database and return the connection variable
     * 
     * @return A Connection object representing the database connection.
     */
    public Connection connect() {
		Properties props = new Properties();
		Connection conn = null;

		try {
			// Path to your properties file
			String homePath = System.getProperty("user.home");
			String propertiesPath = homePath + "/CSCE331/dbconfig.properties";
			props.load(new FileInputStream(propertiesPath));

			// Retrieve properties
			String url = props.getProperty("db.url");
			String user = props.getProperty("db.user");
			String password = props.getProperty("db.password");

			// Connect to the database
			conn = DriverManager.getConnection(url, user, password);
		} catch (IOException | SQLException e) {
			System.out.println(e.getMessage());
		}
		return conn;
	}

}