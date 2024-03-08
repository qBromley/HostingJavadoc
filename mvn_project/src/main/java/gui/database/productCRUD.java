package gui.database;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
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
 * 
 */
/**
 * <p>JavaFx Application for CRUD interactions with the product table<p>
 */
public class productCRUD extends Application {


      /**
     * Main method to launch the application.
     * 
     * @param args Command line arguments 
     */
    public static void main(String[] args) {
        launch(args);
    }
    /**
     * Starts the application and sets up the primary stage with its layout,
     * form inputs for product details, and CRUD operation buttons.
     * 
     * @param primaryStage The primary window for the application.
     */
    @Override
    public void start(Stage primaryStage) {
        // Create layout components
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        // Create text fields for input
        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField priceField = new TextField();

        // Create buttons for CRUD operations
        Button addButton = new Button("Add Product");
        Button readButton = new Button("Read All Products");
        Button updateButton = new Button("Update Product");
        Button deleteButton = new Button("Delete Product");

        // Add event handlers to buttons
        addButton.setOnAction(e -> {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            BigDecimal price = new BigDecimal(priceField.getText());
            createProduct(new Product(id, name, price));
            clearFields(idField, nameField, priceField);
        });

        readButton.setOnAction(e -> {
            List<Product> productList = readAllProducts();
            displayProductsDialogTable(productList);
        });

        updateButton.setOnAction(e -> {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            BigDecimal price = new BigDecimal(priceField.getText());
            updateProduct(new Product(id, name, price));
            clearFields(idField, nameField, priceField);
        });

        deleteButton.setOnAction(e -> {
            int id = Integer.parseInt(idField.getText());
            deleteProduct(id);
            clearFields(idField, nameField, priceField);
        });

        // Add components to the layout
        grid.add(new Label("Product ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Product Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Product Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(addButton, 0, 3);
        grid.add(readButton, 1, 3);
        grid.add(updateButton, 0, 4);
        grid.add(deleteButton, 1, 4);

        // Create and set the scene
        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);

        // Show the stage
        primaryStage.setTitle("Product CRUD");
        primaryStage.show();
    }

    // Define a custom StringConverter to handle Number <-> String conversion,
        // specifically for BigDecimal values within a Number-typed TableColumn.
        /**
         * Define a custom StringConverter to handle Number <-> String conversion,
         * specifically for BigDecimal values within a Number-typed TableColumn.
         */
        StringConverter<Number> bigDecimalStringConverter = new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                if (object == null) {
                    return "";
                } else {
                    // Assuming object is a BigDecimal, convert to String
                    return object.toString();
                }
            }

            /**
             * 
             */
            @Override
            public Number fromString(String string) {
                try {
                    return new BigDecimal(string);
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO; // or handle invalid input differently
                }
            }
        };
    /**
     * Clears the text fields provided as arguments.
     * 
     * @param fields Varargs parameter of TextField objects to be cleared.
     */
    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }
    /**
     * Inserts a new product into the database.
     * 
     * @param product The product to be created in the database.
     */
    private void createProduct(Product product) {
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO products (product_id, ProductName, price) VALUES (?, ?, ?)")) {
            preparedStatement.setInt(1, product.getId());
            preparedStatement.setString(2, product.getProductName());
            preparedStatement.setBigDecimal(3, product.getPrice());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Fetches all product records from the database and returns them as a list.
     * 
     * @return A list of all products from the database.
     */
    private List<Product> readAllProducts() {
        List<Product> productList = new ArrayList<>();
        try (Connection connection = this.connect();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM products");

            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                String productName = resultSet.getString("ProductName");
                BigDecimal price = resultSet.getBigDecimal("price");
                productList.add(new Product(productId, productName, price));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return productList;
    }

    /*
    private void updateProduct(int productId, Product updatedProduct) {
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE products SET ProductName = ?, price = ? WHERE product_id = ?")) {
            preparedStatement.setString(1, updatedProduct.getProductName());
            preparedStatement.setBigDecimal(2, updatedProduct.getPrice());
            preparedStatement.setInt(3, productId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    */
    /**
     * Updates the details of an existing product in the database.
     * 
     * @param product The product to update in the database.
     */
    private void updateProduct(Product product) {
    // Implementation to update product in the database
        try (Connection connection = this.connect();
            PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE products SET ProductName = ?, price = ? WHERE product_id = ?")) {
            
            preparedStatement.setString(1, product.getProductName());
            preparedStatement.setBigDecimal(2, product.getPrice());
            preparedStatement.setInt(3, product.getId());
            preparedStatement.executeUpdate();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Deletes a product from the database based on the provided product ID.
     * 
     * @param productId The ID of the product to delete.
     */
    private void deleteProduct(int productId) {
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM products WHERE product_id = ?")) {
            preparedStatement.setInt(1, productId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Displays a dialog with a table view listing all products. Allows editing
     * of product name and price directly in the table.
     * 
     * @param productList A list of products to display in the table.
     */
    private void displayProductsDialogTable(List<Product> productList) {
    // Create the custom dialog.
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Product List");
    dialog.setHeaderText("List of Products");

    // Setup TableView for Products
    TableView<Product> table = new TableView<>();
    table.setEditable(true); // Enable table editing

    // ID Column (Not Editable)
    TableColumn<Product, Number> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

    // Product Name Column (Editable)
    TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
    nameColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // Make this column editable
    nameColumn.setOnEditCommit((CellEditEvent<Product, String> event) -> {
        Product product = event.getRowValue();
        product.setProductName(event.getNewValue()); // Update the product
        updateProduct(product); // Save the updated product to the database
    });

    // Price Column (Editable)
    TableColumn<Product, Number> priceColumn = new TableColumn<>("Price");
    priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
    priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(bigDecimalStringConverter)); // Allow editing and handle BigDecimal conversion
    priceColumn.setOnEditCommit((CellEditEvent<Product, Number> event) -> {
        Product product = event.getRowValue();
        product.setPrice(new BigDecimal(event.getNewValue().toString())); // Update the product
        updateProduct(product); // Save the updated product to the database
    });

    // Add columns to table
    table.getColumns().addAll(idColumn, nameColumn, priceColumn);

    // Populate table with products
    table.setItems(FXCollections.observableArrayList(productList));

    // Set the content of the dialog to the TableView
    dialog.getDialogPane().setContent(table);

    // Add a button to close the dialog
    ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().add(closeButton);

    // Show the dialog
    dialog.showAndWait();
}
    /**
     * Inner class representing a product with ID, name, and price properties.
     */
    public static class Product {
        private final int id;
        private String productName;
        private BigDecimal price;

        /**
         * 
         * @param id a product ID that is linked to our product table
         * @param productName a product name that is liked to our product table
         * @param price a price that is linked to each product
         */
        public Product(int id, String productName, BigDecimal price) {
            this.id = id;
            this.productName = productName;
            this.price = price;
        }
         /**
 * Retrieves the ID of the product.
 * 
 * @return the product ID
 */
        public int getId() {
            return id;
        }
/**
 * Retrieves the name of the product.
 * 
 * @return the product name as a String
 */
        public String getProductName() {
            return productName;
        }
/**
 * Retrieves the price of the product.
 * 
 * @return the price of the product as a BigDecimal
 */
        public BigDecimal getPrice() {
            return price;
        }

/**
 * Updates the product name with the specified name.
 * 
 * @param productName the new name of the product
 */
        public void setProductName(String productName) {
            this.productName = productName;
        }
/**
 * Updates the price of the product with the specified price.
 * 
 * @param price the new price of the product, as a BigDecimal
 */
        public void setPrice(BigDecimal price) {
            this.price = price;
        }

/**
 * Provides a string representation of the product, including its ID, name, and price.
 * 
 * @return a string representation of the product
 */
        @Override
        public String toString() {
            return "Product{" +
                    "id=" + id +
                    ", productName='" + productName + '\'' +
                    ", price=" + price +
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