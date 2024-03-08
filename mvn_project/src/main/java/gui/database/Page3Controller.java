package gui.database;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * JavaFX application for managing inventory and displaying sales data.
 */
public class Page3Controller extends Application {

    /**
     * Represents an inventory item with an ID and a name.
     */
    public class InventoryItem {
        public int id;
        public String name;

        /**
         * Constructs an InventoryItem with the specified ID and name.
         *
         * @param id   The ID of the inventory item.
         * @param name The name of the inventory item.
         */
        public InventoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        // Override the toString method
        @Override
        public String toString() {
            return id + " - " + name;
        }
    }


    private AnchorPane anchorPane = new AnchorPane();
    private String year = null;
    private String week = null;
    private String day = null;
    private String start_time = null;
    private String end_time = null;
    /**
     * Starts the JavaFX application.
     *
     * @param primaryStage The primary stage of the application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage = Initialize(primaryStage);

        double stagewidth = primaryStage.getWidth();
        double stageheight = primaryStage.getHeight();
        Scene curr_scene = primaryStage.getScene();
        curr_scene.widthProperty().addListener(
                (observable, oldValue, newValue) -> adjustGUI(stagewidth, stageheight));

        primaryStage.show();
    }

    /**
     * The sales data class holds all the data for the sales including productID sproductName quantitySold and totalSales
     */
    public class SalesData {
        private int productId;
        private String productName;
        private int quantitySold;
        private double totalSales;
    
        // Constructor, getters, and setters
        // Constructor
        /**
         * 
         * @param productId The product ID for a products
         * @param productName The name for a product
         * @param quantitySold the quantity sold of a product
         * @param totalSales The total sales we have made
         */
        public SalesData(int productId, String productName, int quantitySold, double totalSales) {
            this.productId = productId;
            this.productName = productName;
            this.quantitySold = quantitySold;
            this.totalSales = totalSales;
        }
        
        /**
         * gets the product ID
         * @return the product ID from the sales data
         */
        public int getProductId() {
            return productId;
        }
         /**
         * gets the product name
         * @return the product name from the sales data
         */
        public String getProductName() {
            return productName;
        }
    /**
    * gets the quantity sold
    * @return the quantity sold from the sales data
    */
        public int getQuantitySold() {
            return quantitySold;
        }
         /**
         * gets the Total sales
         * @return the Total sales from the sales data
         */
        public double getTotalSales() {
            return totalSales;
        }
    }

   /**
    *  holds the trend data from our orders and sales
    */
    public class trendData {
        private String productNameOne;
        private String productNameTwo;
        private int freq;
    
        // Constructor, getters, and setters
        /**
         * gets the trends for two different proucts
         * @param productNameOne the name of a product
         * @param productNameTwo the name of a product
         * @param freq the frequency that the two products were ordered together
         */
        public trendData(String productNameOne, String productNameTwo, int freq) {
            this.productNameOne = productNameOne;
            this.productNameTwo = productNameTwo;
            this.freq = freq;
        }
        /**
         * gets the product name
         * @return the product name of the first product
         */
        public String getProductNameOne() {
            return productNameOne;
        }
          /**
         * gets the product name
         * @return the product name of the second product
         */
        public String getProductNameTwo() {
            return productNameTwo;
        }
          /**
         * gets the frequency varible
         * @return the frequency that the two items occured with one another
         */
        public int getFreq() {
            return freq;
        }
    }
    /**
     * Fetches trend data from the database based on the selected parameters.
     *
     * @return A list of TrendData objects representing the fetched trend data.
     */
    private List<trendData> fetchTrendData() {
        List<trendData> trendDataList = new ArrayList<>();
        String query = "SELECT " +
            "p1.productname AS product_name_1, " +
            "p2.productname AS product_name_2, " +
            "COUNT(*) AS frequency " +
            "FROM " +
            "order_products op1 " +
            "JOIN order_products op2 ON op1.order_id = op2.order_id AND op1.product_id < op2.product_id " +
            "JOIN orders o ON op1.order_id = o.order_id " +
            "JOIN products p1 ON op1.product_id = p1.product_id " +
            "JOIN products p2 ON op2.product_id = p2.product_id " +
            "WHERE o.order_time BETWEEN '" + start_time + "' AND '" + end_time + "' " +
            "GROUP BY p1.productname, p2.productname " +
            "ORDER BY frequency DESC " +
            "LIMIT 10;";
    
        try (Connection connection = this.connect();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query)) {
    
            while (resultSet.next()) {
                String productNameOne = resultSet.getString("product_name_1");
                String productNameTwo = resultSet.getString("product_name_2");
                int freq = resultSet.getInt("frequency");
                trendDataList.add(new trendData(productNameOne, productNameTwo, freq));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return trendDataList;
    }

    /**
     * Displays trend data in a dialog window.
     *
     * @param trendDataList The list of trendData objects to display.
     */
    private void displaytrendDataDialog(List<trendData> trendDataList) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Trend Data");

        TableView<trendData> tableView = new TableView<>();
        tableView.setItems(FXCollections.observableArrayList(trendDataList));

        TableColumn<trendData, String> productNameOneColumn = new TableColumn<>("Product Name 1");
        productNameOneColumn.setCellValueFactory(new PropertyValueFactory<>("productNameOne"));

        TableColumn<trendData, String> productNameTwoColumn = new TableColumn<>("Product Name 2");
        productNameTwoColumn.setCellValueFactory(new PropertyValueFactory<>("productNameTwo"));

        TableColumn<trendData, Integer> freqColumn = new TableColumn<>("Frequency");
        freqColumn.setCellValueFactory(new PropertyValueFactory<>("freq"));
        
        // Set the preferred width for the TableView
        tableView.setPrefWidth(500);

        // Add columns to the TableView
        tableView.getColumns().addAll(productNameOneColumn, productNameTwoColumn, freqColumn);

        dialog.getDialogPane().setContent(tableView);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        dialog.showAndWait();
    }




    /**
     * Initializes the GUI layout and sets up event handlers for various components.
     *
     * @param primaryStage The primary stage of the application.
     * @return The initialized primary stage.
     */
    private Stage Initialize(Stage primaryStage){
        Label yearLabel = new Label("Year:");
        TextField yearField = new TextField();
        yearField.setPromptText("Enter year (e.g., 2024)");

        Label weekLabel = new Label("Week:");
        TextField weekField = new TextField();
        weekField.setPromptText("Enter week (1-52)");

        Label dayLabel = new Label("Day:");
        TextField dayField = new TextField();
        dayField.setPromptText("Enter day (1-7)");

        AnchorPane.setTopAnchor(yearLabel, 0.0);
        AnchorPane.setLeftAnchor(yearLabel, 0.0);
        AnchorPane.setTopAnchor(weekLabel, 25.0);
        AnchorPane.setLeftAnchor(weekLabel, 0.0);
        AnchorPane.setTopAnchor(dayLabel, 50.0);
        AnchorPane.setLeftAnchor(dayLabel, 0.0);

        AnchorPane.setTopAnchor(yearField, 0.0);
        AnchorPane.setLeftAnchor(yearField, 60.0);
        AnchorPane.setTopAnchor(weekField, 25.0);
        AnchorPane.setLeftAnchor(weekField, 60.0);
        AnchorPane.setTopAnchor(dayField, 50.0);
        AnchorPane.setLeftAnchor(dayField, 60.0);

        yearField.textProperty().addListener((observable, oldValue, newValue) -> {
            year = newValue;
        });

        weekField.textProperty().addListener((observable, oldValue, newValue) -> {
            week = newValue;
        });

        dayField.textProperty().addListener((observable, oldValue, newValue) -> {
            day = newValue;
        });



        TextField managerNameField = new TextField();
        managerNameField.setPromptText("Manager name");

        ListView<InventoryItem> inventoryListView = new ListView<>();
        inventoryListView.setPrefWidth(300); // Set preferred width to avoid horizontal scrolling

        List<InventoryItem> inventoryList = fetchInventoryData();
        // Populate inventory list
        inventoryListView.getItems().addAll(inventoryList);
        inventoryListView.setOnMouseClicked(event -> {
            // Open a new window with details of the selected inventory item
            // Implement this logic based on your requirements

            // Example: new
            // InventoryItemDetailsWindow(inventoryListView.getSelectionModel().getSelectedItem());
        });

        Button crudProductButton = new Button("CRUD Product");
        Button crudInventoryButton = new Button("CRUD Inventory");

        // Add event handlers to buttons if needed
        crudProductButton.setOnAction(event -> {
            productCRUD productWindow = new productCRUD();
            productWindow.start(new Stage());
        });

        crudInventoryButton.setOnAction(event -> {
            inventoryCRUD inventoryWindow = new inventoryCRUD();
            inventoryWindow.start(new Stage());
        });

        // Create layout for buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.getChildren().addAll(crudProductButton, crudInventoryButton);

        // Create scroll pane and add the content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(anchorPane);

        // Add components to layout
        anchorPane.getChildren().addAll(yearLabel, yearField, weekLabel, weekField, dayLabel, dayField, managerNameField, inventoryListView, buttonsBox);

        double windowWidth = 800;

        inventoryListView.setMaxWidth(windowWidth * 0.5);

        // Anchor components to their positions

        AnchorPane.setTopAnchor(managerNameField, 10.0);
        AnchorPane.setRightAnchor(managerNameField, 10.0);
        AnchorPane.setTopAnchor(inventoryListView, 75.0);
        AnchorPane.setLeftAnchor(inventoryListView, windowWidth * 0.25);
        AnchorPane.setRightAnchor(inventoryListView, windowWidth * 0.25);

        AnchorPane.setTopAnchor(buttonsBox, AnchorPane.getTopAnchor(inventoryListView) + 400 + 10);
        AnchorPane.setLeftAnchor(buttonsBox, 10.0);

        //start time and end time
        Label startLabel = new Label("Start Time:");
        TextField startField = new TextField();
        startField.setPromptText("Enter start time (10:00 - 16:00)");

        Label endLabel = new Label("End Time:");
        TextField endField = new TextField();
        endField.setPromptText("Enter end time (10:00 - 16:00)");

        AnchorPane.setTopAnchor(startLabel, AnchorPane.getTopAnchor(buttonsBox) + 25);
        AnchorPane.setLeftAnchor(startLabel,  0.0);
        AnchorPane.setTopAnchor(endLabel, AnchorPane.getTopAnchor(buttonsBox) + 50);
        AnchorPane.setLeftAnchor(endLabel, 0.0);
       

        AnchorPane.setTopAnchor(startField, AnchorPane.getTopAnchor(buttonsBox) + 25);
        AnchorPane.setLeftAnchor(startField, 60.0);
        AnchorPane.setTopAnchor(endField, AnchorPane.getTopAnchor(buttonsBox) + 50);
        AnchorPane.setLeftAnchor(endField, 60.0);
        
        anchorPane.getChildren().addAll(startLabel, startField, endLabel, endField);

        startField.textProperty().addListener((observable, oldValue, newValue) -> {
            start_time = newValue;
        });

        endField.textProperty().addListener((observable, oldValue, newValue) -> {
            end_time = newValue;
        });

        
        // drop down menu to the left
        ComboBox<String> comboBox = new ComboBox<>();
        ObservableList<String> trends = FXCollections.observableArrayList(
            "Product Usage Chart",
            "Sales Report",
            "What Sells Together"
            
        );
        comboBox.setItems(trends);
        anchorPane.getChildren().add(comboBox);
        AnchorPane.setTopAnchor(comboBox, AnchorPane.getTopAnchor(buttonsBox) + 20 + 200);
        AnchorPane.setLeftAnchor(comboBox, 0.0);

        // Declare a field to hold the reference to the added Node
        Node[] addedNode = new Node[1]; 

        comboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && addedNode[0] != null) {
                anchorPane.getChildren().remove(addedNode[0]); // Remove the added Node from the AnchorPane
                addedNode[0] = null; // Set the reference to null
            }

            // Check if newValue is not null and add the Node based on the selected index
            if (newValue != null) {
                int selectedIndex = newValue.intValue(); // Get the selected index
                if (selectedIndex == 0) {
                    // Add the BarChart for index 0 (Product Usage Chart)
                    addedNode[0] = createBarChart(); // Create the BarChart
                    AnchorPane.setTopAnchor(addedNode[0], AnchorPane.getTopAnchor(buttonsBox) + 20); // Position below the buttons box
                    AnchorPane.setLeftAnchor(addedNode[0], windowWidth * 0.25); // 25% of the window width from the left
                    AnchorPane.setRightAnchor(addedNode[0], windowWidth * 0.25); // 25% of the window width from the right
                    anchorPane.getChildren().add(addedNode[0]); // Add the BarChart to the AnchorPane
                }
                if (selectedIndex == 1) { // Assuming '1' is the index for your new "Sales Report"
                    List<SalesData> salesDataList = fetchSalesData(); // Fetch the sales data
                    displaySalesDataDialog(salesDataList); // Display it in a dialog
                }
                if (selectedIndex == 2) {
                    List<trendData> trendDataList = fetchTrendData(); // Fetch the trend data
                    displaytrendDataDialog(trendDataList); // Display it in a dialog
                }
            }
        });

        // Create scene and set it on stage
        Scene scene = new Scene(scrollPane, 800, 900);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Inventory Management");
        return primaryStage;
    }
    
    /**
     * Creates a bar chart based on the selected parameters.
     *
     * @return The generated bar chart.
     */
    private BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Inventories");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Quantity");

        // Create the bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Product Usage Chart");

        // populate dict of inventory:quantity
        Map<String, Integer> inventoryMap = new HashMap<>();
        try (Connection connection = this.connect();
                Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("WITH filtered_orders AS (\r\n" + //
                    "    SELECT *\r\n" + //
                    "    FROM orders\r\n" + //
                    "    WHERE order_year = '" + year + "'\r\n" + //
                    "    AND order_week = '" + week + "'\r\n" + //
                    "    AND order_day = '" + day + "'\r\n" + //
                    "    AND order_time BETWEEN '" + start_time + "' AND '" + end_time + "'\r\n" + //
                    ")\r\n" + //
                    "SELECT op.order_id, op.product_id, p.productname\r\n" + //
                    "FROM filtered_orders fo\r\n" + //
                    "JOIN order_products op ON fo.order_id = op.order_id\r\n" + //
                    "JOIN products p ON op.product_id = p.product_id;");
            ArrayList<Integer> productIDs = new ArrayList<>();
            while (resultSet.next()) {
                int product_id = resultSet.getInt("product_id");
                productIDs.add(product_id);
            }
            
            // for each productID
            for (int i=0; i<productIDs.size(); ++i) {
                String product_id_string = Integer.toString(productIDs.get(i));
                ResultSet inventory_IDs_query = statement.executeQuery("SELECT inventory_id FROM product_inventory WHERE product_id = " + product_id_string +";");
                ArrayList<Integer> inventoryIDs = new ArrayList<>();

                while (inventory_IDs_query.next()) {
                    int inventory_id = inventory_IDs_query.getInt("inventory_id");
                    inventoryIDs.add(inventory_id);
                }

                //for each inventoryID
                for(int j=0; j< inventoryIDs.size(); ++j){
                    String inventory_id_string = Integer.toString(inventoryIDs.get(j));
                    ResultSet inventory_name_query = statement.executeQuery("SELECT name FROM inventory WHERE id = " + inventory_id_string + ";");
                    
                    String inventory_name = "";
                    if (inventory_name_query.next()) {
                        inventory_name = inventory_name_query.getString("name");
                    }
                    

                    //check if inventory_name is in map
                    int quantity = inventoryMap.getOrDefault(inventory_name, 0);
                    quantity++;
                    inventoryMap.put(inventory_name, quantity);
                }

            }
        }catch (Exception e) {
            e.printStackTrace();
        }



        // Define the data

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantity of Inventory Used");

        for (String key : inventoryMap.keySet()) {
            int value = inventoryMap.get(key);
            series.getData().add(new XYChart.Data<>(key, value));
        }
        

        // Add the series to the chart
        barChart.getData().add(series);
        return barChart;
    }

    private void adjustGUI(double width, double height) {

    }
    /**
     * Fetches sales data from the database based on the selected parameters.
     *
     * @return A list of SalesData objects representing the fetched sales data.
     */
    private List<SalesData> fetchSalesData() {
        List<SalesData> salesDataList = new ArrayList<>();
        String query = "SELECT " + "\r\n" + 
               "p.product_id, " + "\r\n" + 
               "p.productname, " + "\r\n" + 
               "COUNT(op.product_id) AS quantity_sold, " + "\r\n" + 
               "SUM(p.price) AS total_sales " + "\r\n" + 
               "FROM orders o " + "\r\n" + 
               "JOIN order_products op ON o.order_id = op.order_id " + "\r\n" + 
               "JOIN products p ON op.product_id = p.product_id " + "\r\n" + 
               "WHERE o.order_year = '" + year + "'\r\n" + 
                "AND o.order_week = '" + week + "'\r\n" + 
                "AND o.order_day = '" + day + "'\r\n" + 
                "AND o.order_time BETWEEN '" + start_time + "' AND '" + end_time + "'\r\n" +
               "GROUP BY p.product_id, p.productname " + "\r\n" + 
               "ORDER BY total_sales DESC;";
        try (Connection connection = this.connect();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                String productName = resultSet.getString("productname");
                int quantitySold = resultSet.getInt("quantity_sold");
                double totalSales = resultSet.getDouble("total_sales");
                salesDataList.add(new SalesData(productId, productName, quantitySold, totalSales));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return salesDataList;
    }
    /**
     * Displays sales data in a dialog window.
     *
     * @param salesDataList The list of SalesData objects to display.
     */
    private void displaySalesDataDialog(List<SalesData> salesDataList) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sales Data");

        TableView<SalesData> tableView = new TableView<>();
        tableView.setItems(FXCollections.observableArrayList(salesDataList));

        TableColumn<SalesData, Integer> productIdColumn = new TableColumn<>("Product ID");
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<SalesData, String> productNameColumn = new TableColumn<>("Product Name");
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<SalesData, Integer> quantitySoldColumn = new TableColumn<>("Quantity Sold");
        quantitySoldColumn.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));

        TableColumn<SalesData, Double> totalSalesColumn = new TableColumn<>("Total Sales");
        totalSalesColumn.setCellValueFactory(new PropertyValueFactory<>("totalSales"));
        
        // Set the preferred width for the TableView
        tableView.setPrefWidth(500);

        // Add columns to the TableView
        tableView.getColumns().addAll(productIdColumn, productNameColumn, quantitySoldColumn, totalSalesColumn);

        dialog.getDialogPane().setContent(tableView);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        dialog.showAndWait();
    }

    /**
     * Fetches inventory data from the database.
     *
     * @return A list of InventoryItem objects representing the fetched inventory data.
     */
    // Method to fetch inventory data from the database
    private List<InventoryItem> fetchInventoryData() {
        List<InventoryItem> inventoryList = new ArrayList<>();
        try (Connection connection = this.connect();
                Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT id, name FROM inventory");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                inventoryList.add(new InventoryItem(id, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inventoryList;
    }

    public static void main(String[] args) {
        launch(args);
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