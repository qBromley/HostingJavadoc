package gui.database;

import javafx.scene.Node;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * <p>Controller for the Manager GUI and FXML file<p>
 * this handels all the graphical components of our GUI and displaying the recipt order total. The logic for our buttons and what they do is also here.
 * 
 */

public class ManagerController {
    /**
     * The stage for the manager interface.
     */
    private Stage stage;
    /**
     * The scene for the manager interface.
     */
    private Scene scene;
    /**
     * The root node for the manager interface.
     */
    private Parent root;
    /**
   * Text representing the current order.
   */
	  public String orderText = "";
    /**
     * The main page indicator.
     */
	  public int mainPage = 2;
    /**
     * The order number.
     */
    public int orderNumber = 0;
    /**
     * Decimal format for currency.
     */
    DecimalFormat df = new DecimalFormat("#.##");

    // Make a function to connect to our database and return the connection variable
    /**
     * Establishes a connection to the database.
     * 
     * @return The connection object.
     */

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

  // @param productName Define a method to fetch prices from the database
  /**
   * A function that retrieves the cost of an item from the database
   * @param productName takes in the name of the product
   * @return returns the value of the product set in the database
   */
  private double fetchPriceFromDatabase(String productName) {
      double price = 0.0;

      // Connect to the database through connect()
      try (Connection conn = this.connect();) {
          // Prepare SQL statement to retrieve price based on product name
          String sql = "SELECT price FROM products WHERE productname = ?";
          try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
              pstmt.setString(1, productName);
              try (ResultSet rs = pstmt.executeQuery()) {
                  // If a record is found, retrieve the price
                  if (rs.next()) {
                      price = rs.getDouble("price");
                  }
              }
          }
      } catch (SQLException e) {
          e.printStackTrace();
          // Handle any errors gracefully
      }
      return price;
  }
/**
 * Genereates an order ID
 * @return an integer that is the order id
 */
  private int generateUniqueOrderId() {
    // SQL query to get the last order_id
    int lastOrderId = 0;
    String sql = "SELECT MAX(order_id) AS last_order_id FROM orders";

    // Connect to the database
    try (Connection conn = this.connect();
          PreparedStatement pstmt = conn.prepareStatement(sql);
          ResultSet rs = pstmt.executeQuery()) {

        // Check if a result is returned
        if (rs.next()) {
            lastOrderId = rs.getInt("last_order_id");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        // Handle any errors gracefully
    }

    // Return the next order_id
    return lastOrderId + 1;
  }

    // New method to add order to the database
    /**
     * New method to add order to the database
     * @param orderTotal the total of the customers order
     * @param orderid the ID of the customers order
     * @param productIds a vector of the product IDs
     */
    private void addOrderToDatabase(double orderTotal, int orderid, List<Integer> productIds) {
      // Get the current date
      LocalDate currentDate = LocalDate.now();
      int year = currentDate.getYear();
      int weekOfYear = currentDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
      int day = currentDate.getDayOfMonth();

      // Fetch the current time
      LocalTime currentTime = LocalTime.now(); 
      Time sqlTime = Time.valueOf(currentTime);

      // Generate a unique order ID (this needs to be implemented according to your database design)
      int orderId = orderid;

      // SQL statement to insert the new order
      String sql = "INSERT INTO orders (order_id, order_year, order_week, order_day, price, order_time) VALUES (?, ?, ?, ?, ?, ?)";

      try (Connection conn = this.connect();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {

          // Set parameters
          pstmt.setInt(1, orderId);
          pstmt.setString(2, String.valueOf(year));
          pstmt.setString(3, String.valueOf(weekOfYear));
          pstmt.setString(4, String.valueOf(day));
          pstmt.setDouble(5, orderTotal);
          pstmt.setTime(6, sqlTime);

          // Execute update
          pstmt.executeUpdate();
          

          // now update the order_products table

          String sql2 = "INSERT INTO order_products (order_product_id, order_id, product_id) VALUES (?, ?, ?)";

          for (Integer productId : productIds) {
            // Generate a unique order_product_id
            // Assuming you have a method to generate this or you can use auto-increment in SQL.
            int orderProductId = generateUniqueOrderProductId(); 

            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setInt(1, orderProductId);
                pstmt2.setInt(2, orderId);
                pstmt2.setInt(3, productId);
                pstmt2.executeUpdate();
            }
        }
      } catch (SQLException e) {
          e.printStackTrace();
          // Handle any errors gracefully
      }
    }
    /**
     *  this function gets the inventory Items that are used in a product
     * @param productId takes in the product ID for a product
     * @return the inventory items in a hashmap
     */
    private Map<Integer, Integer> getInventoryItemsForProduct(int productId) {
      Map<Integer, Integer> inventoryItems = new HashMap<>();
      String sql = "SELECT inventory_id FROM product_inventory WHERE product_id = ?";

      try (Connection conn = this.connect();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {

          pstmt.setInt(1, productId);
          ResultSet rs = pstmt.executeQuery();

          while (rs.next()) {
              int inventoryId = rs.getInt("inventory_id");
              inventoryItems.put(inventoryId, 1); // Assuming each product uses a quantity of 1 from each inventory item
          }
      } catch (SQLException e) {
          System.out.println(e.getMessage());
      }
      return inventoryItems;
    }
    /**
     * A function that updates the quantity of an item as it is being orderd
     * @param inventoryId takes in the inventory ID of an item
     * @param quantityUsed takes in how much of an item is used
     */
    private void updateInventoryQuantity(int inventoryId, int quantityUsed) {
      String sql = "UPDATE inventory SET quantity = quantity - ? WHERE id = ?";
  
      try (Connection conn = this.connect();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {
  
          pstmt.setInt(1, quantityUsed);
          pstmt.setInt(2, inventoryId);
          pstmt.executeUpdate();
      } catch (SQLException e) {
          System.out.println(e.getMessage());
      }
    }
/**
 * generates a productID for the orderProduct junction table
 * @return returns an integer for productID
 */
    private int generateUniqueOrderProductId() {
      int lastId = 0;
      String sql = "SELECT MAX(order_product_id) AS last_id FROM order_products";
  
      try (Connection conn = this.connect();
           PreparedStatement pstmt = conn.prepareStatement(sql);
           ResultSet rs = pstmt.executeQuery()) {
  
          if (rs.next()) {
              lastId = rs.getInt("last_id");
          }
      } catch (SQLException e) {
          System.out.println(e.getMessage());
      }
  
      return lastId + 1;
    }
  
    @FXML
    private Button Button00;

    @FXML
    private Button Button01;

    @FXML
    private Button Button02;

    @FXML
    private Button Button03;

    @FXML
    private Button Button10;

    @FXML
    private Button Button11;

    @FXML
    private Button Button12;

    @FXML
    private Button Button20;

    @FXML
    private Button Button21;

    @FXML
    private Button Button22;

    @FXML
    private Button Button23;


    @FXML
    private Button Button30;

    @FXML
    private Button Button31;

    @FXML
    private Button Button32;

    @FXML
    private Button Button33;

    @FXML
    private Rectangle ChxSandwitch;

    @FXML
    private Label Label_1;

    @FXML
    private Label Label_2;

    @FXML
    private Label Label_3;

    @FXML
    private Label Label_4;

    @FXML
    private TextArea Order;
    
    @FXML
    private Button Chip_Combo;
    
    @FXML
    private Button Ranch;

    @FXML
    private Button SpyRanch;

    @FXML
    private Button Combo;

    @FXML
    private Button Fry_Combo;

    @FXML
    private Button Gigem;

    @FXML
    private Button HoneyMust;

    @FXML
    private Button Bean_Patty;

    @FXML
    private Label Total;

    @FXML 
    private Button seasonalButton;
   
    private String checkText = "";
    private double itemCost = 0.0;
    private double orderTotal = 0.0;
    private double totalAfterTax = 0.0;
    private double combo = 1.90;

    private List<Integer> orderedProductIds = new ArrayList<>();
  
    public String seasonal = "";
    LocalDate currentDate = LocalDate.now();
    int month = currentDate.getMonthValue();

    /**
     * Initializes the controller after its root element has been completely processed.
     */
    public void initialize(){
      if(month>11 || month<3){
        seasonal="winter special";
      }
      if(month>2 && month<6){
        seasonal="spring special";
      }
      if(month>5 && month<9){
        seasonal="summer special";
      }
      if(month>8 && month<12){
        seasonal="fall special";
      }
      seasonalButton.setText(seasonal);
    }

    
    
    /**
     * Handles seasonal selection events.
     * 
     * @param event The ActionEvent object.
     */
    @FXML
    void Seasonal(ActionEvent event)
    {
      Object source = event.getSource();
        if (source instanceof Button) {

        Button btn = (Button) source;
        String buttonText = btn.getText();

        
        switch(buttonText)
        {
          case("winter special"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Winter Special : "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;
          Total.setText("$"+Double.parseDouble(df.format(orderTotal)));
        break;
        case("spring special"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Spring Special : "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;
          Total.setText("$"+Double.parseDouble(df.format(orderTotal)));
        break;
        case("summer special"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Summer Special : "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;
          Total.setText("$"+Double.parseDouble(df.format(orderTotal)));
        break;
        case("fall special"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Fall Special : "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;
          Total.setText("$"+Double.parseDouble(df.format(orderTotal)));
        break;
        }
      }
    }
   
    /**
     * The logic for all of our buttons depending on what the button is named it will add that to the order text.
     * Sets buttons visible and not visible based on what the user is ordering.
     * adds up our order total.
     * 
   
     * @param checkText The text that displays on the check
     * @param itemCost  the cost of each item that displays on the check
     * @param orderTotal the order total that displays below the check
     * @param totalAfterTax the total after tax that displays on the check
     * @param combo the set price for a combo
     *
     * @param event The ActionEvent object.
     */
    @FXML
    void AddItem(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Button) {

        Button btn = (Button) source;
        String buttonText = btn.getText();

        
        switch(buttonText) {
        case ("Revs"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Rev's Burger: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(true);
          Chip_Combo.setVisible(true);
          Fry_Combo.setVisible(true);
          Combo.setVisible(false);
          orderTotal+=itemCost;

          orderedProductIds.add(1); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("DBL CHZ"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Double Stack Cheese Burger: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(true);
          Bean_Patty.setVisible(true);
          Chip_Combo.setVisible(true);
          Fry_Combo.setVisible(true);
          Combo.setVisible(false);
          orderTotal+=itemCost;

          orderedProductIds.add(2); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Bacon"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Bacon Cheeseburger: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(true);
          Chip_Combo.setVisible(true);
          Fry_Combo.setVisible(true);
          Combo.setVisible(false);
          orderTotal+=itemCost;

          orderedProductIds.add(4); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("classic"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Classic Burger: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(true);
          Chip_Combo.setVisible(true);
          Fry_Combo.setVisible(true);
          Combo.setVisible(false);
          orderTotal+=itemCost;

          orderedProductIds.add(3); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("3 Tender"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Three Tender Basket: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(false);
          Chip_Combo.setVisible(false);
          Fry_Combo.setVisible(false);
          Combo.setVisible(true);
          orderTotal+=itemCost;

          orderedProductIds.add(5); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Steak Finger"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Four Steak Finger Basket: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(false);
          Chip_Combo.setVisible(false);
          Fry_Combo.setVisible(false);
          Combo.setVisible(true);
          orderTotal+=itemCost;

          orderedProductIds.add(6); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Patty Melt"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Patty Melt: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(false);
          Chip_Combo.setVisible(true);
          Fry_Combo.setVisible(true);
          Combo.setVisible(false);
          orderTotal+=itemCost;

          orderedProductIds.add(7); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Spicy Chkn Sndwch"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Spicy Chicken Strip Sandwich: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(false);
          Chip_Combo.setVisible(true);
          Fry_Combo.setVisible(true);
          Combo.setVisible(false);
          orderTotal+=itemCost;

          orderedProductIds.add(8); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Grilled Chz"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Grilled Cheese: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(false);
          Chip_Combo.setVisible(true);
          Fry_Combo.setVisible(true);
          Combo.setVisible(false);
          orderTotal+=itemCost;

          orderedProductIds.add(10); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Chkn tndr sndwch"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Chicken Tender Sandwich "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(false);
          Chip_Combo.setVisible(true);
          Fry_Combo.setVisible(true);
          Combo.setVisible(false);
          orderTotal+=itemCost;

          orderedProductIds.add(9); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Salad Bar"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Salad Bar: "+itemCost+ '\n' ;
          Order.setText(orderText);
          Bean_Patty.setVisible(false);
          Chip_Combo.setVisible(false);
          Fry_Combo.setVisible(false);
          Combo.setVisible(false);
          orderTotal+=itemCost;

          orderedProductIds.add(18); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Chocolate Shake"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Chocolate Shake: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(11); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Vanilla Shake"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Vanilla Shake: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(12); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Strawberry Shake"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Strawberry Shake: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(13); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Cappuccino Shake"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Cappuccino Shake "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(14); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Ice Cream"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Ice Cream: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(15); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Choc chip cookie"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Chocolate Chip Chunk Cookie: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(16); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Brownie"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Chocolate Fundge Brownie: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(17); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Fries"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Seasoned Fries: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(23); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Chips"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Kettle Chips: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(26); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Tater Tots"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Tater Tots: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(24); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Onion rings"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Onion Rings: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(25); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case("sm Drink"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Small Fountain Drink: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(19); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("lg Drink"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Large Fountain Drink: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(20); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Coffee"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Drip Coffee: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(21); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case ("Cold brew"):
          itemCost = fetchPriceFromDatabase(buttonText);
          orderText = orderText+"Cold Brew: "+itemCost+ '\n' ;
          Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(22); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case("Chip Combo"):
        	orderText+= "\t"+"Chip Combo "+ combo+'\n';
        	Order.setText(orderText);
          Chip_Combo.setVisible(false);
          Fry_Combo.setVisible(false);
          Combo.setVisible(false);
          orderTotal+=combo;

          orderedProductIds.add(26); // make integer the product_id of our menu item
          orderedProductIds.add(20); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case("Fry Combo"):
        	orderText+= "\t"+"Fry Combo "+ combo+'\n';
        	Order.setText(orderText);
        	Chip_Combo.setVisible(false);
          Fry_Combo.setVisible(false);
          Combo.setVisible(false);
          orderTotal+=combo;

          orderedProductIds.add(23); // make integer the product_id of our menu item
          orderedProductIds.add(20); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case("Combo"):
        	orderText+= "\t"+"Combo "+ combo+'\n';
        	Order.setText(orderText);
        	Chip_Combo.setVisible(false);
          Fry_Combo.setVisible(false);
          Combo.setVisible(false);
          orderTotal+=combo;

          orderedProductIds.add(20); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
        break;
        case("Bean Patty"):
        	orderText+="\t"+" Bean Patty"+ "\n";
        	Order.setText(orderText);
        	Bean_Patty.setVisible(false);

          // No product for bean patty? 

          Total.setText(df.format(orderTotal));
        break;
        case("Gig em"):
          itemCost = fetchPriceFromDatabase(buttonText);
	        orderText = orderText+"Gig em sauce : "+itemCost+ '\n' ;
	        Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(27); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
	      break;
        case("Buffalo"):
          itemCost = fetchPriceFromDatabase(buttonText);
	        orderText = orderText+"Buffalo : "+itemCost+ '\n' ;
	        Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(28); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
	      break;
        case("BBQ"):
          itemCost = fetchPriceFromDatabase(buttonText);
	        orderText = orderText+"BBQ : "+itemCost+ '\n' ;
	        Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(29); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
	      break;
        case("Honey must"):
          itemCost = fetchPriceFromDatabase(buttonText);
	        orderText = orderText+"Honey must : "+itemCost+ '\n' ;
	        Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(30); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
	      break;
        case("Spy Ranch"):
          itemCost = fetchPriceFromDatabase(buttonText);
	        orderText = orderText+"Spy Ranch : "+itemCost+ '\n' ;
	        Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(31); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
	      break;
        case("Ranch"):
          itemCost = fetchPriceFromDatabase(buttonText);
	        orderText = orderText+"Ranch : "+itemCost+ '\n' ;
	        Order.setText(orderText);
          orderTotal+=itemCost;

          orderedProductIds.add(32); // make integer the product_id of our menu item

          Total.setText(df.format(orderTotal));
	      break;
        
        }
      }

    }

    /**
     * Logs out the current user back to pinpad gui.
     * 
     * @param event The ActionEvent object.
     */
    @FXML
    void Logout(ActionEvent event) {
       try {
			root = FXMLLoader.load(getClass().getResource("pinpad.fxml"));
			stage = (Stage)((Node)event.getSource()).getScene().getWindow();
			scene = new Scene(root);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
    }
    /**
     * Confirms the current order to the database.
     * 
     * @param event The ActionEvent object.
     */
    @FXML
    void Confirm_Order(ActionEvent event) {
      orderNumber = generateUniqueOrderId();
      totalAfterTax = orderTotal*1.08;
      LocalTime currentTime = LocalTime.now();
      orderText=orderText+"\n"+"Time of order: "+currentTime;
      orderText=orderText+"\n"+"Order Number: "+orderNumber;
      orderText=orderText+"\n"+"Order Total Plus Tax: $"+Double.parseDouble(df.format(totalAfterTax));
      Order.setText(orderText);
      Total.setText("$"+Double.parseDouble(df.format(totalAfterTax)));
      addOrderToDatabase(Double.parseDouble(df.format(totalAfterTax)), orderNumber, orderedProductIds);

      // Now update our inventory for the products that were used
      for (Integer productId : orderedProductIds) {
        Map<Integer, Integer> inventoryItems = getInventoryItemsForProduct(productId);
        for (Map.Entry<Integer, Integer> entry : inventoryItems.entrySet()) {
            int inventoryId = entry.getKey();
            int quantityUsed = entry.getValue();
            updateInventoryQuantity(inventoryId, quantityUsed);
        }
      }
      
      // clear our orderedProductIds for the next order
      orderedProductIds.clear();
    }
    /**
     * Initiates a new order.
     * 
     * @param event The ActionEvent object.
     */
    @FXML
    void New_Order(ActionEvent event) {
      orderText="";
      orderTotal=0.0;
      totalAfterTax=0.0;
      Order.setText(orderText);
      Total.setText(orderText);
      
      // clear our orderedProductIds for the next order
      orderedProductIds.clear();
    }
    /**
     * Switches to the manager's data page.
     * 
     * @param event The ActionEvent object.
     */
    @FXML
    void Switch_Page(ActionEvent event)
    {
      Page3Controller newWindow = new Page3Controller();
      newWindow.start(new Stage());
    }
    /**
     * Switches to another page of buttons.
     * 
     * @param event The ActionEvent object.
     */
    @FXML
    void switchPage(ActionEvent event) {
    	if(mainPage%2==0){
        Button00.setText("Chocolate Shake");
        Button01.setText("Vanilla Shake");
        Button02.setText("Strawberry Shake");
        Button03.setText("Cappuccino Shake");
        Button10.setText("Choc chip cookie");
        Button11.setText("Ice Cream");
        Button12.setText("Brownie");
        Button12.setVisible(true);
        Button20.setText("Fries");
        Button21.setText("Chips");
        Button22.setText("Tater Tots");
        Button23.setText("Onion rings");
        Button30.setText("sm Drink");
        Button30.setVisible(true);
        Button31.setText("lg Drink");
        Button31.setVisible(true);
        Button32.setText("Coffee");
        Button32.setVisible(true);
        Button33.setText("Cold brew");
        //Button33.setVisible(true);
        Label_1.setText("Shakes");
        Label_2.setText("Sweets");
        Label_3.setText("Sides");
        
        Label_4.setText("Drinks");
        Label_4.setVisible(true);
        Bean_Patty.setVisible(false);
        Chip_Combo.setVisible(false);
        Fry_Combo.setVisible(false);
        Combo.setVisible(false);
        mainPage+=1;
           
      }
    	else{
        Button00.setText("Revs");
        Button01.setText("DBL CHZ");
        Button02.setText("Bacon");
        Button03.setText("classic");
        Button10.setText("3 Tender");
        Button11.setText("Steak Finger");
        Button12.setVisible(false);
        
        Button20.setText("Patty Melt");
        Button21.setText("Spicy Chkn Sndwch");
        Button22.setText("Chkn tndr sndwch");
        Button23.setText("Grilled Chz");
        Label_1.setText("Burgers");
        Label_2.setText("Baskets");
        Label_3.setText("Sandwitches");
        Label_4.setVisible(false);
        Button30.setVisible(false);
        Button31.setVisible(false);
        Button32.setVisible(false);
        //Button33.setVisible(false);
        Button33.setText("Salad Bar");
        mainPage+=1;
        
      }
    }
    
}
    