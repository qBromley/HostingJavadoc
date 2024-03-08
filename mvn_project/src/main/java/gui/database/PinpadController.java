package gui.database;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
/**
 * The Pinpad Controller hold the logic for the pinpad FXML file, it is the file that is loaded first, and is responsable for opeaning the manager or employee view
 */


public class PinpadController {
    private Stage stage;
	private Scene scene;
	private Parent root;
    private String pin = "";

    /**
     * @param  employeeNum This takes in a int that will be compared to the employee numbers in the datbase, 
     * if there is a match it will return the role of the employee manager or casheir.
     * @return this method returns a string of the employee role
     */
    private String fetchNumberFromDatabase(int employeeNum) {
     
      double price = 0.0;
      try (Connection conn = this.connect()) {
          // Prepare SQL statement to retrieve price based on product name
          String sql = "SELECT EmployeeRole FROM employee WHERE employeeID = ?";
          try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
              pstmt.setInt( 1, employeeNum);
              try (ResultSet rs = pstmt.executeQuery()) {
                  // If a record is found, retrieve the price
                  if (rs.next()) {
                      return rs.getString("EmployeeRole");
                  }
              }
          }
      } catch (SQLException e) {
          e.printStackTrace();
          // Handle any errors gracefully
      }
      return "";
    }
    /** <p> this function implements the backspace function on the pin pad <p> 
     * @param event the button press
    */
    @FXML
    void back(ActionEvent event) {
        int length = pin.length();
        if(pin != ""){
            pin = pin.substring(0, length - 1);
        }
    }
    /** hold the logic for the enter function, it checks if the number matches any value of the database. 
     * from there it checks the value returned from {@link fetchNumberFromDatabase} function. If the role is a cashier it will bring up cashier page.
     * if the role is a manager the manager page will be brought up.
     * 
     */
    @FXML
    /**
     * checks the pin entered against the pins in the database and will open GUI accordingly
     * @param event the button press
     */
    void enter(ActionEvent event) {
        
        if(fetchNumberFromDatabase(Integer.parseInt(pin)).equals("Cashier"))
        {
            try {
			root = FXMLLoader.load(getClass().getResource("Page1.fxml"));
			stage = (Stage)((Node)event.getSource()).getScene().getWindow();
			scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
        else if((fetchNumberFromDatabase(Integer.parseInt(pin)).equals("Manager")))
        {
            try {
			root = FXMLLoader.load(getClass().getResource("Manager.fxml"));
			stage = (Stage)((Node)event.getSource()).getScene().getWindow();
			scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
        }
       
        pin = "";
        

    }
    
    /** this pinpad function holds the functionality of the pin pad, it will add numbers to the sring pin creating the pin that the user enterd */
    /**
     * this pinpad function holds the functionality of the pin pad, it will add numbers to the sring pin creating the pin that the user enterd
     * @param event the event is the button press of any number key
     */
    @FXML
    void pinpad(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Button) {

            Button btn = (Button) source;
            String buttonText = btn.getText();
            switch(buttonText){
                case("1"):
                    pin = pin+"1";
                break;
                case("2"):
                    pin = pin+"2";
                break;
                case("3"):
                    pin = pin+"3";
                break;
                case("4"):
                    pin = pin+"4";
                break;
                case("5"):
                    pin = pin+"5";
                break;
                case("6"):
                    pin = pin+"6";
                break;
                case("7"):
                    pin = pin+"7";
                break;
                case("8"):
                    pin = pin+"8";
                break;
                case("9"):
                    pin = pin+"9";
                break;
                case("0"):
                    pin = pin+"0";
                break;
            }
        }
    }

    // Make a function to connect to our database and return the connection variable
	/**
     * Establishes a connection to the database.
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