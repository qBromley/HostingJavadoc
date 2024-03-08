package gui.database;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * @author Quinn Bromley
 * @author Kaghan Odom
 * @author Ethan Woods
 * @author Chance Hughes
 * @author Jacob Krivulla
 * @author Haden O'Keef
 * <p>JavaFX application for CRUD operations on inventory items.<p>
 */

 /**
  * JavaFX application for CRUD operations on inventory items.
  */
public class inventoryCRUD extends Application {
    /**
     * The main method to launch the JavaFX application.
     * 
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the JavaFX application.
     *
     * @param primaryStage The primary stage of the application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inventory CRUD");

        // Create layout components
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        // Create text fields for input
        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField typeField = new TextField();
        TextField quantityField = new TextField();
        TextField unitField = new TextField();
        TextField lowThresholdField = new TextField();
        TextField yearField = new TextField();
        TextField monthField = new TextField();

        // Create buttons for CRUD operations
        Button addButton = new Button("Add Inventory");
        Button readButton = new Button("Read All Inventory");
        Button updateButton = new Button("Update Inventory");
        Button deleteButton = new Button("Delete Inventory");
        Button restockButton = new Button("Check Items for Restock");
        Button ExcessItemsButton = new Button("Get Excess Items");
        

        // Add event handlers to buttons
        addButton.setOnAction(e -> {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            String type = typeField.getText();
            int quantity = Integer.parseInt(quantityField.getText());
            String unit = unitField.getText();
            int lowThreshold = Integer.parseInt(lowThresholdField.getText());
            int inv_month = Integer.parseInt(monthField.getText());
            int inv_year = Integer.parseInt(yearField.getText());

            createInventory(new Inventory(id, name, type, quantity, unit, lowThreshold, inv_month, inv_year));
            clearFields(idField, nameField, typeField, quantityField, unitField, lowThresholdField, monthField, yearField);
        });

        readButton.setOnAction(e -> {
        List<Inventory> inventoryList = readAllInventory();
        displayInventoryDialog(inventoryList);

        });

        restockButton.setOnAction(e -> {
        List<Inventory> inventoryNeedingRestock = readInventoryNeedingRestock();
        displayInventoryDialog(inventoryNeedingRestock);

        });

        updateButton.setOnAction(e -> {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            String type = typeField.getText();
            int quantity = Integer.parseInt(quantityField.getText());
            String unit = unitField.getText();
            int lowThreshold = Integer.parseInt(lowThresholdField.getText());
            int inv_month = Integer.parseInt(monthField.getText());
            int inv_year = Integer.parseInt(yearField.getText());

            updateInventory(new Inventory(id, name, type, quantity, unit, lowThreshold, inv_month, inv_year));
            clearFields(idField, nameField, typeField, quantityField, unitField, lowThresholdField, monthField, yearField);
        });

        deleteButton.setOnAction(e -> {
            int id = Integer.parseInt(idField.getText());
            deleteInventory(id);
            clearFields(idField, nameField, typeField, quantityField, unitField, lowThresholdField, monthField, yearField);
        });

        
        ExcessItemsButton.setOnAction(e -> {
            int selectedYear = Integer.parseInt(yearField.getText());
            int selectedMonth = Integer.parseInt(monthField.getText());

            // Call the method to get consumed items
            List<Inventory> ExcessItemsList = getExcessItems(selectedYear, selectedMonth);

            // Display the result in the interactive table
            displayInventoryDialog(ExcessItemsList);
        });

        // Add components to the layout
        grid.add(new Label("Inventory ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(quantityField, 1, 3);
        grid.add(new Label("Unit:"), 0, 4);
        grid.add(unitField, 1, 4);
        grid.add(new Label("Low Threshold:"), 0, 5);
        grid.add(new Label("Month:"), 2, 0);
        grid.add(monthField, 3, 0);
        grid.add(new Label("Year:"), 2, 1);
        grid.add(yearField, 3, 1);
        grid.add(lowThresholdField, 1, 5);
        grid.add(addButton, 0, 6);
        grid.add(readButton, 1, 6);
        grid.add(updateButton, 0, 7);
        grid.add(deleteButton, 1, 7);
        grid.add(restockButton, 2, 6);
        grid.add(ExcessItemsButton, 2, 7);

        // Create and set the scene
        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);

        // Show the stage
        primaryStage.show();
    }
    /**
     * The main method to launch the JavaFX application.
     * 
     * @param fields takes in a TextField, and the function clears it and {@return void}.
     * 
     */
    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    /**
     * Inserts a new inventory item into the database.
     *
     * @param inventory The inventory item to insert.
     */
    private void createInventory(Inventory inventory) {
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO inventory (id, name, type, quantity, unit, low_threshold, month, year)" +
            "  VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            preparedStatement.setInt(1, inventory.getId());
            preparedStatement.setString(2, inventory.getName());
            preparedStatement.setString(3, inventory.getType());
            preparedStatement.setInt(4, inventory.getQuantity());
            preparedStatement.setString(5, inventory.getUnit());
            preparedStatement.setInt(6, inventory.getLowThreshold());
            preparedStatement.setInt(7, inventory.getMonth());
            preparedStatement.setInt(8, inventory.getYear());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Retrieves all inventory items from the database.
     *
     * @return A list of Inventory objects representing the retrieved inventory items.
     */
    private List<Inventory> readAllInventory() {
        List<Inventory> inventoryList = new ArrayList<>();
        try (Connection connection = this.connect();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM inventory");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String type = resultSet.getString("type");
                int quantity = resultSet.getInt("quantity");
                String unit = resultSet.getString("unit");
                int lowThreshold = resultSet.getInt("low_threshold");
                int inv_month = resultSet.getInt("month");
                int inv_year = resultSet.getInt("year");
                inventoryList.add(new Inventory(id, name, type, quantity, unit, lowThreshold, inv_month, inv_year));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return inventoryList;
    }

    /**
     * Updates an existing inventory item in the database.
     *
     * @param updatedInventory The updated inventory item.
     */
    private void updateInventory(Inventory updatedInventory) {
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE inventory SET name = ?, type = ?, quantity = ?, unit = ?, low_threshold = ?, month = ?, year = ? WHERE id = ?")) {
            preparedStatement.setString(1, updatedInventory.getName());
            preparedStatement.setString(2, updatedInventory.getType());
            preparedStatement.setInt(3, updatedInventory.getQuantity());
            preparedStatement.setString(4, updatedInventory.getUnit());
            preparedStatement.setInt(5, updatedInventory.getLowThreshold());
            preparedStatement.setInt(6, updatedInventory.getMonth());
            preparedStatement.setInt(7, updatedInventory.getYear());
            preparedStatement.setInt(8, updatedInventory.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Deletes an existing inventory item from the database.
     *
     * @param id The ID of the inventory item to delete.
     */
    private void deleteInventory(int id) {
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM inventory WHERE id = ?")) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Retrieves inventory items needing restock from the database.
     *
     * @return A list of Inventory objects representing the inventory items needing restock.
     */
    private List<Inventory> readInventoryNeedingRestock() {
    List<Inventory> inventoryList = new ArrayList<>();
    try (Connection connection = this.connect();
         Statement statement = connection.createStatement()) {
        ResultSet resultSet = statement.executeQuery("SELECT * FROM inventory WHERE quantity < low_threshold");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            String type = resultSet.getString("type");
            int quantity = resultSet.getInt("quantity");
            String unit = resultSet.getString("unit");
            int lowThreshold = resultSet.getInt("low_threshold");
            int inv_month = resultSet.getInt("month");
            int inv_year = resultSet.getInt("year");
            inventoryList.add(new Inventory(id, name, type, quantity, unit, lowThreshold, inv_month, inv_year));
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return inventoryList;
}

/**
 * Retrieves excess inventory items from the database based on the specified year and month.
 *
 * @param year  The year for which to retrieve excess inventory items.
 * @param month The month for which to retrieve excess inventory items.
 * @return A list of Inventory objects representing the excess inventory items.
 */
private List<Inventory> getExcessItems(int year, int month) {
    List<Inventory> ExcessItemsList = new ArrayList<>();
    try (Connection connection = this.connect();
         Statement statement = connection.createStatement()) {
        // Get the current year and month
        int currentYear = Year.now().getValue();
        int currentMonth = MonthDay.now().getMonthValue();

        // Execute a query to get items with excess
        ResultSet resultSet = statement.executeQuery("SELECT * FROM inventory " +
                "WHERE (quantity > low_threshold * 10) AND " +
                "( ( ( month  <= " + currentMonth + ") AND (month >= " + month + ") AND ( year = " + currentYear + ") ) " +
                "OR (" + year + " < " + currentYear + ") )");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            String type = resultSet.getString("type");
            int quantity = resultSet.getInt("quantity");
            String unit = resultSet.getString("unit");
            int lowThreshold = resultSet.getInt("low_threshold");
            int inv_month = resultSet.getInt("month");
            int inv_year = resultSet.getInt("year");
            ExcessItemsList.add(new Inventory(id, name, type, quantity, unit, lowThreshold, inv_month, inv_year));
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return ExcessItemsList;
}

/**
 * Displays a dialog window with a table of inventory items.
 *
 * @param inventoryList The list of Inventory objects to display.
 */
private void displayInventoryDialog(List<Inventory> inventoryList) {
    // Create the custom dialog.
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Inventory List");

    // Setup TableView
    TableView<Inventory> table = new TableView<>();
    table.setEditable(true); // Enable table editing

    // Define columns
    TableColumn<Inventory, Number> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

    TableColumn<Inventory, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // Make this column editable
    nameColumn.setOnEditCommit((CellEditEvent<Inventory, String> event) -> {
        Inventory inventory = event.getRowValue();
        inventory.setName(event.getNewValue()); // Update the inventory
        updateInventory(inventory); // Save the updated inventory to the database
    });

    TableColumn<Inventory, String> typeColumn = new TableColumn<>("Type");
    typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
    typeColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // Make this column editable
    typeColumn.setOnEditCommit((CellEditEvent<Inventory, String> event) -> {
        Inventory inventory = event.getRowValue();
        inventory.setType(event.getNewValue()); // Update the inventory
        updateInventory(inventory); // Save the updated inventory to the database
    });

    TableColumn<Inventory, Number> quantityColumn = new TableColumn<>("Quantity");
    quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter())); // Allow editing and handle Number conversion
    quantityColumn.setOnEditCommit((CellEditEvent<Inventory, Number> event) -> {
        Inventory inventory = event.getRowValue();
        inventory.setQuantity(event.getNewValue().intValue()); // Update the inventory
        updateInventory(inventory); // Save the updated inventory to the database
    });

    TableColumn<Inventory, String> unitColumn = new TableColumn<>("Unit");
    unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
    unitColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // Make this column editable
    unitColumn.setOnEditCommit((CellEditEvent<Inventory, String> event) -> {
        Inventory inventory = event.getRowValue();
        inventory.setUnit(event.getNewValue()); // Update the inventory
        updateInventory(inventory); // Save the updated inventory to the database
    });

    TableColumn<Inventory, Number> lowThresholdColumn = new TableColumn<>("Low Threshold");
    lowThresholdColumn.setCellValueFactory(new PropertyValueFactory<>("lowThreshold"));
    lowThresholdColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter())); // Allow editing and handle Number conversion
    lowThresholdColumn.setOnEditCommit((CellEditEvent<Inventory, Number> event) -> {
        Inventory inventory = event.getRowValue();
        inventory.setLowThreshold(event.getNewValue().intValue()); // Update the inventory
        updateInventory(inventory); // Save the updated inventory to the database
    });

    TableColumn<Inventory, Number> monthColumn = new TableColumn<>("Month");
    monthColumn.setCellValueFactory(new PropertyValueFactory<>("month"));
    monthColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter())); // Allow editing and handle Number conversion
    monthColumn.setOnEditCommit((CellEditEvent<Inventory, Number> event) -> {
    Inventory inventory = event.getRowValue();
    inventory.setMonth(event.getNewValue().intValue()); // Update the inventory
    updateInventory(inventory); // Save the updated inventory to the database
});

    TableColumn<Inventory, Number> yearColumn = new TableColumn<>("Year");
    yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
    yearColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter())); // Allow editing and handle Number conversion
    yearColumn.setOnEditCommit((CellEditEvent<Inventory, Number> event) -> {
    Inventory inventory = event.getRowValue();
    inventory.setYear(event.getNewValue().intValue()); // Update the inventory
    updateInventory(inventory); // Save the updated inventory to the database
});

    // Add columns to table
    table.getColumns().addAll(idColumn, nameColumn, typeColumn, quantityColumn, unitColumn, lowThresholdColumn, monthColumn, yearColumn);

    // Populate table with inventory
    table.setItems(FXCollections.observableArrayList(inventoryList));

    // Set the content of the dialog to the TableView
    dialog.getDialogPane().setContent(table);

    // Add a button to close the dialog
    ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().add(closeButton);

    // Show the dialog
    dialog.showAndWait();
}
    /**
     * Represents an inventory item with various attributes.
     */
    public static class Inventory {
        private final int id;
        private String name;
        private String type;
        private int quantity;
        private String unit;
        private int lowThreshold;
        private int month;
        private int year;


        /**
         * Constructs an Inventory object with the specified attributes.
         *
         * @param id            The ID of the inventory item.
         * @param name          The name of the inventory item.
         * @param type          The type of the inventory item.
         * @param quantity      The quantity of the inventory item.
         * @param unit          The unit of measurement for the inventory item.
         * @param lowThreshold  The low threshold quantity for restocking.
         * @param month         The month of the inventory item.
         * @param year          The year of the inventory item.
         */
        public Inventory(int id, String name, String type, int quantity, String unit, int lowThreshold, int month, int year) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.quantity = quantity;
            this.unit = unit;
            this.lowThreshold = lowThreshold;
            this.month = month;
            this.year = year;
        }
        /**
         * Gets the ID value from Inventory.
         * @return Returns an integer value that represents the ID of the inventory.
         */
        public int getId() {
            return id;
        }

        /**
         * Gets the Name value from Inventory.
         * @return  Returns an String value that represents the ID of the inventory.
         */
        public String getName() {
            return name;
        }
 /**
         * Gets the Type value from Inventory.
         * @return  Returns an String value that represents the type. 
         */
        public String getType() {
            return type;
        }
         /**
         * Gets the Quantity value from Inventory.
         * @return  Returns an integer value that represents the quantity of and item in the inventory.
         */
        public int getQuantity() {
            return quantity;
        }
         /**
         * Gets the Unit value from Inventory.
         * @return  Returns an String value that represents unit of an item in the inventory.
         */
        public String getUnit() {
            return unit;
        }
         /**
         * Gets the Low Threshold value from Inventory.
         * @return  Returns an integer value that represents the low threshold value for the inventory.
         */
        public int getLowThreshold() {
            return lowThreshold;
        }
         /**
         * Returns the month from Inventory.
         * @return  Returns an integer value that represents the month.
         */
        public int getMonth() {
            return month;
        }
         /**
         * Returns the year from Inventory. 
         * @return  Returns an integer value that represents the month.
         */
        public int getYear() {
            return year;
        }
          /**
         *Sets the Name value for an item in inventory.
         *
         */
        public void setName(String name) {
            this.name = name;
        }
        /**
         *Sets the Type value for an item in inventory.
         *
         */
        public void setType(String type) {
            this.type = type;
        }
         /**
         *Sets the Quantity value for an item in inventory.
         *
         */
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
         /**
         *Sets the Unit value for an item in inventory.
         *
         */
        public void setUnit(String unit) {
            this.unit = unit;
        }
         /**
         *Sets the LowThreshold value for an item in inventory.
         *@param lowThreshold an integer value that becomes the new low threshold.
         *
         */
        public void setLowThreshold(int lowThreshold) {
            this.lowThreshold = lowThreshold;
        }
          /**
         *Sets the month value for an item in inventory.
         *@param month an integer value that becomes the new month.
         *
         */
        public void setMonth(int month) {
            this.month = month;
        }
         /**
         *Sets the year value for an item in inventory.
         *@param year an integer value that becomes the new year.
         *
         */
        public void setYear(int year) {
            this.year = year;
        }
        /**
         * puts the inventory into a string value.
         * @return A string value that is evetrything in the inventory 
         */
        @Override
        public String toString() {
            return "Inventory{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", quantity=" + quantity +
                    ", unit='" + unit + '\'' +
                    ", lowThreshold=" + lowThreshold +
                    ", month=" + month +
                    ", year=" + year +
                    '}';
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