import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.File;
import java.util.List;

abstract class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String PhoneNumber;

    public User(String username, String password, String role, String phonenumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.PhoneNumber = phonenumber;
    }

    public User(int id, String username, String password, String role, String phonenumber) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.PhoneNumber = phonenumber;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public abstract void displayRoleFunctions();
}

class Customer extends User {
    public Customer(String username, String password, String PhoneNumber) {
        super(username, password, "customer", PhoneNumber);
    }

    public Customer(int id, String username, String password, String PhoneNumber) {
        super(id, username, password, "customer", PhoneNumber);
    }

    @Override
    public void displayRoleFunctions() {
        JOptionPane.showMessageDialog(null, "Welcome, Customer! You can now rent a vehicle.");
    }

    public void rentVehicle(Vehicle vehicle, long rentalDays,LocalDate endDate, String name) throws SQLException {
        double totalPrice = rentalDays * vehicle.getRentalPrice();
        DatabaseUtility.rentVehicle(vehicle.getId(), name, rentalDays,endDate, totalPrice);
        JOptionPane.showMessageDialog(null, "Rental confirmed for " + vehicle.getModel() +
                " for " + rentalDays + " days. Total Price: $" + totalPrice);
    }
}

class Admin extends User {
    public Admin(String username, String password, String PhoneNumber) {
        super(username, password, "admin", PhoneNumber);
    }

    public Admin(int id, String username, String password, String PhoneNumber) {
        super(id, username, password, "admin", PhoneNumber);
    }

    @Override
    public void displayRoleFunctions() {
        JOptionPane.showMessageDialog(null, "Welcome, Admin! You can now manage users and vehicles.");
    }

    public void manageUsers(List<User> users) {
        StringBuilder userList = new StringBuilder("Users List:\n");
        for (User  user : users) {
            userList.append(user.getUsername()).append(" - ").append(user.getRole()).append("\n");
        }
        JOptionPane.showMessageDialog(null, userList.toString(), "Manage Users", JOptionPane.INFORMATION_MESSAGE);
    }

    public void manageVehicles(List<Vehicle> vehicles) {
        StringBuilder vehicleList = new StringBuilder("Vehicles List:\n");
        for (Vehicle vehicle : vehicles) {
            vehicleList.append(vehicle.getModel()).append(" - ").append(vehicle.getType()).append("\n");
        }
        JOptionPane.showMessageDialog(null, vehicleList.toString(), "Manage Vehicles", JOptionPane.INFORMATION_MESSAGE);
    }
}

// Model for Vehicle
class Vehicle {
    private int id;
    private String name;
    private String model;
    private String licensePlate;
    private String type;
    private double rentalPrice;

    // Constructor
    public Vehicle(int id, String name, String model, String licensePlate, String type, double rentalPrice) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.licensePlate = licensePlate;
        this.type = type;
        this.rentalPrice = rentalPrice;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getType() {
        return type;
    }

    public double getRentalPrice() {
        return rentalPrice;
    }

    public String toString() {
        return "vid:"+id +" vName:"+ name+" vModel:" + model+" vLPlate:" + licensePlate+" vType" + type+" price:" + rentalPrice;
    }
}

class DatabaseUtility {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/vehicleRentalApp";
    private static final String DB_USER = "root";  
    private static final String DB_PASSWORD = "manada5707#"; 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static List<User> getUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String role = resultSet.getString("role");
                String phone = resultSet.getString("phone_number");

                if (role.equals("admin")) {
                    users.add(new Admin(id, username, password, phone));
                } else {
                    users.add(new Customer(id, username, password, phone));
                }
            }
        }
        return users;
    }

    public static List<Vehicle> getVehicles() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String query = "SELECT * FROM vehicles";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String licensePlate = resultSet.getString("license_plate");
                String model = resultSet.getString("model");
                String type = resultSet.getString("type");
                double rentalRate = resultSet.getDouble("rental_price");

                vehicles.add(new Vehicle(id, name, model, licensePlate, type, rentalRate));
            }
        }
        return vehicles;
    }

    public static void updateUserProfile(int userId, String newUsername, String newPassword, String phoneNumber) throws SQLException {
        String query = "UPDATE users SET username = ?, password = ?, phone_number = ? WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newUsername);
            stmt.setString(2, newPassword);
            stmt.setString(3, phoneNumber);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        }
    }

    public static List<RentalHistory> getRentalHistory(String username) throws SQLException {
        List<RentalHistory> history = new ArrayList<>();
        String query = "SELECT * FROM rental_history WHERE customer_name= ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RentalHistory rental = new RentalHistory(rs.getInt("rental_id"), rs.getInt("vehicle_id"), rs.getString("customer_name"), rs.getInt("rental_days"), rs.getDouble("rental_price"), rs.getBoolean("returned_status"),rs.getDate("rentalenddate"),rs.getDouble("fine"));
                history.add(rental);
            }
        }
        return history;
    }

    public static boolean deleteUser (int userId) throws SQLException {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static void addVehicle(Vehicle vehicle) throws SQLException {
        String query = "INSERT INTO vehicles (name, model, license_plate, type, rental_price) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, vehicle.getName());
            stmt.setString(2, vehicle.getModel());
            stmt.setString(3, vehicle.getLicensePlate());
            stmt.setString(4, vehicle.getType());
            stmt.setDouble(5, vehicle.getRentalPrice());
            stmt.executeUpdate();
        }
    }

    public static void updateVehicleRentalPrice(int vehicleId, double newPrice) throws SQLException {
        String query = "UPDATE vehicles SET rental_price = ? WHERE id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, newPrice);
            stmt.setInt(2, vehicleId);
            stmt.executeUpdate();
        }
    }
    public static void removeVehicle(int vehicleId) throws SQLException {
        String query = "DELETE FROM vehicles WHERE id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, vehicleId);
            stmt.executeUpdate();
        }
    }

    public static void rentVehicle(int vehicleId, String customerName, long rentalDays,LocalDate rentenddays, double rentalPrice) throws SQLException {
        String query = "INSERT INTO rental_history (vehicle_id, customer_name, rental_days, rental_price, returned_status,rentalenddate) VALUES (?, ?, ?, ?, ?,?)";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, vehicleId);
            stmt.setString(2, customerName);
            stmt.setLong(3, rentalDays);
            stmt.setDouble(4, rentalPrice);
            stmt.setBoolean(5, false);
            stmt.setDate(6, Date.valueOf(rentenddays));
            stmt.executeUpdate();
        }
    }

    public static List<Vehicle> getVehiclesByType(String vehicleType) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String query = "SELECT * FROM vehicles WHERE type = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, vehicleType);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Vehicle vehicle = new Vehicle(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("model"),
                        resultSet.getString("license_plate"),
                        resultSet.getString("type"),
                        resultSet.getDouble("rental_price")
                );
                vehicles.add(vehicle);
            }
        }
        return vehicles;
    }
    
    public static void updateRentalStatus(int rentalId, boolean isReturned) {
        String query = "UPDATE rental_history SET returned_status = ? WHERE rental_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, isReturned); 
            stmt.setInt(2, rentalId);       

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Rental status updated successfully.");
            } else {
                System.out.println("No rental found with ID: " + rentalId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int deleteRentalRecord(int rentalId) {
        String query = "DELETE FROM rental_history WHERE rental_id = ? AND returned_status = 1"; 

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, rentalId);  

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Rental record deleted successfully.");
                
            } else {
                System.out.println("No rental record found or rental not returned yet.");
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static List<RentalHistory> getAllRentals() {
        String query = "SELECT * FROM rental_history";
        List<RentalHistory> rentals = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int rentalId = rs.getInt("rental_id");
                int vehicleId = rs.getInt("vehicle_id");
                String customerName = rs.getString("customer_name");
                int rentalDays = rs.getInt("rental_days");
                double rentalPrice = rs.getDouble("rental_price");
                boolean returnedStatus = rs.getBoolean("returned_status");
                Date rentenddate=rs.getDate("rentalenddate");
                double fine=rs.getDouble("fine");

                rentals.add(new RentalHistory(rentalId, vehicleId, customerName, rentalDays, rentalPrice, returnedStatus,rentenddate,fine));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rentals;
    }
    public static void checkRentalStatusAndApplyFines() throws SQLException {
        // Establish the database connection
        try (Connection connection = getConnection()) {
            String query = "SELECT rental_id, rentalenddate, fine, rental_price,vehicle_id FROM rental_history WHERE rentalenddate IS NULL";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    int rentalId = resultSet.getInt("rental_id");
                    Date rentalEndDate = resultSet.getDate("rentalenddate");
                    double rentalPrice=resultSet.getDouble("rental_price");

                    double fine = calculateFine(rentalEndDate.toLocalDate(),rentalPrice);
                    if (fine > 0) {
                        System.out.println("Rental ID: " + rentalId + " - Fine: $" + fine);
                        updateRentalWithFine(rentalId, fine, connection);
                    } else {
                        System.out.println("Rental ID: " + rentalId + " - No fine (Not overdue).");
                    }
                }
            }
        }
    }

    /**
     * Fine calculation logic: Calculate fine based on the number of overdue days.
     */
    public static double calculateFine(LocalDate rentalEndDate,double rentalPrice) {
        LocalDate today = LocalDate.now();  // Get today's date
        
        // If the rental is not overdue, no fine
        if (today.isBefore(rentalEndDate) || today.equals(rentalEndDate)) {
            return 0.00;
        }

        // Calculate the number of overdue days
        long overdueDays = ChronoUnit.DAYS.between(rentalEndDate, today);
        
        // Apply fine for overdue days
        if (overdueDays > 0) {
            return overdueDays *rentalPrice;
        }
        
        return 0.00;  // No fine if returned within grace period
    }

    /**
     * Update the rental record with the fine in the rentals table.
     */
    public static void updateRentalWithFine(int rentalId, double fine, Connection connection) throws SQLException {
        String updateQuery = "UPDATE rental_history SET fine = ? WHERE rental_id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setDouble(1, fine);
            preparedStatement.setInt(2, rentalId);
            
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Successfully updated fine for rental ID: " + rentalId);
            } else {
                System.out.println("Failed to update fine for rental ID: " + rentalId);
            }
        }
    }
}


class RentalHistory {
    private int rentalId;
    private int vehicleId;
    private String Customername;
    private int rentalDays;
    private double rentalPrice,fine;
    private Boolean returnedStatus;
    Date rentenddate;

    public RentalHistory(int rentalId, int vehicleId, String customerName, int rentalDays, double rentalPrice, Boolean returnedStatus,Date rentenddate,double fine) {
        this.vehicleId = vehicleId;
        this.rentalId = rentalId;
        this.Customername = customerName;
        this.rentalDays = rentalDays;
        this.rentalPrice = rentalPrice;
        this.returnedStatus = returnedStatus;
        this.rentenddate=rentenddate;
        this.fine=fine;
    }

    public int getRentalid() {
        return rentalId;              
    }

    public int getVehicleId() {
        return vehicleId;
 }

    public int getRentalDays() {
        return rentalDays;
    }

    public double getRentalPrice() {
        return rentalPrice;
    }

    public String getCustomername() {
        return Customername;
    }

    public Boolean getReturnedStatus() {
        return returnedStatus;
    }
    public Date getenddate(){
        return rentenddate;
    }
    public String toString() {
        return "Rentalid:" + rentalId + " VehicleId:" + vehicleId + " CustName:" + Customername + " RentalDays:" + rentalDays + " RentalPrice:" + rentalPrice + " Returned:" + returnedStatus+" Rent_end Date:"+rentenddate;
    }
    
    public double getFine() {
            return fine;
    }
}
    
    public class VehicleRental {
        private JFrame frame;
        private JTextField usernameField, regUsernameField, regPhoneNumField, vehicleNameField, vehicleModelField, licensePlateField, rentalPriceField,startDateField,endDateField;
        private JPasswordField passwordField, regPasswordField;
        JLabel totalRentLabel;
        private JComboBox<String> loginRoleComboBox, regRoleComboBox, vehicleTypeComboBox;
        private User loggedInUser ;
        private List<User> users;
        private List<Vehicle> vehicles;
    
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                try {
                    new VehicleRental().startApp();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
    
        public void startApp() throws SQLException {
            users = DatabaseUtility.getUsers();
            vehicles = DatabaseUtility.getVehicles();
            initializeUI();
        }
    
        private void initializeUI() throws SQLException {
            frame = new JFrame("Vehicle Rental System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            checkAndApplyFines();
            showLoginPanel();
            frame.setVisible(true);
            frame.setResizable(false);
        }
        public static void checkAndApplyFines() throws SQLException {
            DatabaseUtility.checkRentalStatusAndApplyFines();
        }
    
        private void showLoginPanel() {
            JPanel loginPanel = new JPanel();  
            ImageIcon imageIcon = new ImageIcon("C:\\Users\\hp\\OneDrive\\Pictures\\images.jpeg");
            Image image = imageIcon.getImage();  
            Image scaledImage = image.getScaledInstance(frame.getWidth(), frame.getHeight(), Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(scaledImage);  
            
            JLabel backgroundLabel = new JLabel(imageIcon);
            backgroundLabel.setBounds(0, 0,600,400); 
            loginPanel.add(backgroundLabel); 
            loginPanel.setLayout(null);
            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setBounds(50, 50, 100, 25);
            loginPanel.add(usernameLabel);
            usernameField = new JTextField(15);
            usernameField.setBounds(150, 50, 150, 25);
            loginPanel.add(usernameField);
            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setBounds(50, 100, 100, 25);
            loginPanel.add(passwordLabel);
            passwordField = new JPasswordField(15);
            passwordField.setBounds(150, 100, 150, 25);
            loginPanel.add(passwordField);
            JLabel roleLabel = new JLabel("Role:");
            roleLabel.setBounds(50, 150, 100, 25);
            loginPanel.add(roleLabel);
            loginRoleComboBox = new JComboBox<>(new String[]{"Admin", "Customer"});
            loginRoleComboBox.setBounds(150, 150, 150, 25);
            loginPanel.add(loginRoleComboBox);
            JButton loginButton = new JButton("Login");
            loginButton.setBounds(50, 200, 100, 25);
            loginButton.addActionListener(e -> performLogin());
            loginPanel.add(loginButton);
            JButton registerButton = new JButton("Register");
            registerButton.setBounds(200, 200, 100, 25);
            registerButton.addActionListener(e -> showRegistrationPanel());
            loginPanel.add(registerButton);
            loginPanel.setComponentZOrder(backgroundLabel, loginPanel.getComponentCount() - 1);
            frame.setContentPane(loginPanel);
            frame.revalidate();
            frame.repaint();
        }
    
        private void showRegistrationPanel() {
            JPanel registrationPanel = new JPanel();
            ImageIcon imageIcon = new ImageIcon("C:\\Users\\hp\\OneDrive\\Pictures\\images.jpeg");
            Image image = imageIcon.getImage();  
            Image scaledImage = image.getScaledInstance(frame.getWidth(), frame.getHeight(), Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(scaledImage);   
            JLabel backgroundLabel = new JLabel(imageIcon);
            backgroundLabel.setBounds(0, 0,600,400); 
            registrationPanel.add(backgroundLabel); 
            registrationPanel.setLayout(null);
            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setBounds(50, 50, 100, 25);
            registrationPanel.add(usernameLabel);
            regUsernameField = new JTextField(15);
            regUsernameField.setBounds(150, 50, 150, 25);
            registrationPanel.add(regUsernameField);
            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setBounds(50, 100, 100, 25);
            registrationPanel.add(passwordLabel);
            passwordField = new JPasswordField(15);
            passwordField.setBounds(150, 100, 150, 25);
            registrationPanel.add(passwordField);  
            JButton uploadButton = new JButton("Upload File");
            JLabel statusLabel = new JLabel("No file uploaded yet.");
            
            // Add action listener to the button
            uploadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    uploadFile();
                }
                private void uploadFile() {
                    JFileChooser fileChooser = new JFileChooser();
                    int returnValue = fileChooser.showOpenDialog(frame);
                    
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        // Get the selected file
                        File selectedFile = fileChooser.getSelectedFile();
                        // Perform verification (for example, check file extension)
                        if (verifyFile(selectedFile)) {
                            statusLabel.setText("File uploaded: " + selectedFile.getName());
                        } else {
                            statusLabel.setText("Invalid file type! Please upload a .txt or .pdf file.");
                        }
                    } else {
                        statusLabel.setText("File selection was canceled.");
                    }
                }
                private boolean verifyFile(File file) {
                    // Check file extension
                    String fileName = file.getName();
                    return fileName.endsWith(".txt") || fileName.endsWith(".pdf");
                }
            });
            JLabel S =new JLabel("Submit any proof:");
            S.setBounds(50,125,100,25);
            registrationPanel.add(S);
            uploadButton.setBounds(150, 125, 100, 25);
            registrationPanel.add(uploadButton);
            statusLabel.setBounds(250, 125, 100, 25);
            registrationPanel.add(statusLabel);
            JLabel phoneLabel = new JLabel("Phone Number:");
            phoneLabel.setBounds(50, 150, 100, 25);
            registrationPanel.add(phoneLabel);
            regPhoneNumField = new JTextField(15);
            regPhoneNumField.setBounds(150, 150, 150, 25);
            registrationPanel.add(regPhoneNumField);
    
            JLabel roleLabel = new JLabel("Role:");
            roleLabel.setBounds(50, 200, 100, 25);
            registrationPanel.add (roleLabel);
    
            regRoleComboBox = new JComboBox<>(new String[]{"Admin", "Customer"});
            regRoleComboBox.setBounds(150, 200, 150, 25);
            registrationPanel.add(regRoleComboBox);
            JFileChooser fileChooser = new JFileChooser();
            File selectedFile = fileChooser.getSelectedFile();
            JButton registerButton = new JButton("Register");
            registerButton.setBounds(50, 250, 100, 25);
            registerButton.addActionListener(e -> registerUser (selectedFile));
            registrationPanel.add(registerButton);
    
            JButton backButton = new JButton("Back to Login");
            backButton.setBounds(200, 250, 150, 25);
            backButton.addActionListener(e -> showLoginPanel());
            registrationPanel.add(backButton);
            registrationPanel.setComponentZOrder(backgroundLabel, registrationPanel.getComponentCount() - 1);
            frame.setContentPane(registrationPanel);
            frame.revalidate();
            frame.repaint();
        }
    
        private void registerUser (File file) {
            String username = regUsernameField.getText();
            String password = new String(passwordField.getPassword());
            String phoneNumber = regPhoneNumField.getText();
            String role = (String) regRoleComboBox.getSelectedItem();
            if (username.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {   
                JOptionPane.showMessageDialog(frame, "All fields are required.");
                return;
            }
            if (!isValidPassword(password)) {
                JOptionPane.showMessageDialog(frame, "Invalid Password");
                return;
            }
            if(phoneNumber.length()<10){
                JOptionPane.showMessageDialog(frame, "Invalid PhoneNumber");
                return;
            }
    
            try (Connection connection = DatabaseUtility.getConnection()) {
                String checkUserQuery = "SELECT * FROM users WHERE username = ?";
                try (PreparedStatement stmt = connection.prepareStatement(checkUserQuery)) {
                    stmt.setString(1, username);
                    ResultSet resultSet = stmt.executeQuery();
    
                    if (resultSet.next()) {
                        JOptionPane.showMessageDialog(frame, "Username already exists!");
                    } else {
                        String insertUserQuery = "INSERT INTO users (username, password, role, phone_number) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertUserQuery)) {
                            insertStmt.setString(1, username);
                            insertStmt.setString(2, password);
                            insertStmt.setString(3, role.toLowerCase());
                            insertStmt.setString(4, phoneNumber);
                            insertStmt.executeUpdate();
    
                            JOptionPane.showMessageDialog(frame, "Registration successful! Please log in.");
                            startApp();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "An error occurred while registering the user.");
            }
        }
        public static boolean isValidPassword(String password) {
            // Regular expression for password validation
            String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
            
            // Check if password matches the regex
            return password.matches(regex);
        }
        private void performLogin() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) loginRoleComboBox.getSelectedItem();
            for (User  user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password) &&
                    user.getRole().equals(role.toLowerCase())) {
                    loggedInUser  = user;
                    loggedInUser .displayRoleFunctions();
                    if (role.equals("Customer")) {
                        showRentalPanel();
                    } else if (role.equals("Admin")) {
                        showAdminPanel();
                    }
                    return;
                }
            }
    
            JOptionPane.showMessageDialog(frame, "Invalid login details!");
        }
    
        private void showRentalPanel() {
            JPanel rentalPanel = new JPanel();
            rentalPanel.setLayout(null); 
            ImageIcon imageIcon = new ImageIcon("C:\\Users\\hp\\OneDrive\\Pictures\\images.jpeg");
            Image image = imageIcon.getImage();  
            Image scaledImage = image.getScaledInstance(frame.getWidth(), frame.getHeight(), Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(scaledImage);   
            JLabel backgroundLabel = new JLabel(imageIcon);
            backgroundLabel.setBounds(0, 0,600,400); 
            rentalPanel.add(backgroundLabel); 
            JMenuBar menuBar = new JMenuBar();
                        JMenu settingsMenu = new JMenu("Settings");
                        JMenuItem updateProfileItem = new JMenuItem("Update Profile");
                        updateProfileItem.addActionListener(e -> showUpdateProfilePanel());
                        JMenuItem viewHistoryItem = new JMenuItem("View Rental History");
                        viewHistoryItem.addActionListener(e -> showRentalHistoryPanel());
                        JMenuItem logout=new JMenuItem("Logout");
                        logout.addActionListener(e->{
                            try {
                                startApp();
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                        });
                        settingsMenu.add(updateProfileItem);
                        settingsMenu.add(viewHistoryItem);
                        settingsMenu.add(logout);
                        menuBar.add(settingsMenu);
                        frame.setJMenuBar(menuBar);
            JLabel vehicleLabel = new JLabel("Select Vehicle:");
            vehicleLabel.setBounds(50, 10, 100, 25);
            rentalPanel.add(vehicleLabel);
            JComboBox<String> vehicleTypeComboBox = new JComboBox<>(new String[]{"Car", "Bike", "Truck", "Van"});
            vehicleTypeComboBox.setBounds(150, 10, 150, 25);
            rentalPanel .add(vehicleTypeComboBox);
    
            JComboBox<Vehicle> vehicleListComboBox = new JComboBox<>();
            vehicleTypeComboBox.addActionListener(e -> {
                String selectedType = (String) vehicleTypeComboBox.getSelectedItem();
                updateVehicleListComboBox(selectedType, vehicleListComboBox);
            });
            vehicleListComboBox.setBounds(300,10, 450, 25);
            rentalPanel.add(vehicleListComboBox);
            JLabel startDateLabel = new JLabel("Start Date (yyyy-mm-dd):");
            startDateLabel.setBounds(50, 50, 200, 25);
            rentalPanel.add(startDateLabel);
    
            startDateField = new JTextField();
            startDateField.setBounds(200, 50, 100, 25);
            rentalPanel.add(startDateField);
    
            JLabel endDateLabel = new JLabel("End Date (yyyy-mm-dd):");
            endDateLabel.setBounds(50, 100, 200, 25);
            rentalPanel.add(endDateLabel);
            endDateField = new JTextField();
            endDateField.setBounds(200, 100, 100, 25);
            rentalPanel.add(endDateField);
            JLabel totalRentLabel = new JLabel("Total Rent: ");
            totalRentLabel.setBounds(50, 130, 250, 25);
            rentalPanel.add(totalRentLabel);
            JTextField price=new JTextField();
            price.setBounds(150, 130, 100, 25);
            rentalPanel.add(price);
            JButton calculateButton = new JButton("Calculate Rent");
            calculateButton.setBounds(50, 170, 150, 25);
            calculateButton.addActionListener(e->calculateRent(price,(Vehicle)vehicleListComboBox.getSelectedItem()));
            rentalPanel.add(calculateButton);
            JButton rentButton = new JButton("Rent");
            rentButton.setBounds(200, 170, 100, 25);
            rentButton.addActionListener(e -> {
                try {
                    rentSelectedVehicle(vehicleListComboBox);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            });
            rentalPanel.add(rentButton);
            rentalPanel.setComponentZOrder(backgroundLabel, rentalPanel.getComponentCount() - 1);
            frame.setContentPane(rentalPanel);
            frame.revalidate();
            frame.repaint();
        }
        private void calculateRent(JTextField price,Vehicle vehicle) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate startDate = LocalDate.parse(startDateField.getText(), formatter);
                    LocalDate endDate = LocalDate.parse(endDateField.getText(), formatter);
                    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
                    double rentPerDay = vehicle.getRentalPrice();
                
    
                if (daysBetween < 0) {
                    price.setText("Invalid date range.");
                } else {
                    double totalRent = daysBetween * rentPerDay;
                    price.setText("$"+totalRent);
                }
            } catch (Exception ex) {
                    price.setText( "Invalid input.");
            }
        }
    
        private void rentSelectedVehicle(JComboBox<Vehicle> vehicleListComboBox) throws SQLException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Vehicle selectedVehicle = (Vehicle) vehicleListComboBox.getSelectedItem();
            LocalDate startDate = LocalDate.parse(startDateField.getText(), formatter);
            LocalDate endDate = LocalDate.parse(endDateField.getText(), formatter);
            long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);
    
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getModel().equals(selectedVehicle.getModel())) {
                    ((Customer) loggedInUser ).rentVehicle(vehicle, rentalDays,endDate, loggedInUser .getUsername());
                    return;
                }
            }
        }
        private void managerental() {
            JPanel rentalPanel = new JPanel();
            rentalPanel.setLayout(new GridLayout(5, 2));
            JLabel rentalIdLabel = new JLabel("Select Rental ID:");
            JComboBox<RentalHistory> rentalIdComboBox = new JComboBox<>();
            rentalPanel.add(rentalIdLabel);
            rentalPanel.add(rentalIdComboBox);
            loadRentals(rentalIdComboBox);
            JButton updateButton = new JButton("Update Rental Status");
            updateButton.addActionListener(e -> updateRentalStatus(rentalIdComboBox));
            rentalPanel.add(updateButton);
            JButton deleteButton = new JButton("Delete Rental Record");
            deleteButton.addActionListener(e -> deleteRentalRecord(rentalIdComboBox));
            rentalPanel.add(deleteButton);
            JButton back=new JButton("<-");
            back.addActionListener(e->showAdminPanel());
            rentalPanel.add(back);
            frame.setContentPane(rentalPanel);
            frame.revalidate();
    }
    private void loadRentals(JComboBox<RentalHistory> rentalIdComboBox) {
    List<RentalHistory> rentals = DatabaseUtility.getAllRentals();
    for (RentalHistory rental : rentals) {
    rentalIdComboBox.addItem(rental);
    }
    }
    private void updateRentalStatus(JComboBox<RentalHistory> rentalIdComboBox) {
    Integer selectedRentalId = (Integer) ((RentalHistory) rentalIdComboBox.getSelectedItem()).getRentalid();
    if (selectedRentalId != null) {
    int confirmation = JOptionPane.showConfirmDialog(frame,
            "Do you want to mark this rental as returned?", "Update Rental Status",
            JOptionPane.YES_NO_OPTION);
    
    if (confirmation == JOptionPane.YES_OPTION) {
        DatabaseUtility.updateRentalStatus(selectedRentalId, true);
        JOptionPane.showMessageDialog(frame, "Rental marked as returned.");
    }
    }
    }
    private void deleteRentalRecord(JComboBox<RentalHistory> rentalIdComboBox) {
    Integer selectedRentalId = (Integer) ((RentalHistory)rentalIdComboBox.getSelectedItem()).getRentalid();
    if (selectedRentalId != null) {
    int confirmation = JOptionPane.showConfirmDialog(frame,
            "Do you want to delete this rental record?", "Delete Rental Record",
            JOptionPane.YES_NO_OPTION);
    
    if (confirmation == JOptionPane.YES_OPTION) {
        int a=DatabaseUtility.deleteRentalRecord(selectedRentalId);
        if(a==1){

        JOptionPane.showMessageDialog(frame, "Rental record deleted.");
    }
    else{
        JOptionPane.showMessageDialog(frame, "Rent not returned.");
    }
}
    }
    }
    private void showUpdateProfilePanel() {
    JPanel updateProfilePanel = new JPanel();
    updateProfilePanel.setLayout(new GridLayout(5, 2));
    
    updateProfilePanel.add(new JLabel("New Username:"));
    JTextField newUsernameField = new JTextField();
    updateProfilePanel.add(newUsernameField);
    
    updateProfilePanel.add(new JLabel("New Password:"));
    JPasswordField newPasswordField = new JPasswordField();
    updateProfilePanel.add(newPasswordField);
    
    updateProfilePanel.add(new JLabel("Phone Number:"));
    JTextField phoneField = new JTextField();
    updateProfilePanel.add(phoneField);
    
    JButton updateButton = new JButton("Update Profile");
    updateButton.addActionListener(e -> updateUserProfile(newUsernameField, newPasswordField,  phoneField));
    updateProfilePanel.add(updateButton);
    
    JButton backButton = new JButton("Back");
    backButton.addActionListener(e -> showRentalPanel());
    updateProfilePanel.add(backButton);
    
    frame.setContentPane(updateProfilePanel);
    frame.revalidate();
    }
    
    private void updateUserProfile(JTextField newUsernameField, JPasswordField newPasswordField, JTextField phoneField) {
    String newUsername = newUsernameField.getText();
    String newPassword = new String(newPasswordField.getPassword());
    String phoneNumber = phoneField.getText();
    
    if (newUsername.isEmpty() || newPassword.isEmpty() || phoneNumber.isEmpty()) {
    JOptionPane.showMessageDialog(frame, "All fields are required.");
    return;
    }
    
    try {
    DatabaseUtility.updateUserProfile(loggedInUser.getId(), newUsername, newPassword,phoneNumber);
    JOptionPane.showMessageDialog(frame, "Profile updated successfully!");
    showRentalPanel();
    } catch (SQLException e) {
    JOptionPane.showMessageDialog(frame, "Error updating profile.");
    e.printStackTrace();
    }
    }
    
    
    private void showRentalHistoryPanel() {
    JPanel rentalHistoryPanel = new JPanel();
    rentalHistoryPanel.setLayout(new BorderLayout());
    try {
    List<RentalHistory> history = DatabaseUtility.getRentalHistory(loggedInUser.getUsername());
    StringBuilder historyText = new StringBuilder("Rental History:\n");
    
    for (RentalHistory entry : history) {
        historyText.append("Vehicle ID: ").append(entry.getVehicleId())
                .append(" rentalID: ").append(entry.getRentalid())
                .append(" Rental Days: ").append(entry.getRentalDays())
                .append(" Rental Price: ").append(entry.getRentalPrice())
                .append(" Returned Status:").append(entry.getReturnedStatus())
                .append(" Rent_end Date:").append(entry.getenddate())
                .append(" Fine:").append(entry.getFine())
            .append("\n");
}

JTextArea historyTextArea = new JTextArea(historyText.toString());
historyTextArea.setEditable(false);
rentalHistoryPanel.add(new JScrollPane(historyTextArea), BorderLayout.CENTER);

} catch (SQLException e) {
JOptionPane.showMessageDialog(frame, "Error fetching rental history.");
e.printStackTrace();
}

JButton backButton = new JButton("Back");
backButton.addActionListener(e -> showRentalPanel());
rentalHistoryPanel.add(backButton, BorderLayout.SOUTH);

frame.setContentPane(rentalHistoryPanel);
frame.revalidate();
}

    private void showAdminPanel() {
        JPanel adminPanel = new JPanel();
        adminPanel.setLayout(null); 

        JButton manageUsersButton = new JButton("Manage Users");
        manageUsersButton.setBounds(50, 50, 150, 25);
        manageUsersButton.addActionListener(e -> manageUsers());
        adminPanel.add(manageUsersButton);

        JButton manageVehiclesButton = new JButton("Manage Vehicles");
        manageVehiclesButton.setBounds(50, 100, 150, 25);
        manageVehiclesButton.addActionListener(e -> manageVehicles());
        adminPanel.add(manageVehiclesButton);

        JButton manageRentalsButton = new JButton("Manage Rentals");
        manageRentalsButton.setBounds(50, 150, 150, 25);
        manageRentalsButton.addActionListener(e -> managerental());
        adminPanel.add(manageRentalsButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(50, 200, 150, 25);
        logoutButton.addActionListener(e -> showLoginPanel());
        adminPanel.add(logoutButton);

        frame.setContentPane(adminPanel);
        frame.revalidate();
    }

    private void manageUsers() {
        JPanel manageUsersPanel = new JPanel();
        manageUsersPanel.setLayout(null); 

        JTextArea userListArea = new JTextArea(10, 50);
        userListArea.setEditable(false);
        StringBuilder userList = new StringBuilder("Users List:\n");
        for (User  user : users) {
            userList.append("UserId:").append(user.getId()).append(",   Name:").append(user.getUsername()).append(",  Role:").append(user.getRole()).append(",   Phone number:").append(user.getPhoneNumber()).append("\n");
        }
        userListArea.setText(userList.toString());
        JScrollPane scrollPane = new JScrollPane(userListArea);
        scrollPane.setBounds(50, 50, 300, 200);
        manageUsersPanel.add(scrollPane);

        JLabel userIdLabel = new JLabel("Enter User ID to delete:");
        userIdLabel.setBounds(50, 270, 150, 25);
        manageUsersPanel.add(userIdLabel);

        JTextField userIdField = new JTextField(5);
        userIdField.setBounds(200, 270, 50, 25);
        manageUsersPanel.add(userIdField);

        JButton deleteButton = new JButton("Delete User");
        deleteButton.setBounds(260, 270, 150, 25);
        deleteButton.addActionListener(e -> deleteUser (userIdField));
        manageUsersPanel.add(deleteButton);

        JButton backButton = new JButton("Back");
        backButton.setBounds(50, 310, 100, 25);
        backButton.addActionListener(e -> showAdminPanel());
        manageUsersPanel.add(backButton);

        frame.setContentPane(manageUsersPanel);
        frame.revalidate();
    }

    private void deleteUser (JTextField userIdField) {
        String userIdText = userIdField.getText();
        if (userIdText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "User  ID is required to delete.");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdText);
            if (DatabaseUtility.deleteUser (userId)) {
                JOptionPane.showMessageDialog(frame, "User  deleted successfully.");
                users = DatabaseUtility.getUsers(); 
                showAdminPanel(); 
            } else {
                JOptionPane.showMessageDialog(frame, "User  not found.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid User ID.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "An error occurred while deleting the user.");
        }
    }

    private void manageVehicles() {
        JPanel manageVehiclesPanel = new JPanel();
        manageVehiclesPanel.setLayout(null); // Set layout to null for absolute positioning

        JButton viewVehiclesButton = new JButton("View Vehicles");
        viewVehiclesButton.setBounds(50, 50, 150, 25);
        viewVehiclesButton.addActionListener(e -> showViewVehiclesPanel());
        manageVehiclesPanel.add(viewVehiclesButton);

        JButton addVehicleButton = new JButton("Add New Vehicle");
        addVehicleButton.setBounds(50, 100, 150, 25);
        addVehicleButton.addActionListener(e -> showAddVehicleForm());
        manageVehiclesPanel.add(addVehicleButton);

        JButton editVehicleButton = new JButton("Edit Vehicle");
        editVehicleButton.setBounds(50, 150, 150, 25);
        editVehicleButton.addActionListener(e -> showEditVehicleForm());
        manageVehiclesPanel.add(editVehicleButton);

        JButton removeVehicleButton = new JButton("Remove Vehicle");
        removeVehicleButton.setBounds(50, 200, 150, 25);
        removeVehicleButton.addActionListener(e -> showRemoveVehicleForm());
        manageVehiclesPanel.add(removeVehicleButton);

        JButton backButton = new JButton("Back");
        backButton.setBounds(50, 250, 150, 25);
        backButton.addActionListener(e -> showAdminPanel());
        manageVehiclesPanel.add(backButton);

        frame.setContentPane(manageVehiclesPanel);
        frame.revalidate();
    }

    private void showAddVehicleForm() {
        JPanel addVehiclePanel = new JPanel();
        addVehiclePanel.setLayout(null); // Set layout to null for absolute positioning

        JLabel nameLabel = new JLabel("Vehicle Name:");
        nameLabel.setBounds(50, 50, 100, 25);
        addVehiclePanel.add(nameLabel);

        vehicleNameField = new JTextField(10);
        vehicleNameField.setBounds(150, 50, 150, 25);
        addVehiclePanel.add(vehicleNameField);

        JLabel modelLabel = new JLabel("Vehicle Model:");
        modelLabel.setBounds(50, 100, 100, 25);
        addVehiclePanel.add(modelLabel);

        vehicleModelField = new JTextField(10);
        vehicleModelField.setBounds(150, 100, 150, 25);
        addVehiclePanel.add(vehicleModelField);

        JLabel licenseLabel = new JLabel("License Plate:");
        licenseLabel.setBounds(50, 150, 100, 25);
        addVehiclePanel.add(licenseLabel);

        licensePlateField = new JTextField(10);
        licensePlateField.setBounds(150, 150, 150, 25);
        addVehiclePanel.add(licensePlateField);

        JLabel typeLabel = new JLabel("Vehicle Type:");
        typeLabel.setBounds(50, 200, 100, 25);
        addVehiclePanel.add(typeLabel);

        vehicleTypeComboBox = new JComboBox<>(new String[]{"Car", "Bike", "Truck", "Van"});
        vehicleTypeComboBox.setBounds(150, 200, 150, 25);
        addVehiclePanel.add(vehicleTypeComboBox);

        JLabel priceLabel = new JLabel("Rental Price:");
        priceLabel.setBounds(50, 250, 100, 25);
        addVehiclePanel.add(priceLabel);

        rentalPriceField = new JTextField(10);
        rentalPriceField.setBounds(150, 250, 150, 25);
        addVehiclePanel.add(rentalPriceField);

        JButton submitButton = new JButton("Add Vehicle");
        submitButton.setBounds(50, 300, 150, 25);
        submitButton.addActionListener(e -> addVehicleToDatabase());
        addVehiclePanel.add(submitButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 300, 150, 25);
        cancelButton.addActionListener(e -> manageVehicles());
        addVehiclePanel.add(cancelButton);

        frame.setContentPane(addVehiclePanel);
        frame.revalidate();
    }

    private void addVehicleToDatabase() {
        String name = vehicleNameField.getText();
        String model = vehicleModelField.getText();
        String licensePlate = licensePlateField.getText();
        String type = (String) vehicleTypeComboBox.getSelectedItem();
        double rentalPrice = Double.parseDouble(rentalPriceField.getText());
        Vehicle v = new Vehicle(0, name, model, licensePlate, type, rentalPrice);

        try {
            DatabaseUtility.addVehicle(v);
            JOptionPane.showMessageDialog(frame, "Vehicle Added Successfully");
            manageVehicles();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error adding vehicle: " + ex.getMessage());
        }
    }

    private void showEditVehicleForm() {
        JPanel editVehiclePanel = new JPanel();
        editVehiclePanel.setLayout(null); 

        JLabel vehicleTypeLabel = new JLabel("Select Vehicle Type:");
        vehicleTypeLabel.setBounds(50, 50, 150, 25);
        editVehiclePanel.add(vehicleTypeLabel);

        JComboBox<String> vehicleTypeComboBox = new JComboBox<>(new String[]{"Car", "Bike", "Truck", "Van"});
        vehicleTypeComboBox.setBounds(200, 50, 150, 25);
        editVehiclePanel .add(vehicleTypeComboBox);

        JLabel vehicleLabel = new JLabel("Select Vehicle:");
        vehicleLabel.setBounds(50, 100, 3150, 25);
        editVehiclePanel.add(vehicleLabel);

        JComboBox<Vehicle> vehicleListComboBox = new JComboBox<>();
        vehicleTypeComboBox.addActionListener(e -> {
            String selectedType = (String) vehicleTypeComboBox.getSelectedItem();
            updateVehicleListComboBox(selectedType, vehicleListComboBox);
        });
        vehicleListComboBox.setBounds(200, 100, 400, 25);
        editVehiclePanel.add(vehicleListComboBox);

        JLabel priceLabel = new JLabel("New Rental Price:");
        priceLabel.setBounds(50, 150, 150, 25);
        editVehiclePanel.add(priceLabel);

        rentalPriceField = new JTextField(10);
        rentalPriceField.setBounds(200, 150, 150, 25);
        editVehiclePanel.add(rentalPriceField);

        JButton submitButton = new JButton("Update Vehicle");
        submitButton.setBounds(50, 200, 150, 25);
        submitButton.addActionListener(e -> updateVehicleInDatabase(vehicleListComboBox));
        editVehiclePanel.add(submitButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 200, 150, 25);
        cancelButton.addActionListener(e -> manageVehicles());
        editVehiclePanel.add(cancelButton);

        frame.setContentPane(editVehiclePanel);
        frame.revalidate();
    }

    private void updateVehicleListComboBox(String vehicleType, JComboBox<Vehicle> vehicleListComboBox) {
        try {
            List<Vehicle> vehicles = DatabaseUtility.getVehiclesByType(vehicleType);
            vehicleListComboBox.removeAllItems();
            for (Vehicle vehicle : vehicles) {
                vehicleListComboBox.addItem(vehicle);
            }
            if (vehicles.isEmpty()) {
                vehicleListComboBox.addItem(new Vehicle(-1, "No vehicles available", "", "", "", 0.0));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error fetching vehicles: " + ex.getMessage());
        }
    }

    private void updateVehicleInDatabase(JComboBox<Vehicle> vehicleListComboBox) {
        Vehicle selectedVehicle = (Vehicle) vehicleListComboBox.getSelectedItem();
        double newRentalPrice = Double.parseDouble(rentalPriceField.getText());

        try {
            DatabaseUtility.updateVehicleRentalPrice(selectedVehicle.getId(), newRentalPrice);
            JOptionPane.showMessageDialog(frame, "Vehicle Updated Successfully");
            manageVehicles();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error updating vehicle: " + ex.getMessage());
        }
    }

    private void showRemoveVehicleForm() {
        JPanel removeVehiclePanel = new JPanel();
        removeVehiclePanel.setLayout(null); 

        JLabel vehicleTypeLabel = new JLabel("Select Vehicle Type:");
        vehicleTypeLabel.setBounds(50, 50, 150, 25);
        removeVehiclePanel.add(vehicleTypeLabel);

        JComboBox<String> vehicleTypeComboBox = new JComboBox<>(new String[]{"Car", "Bike", "Truck", "Van"});
        vehicleTypeComboBox.setBounds(200, 50, 150, 25);
        removeVehiclePanel.add(vehicleTypeComboBox);

        JLabel vehicleLabel = new JLabel("Select Vehicle to Remove:");
        vehicleLabel.setBounds(50, 100, 200, 25);
        removeVehiclePanel.add(vehicleLabel);

        JComboBox<Vehicle> vehicleListComboBox = new JComboBox<>();
        vehicleTypeComboBox.addActionListener(e -> {
            String selectedType = (String) vehicleTypeComboBox.getSelectedItem();
            updateVehicleListComboBox(selectedType, vehicleListComboBox);
        });
        vehicleListComboBox.setBounds(200, 100, 400, 25);
        removeVehiclePanel.add(vehicleListComboBox);

        JButton submitButton = new JButton("Remove Vehicle");
        submitButton.setBounds(50, 150, 150, 25);
        submitButton.addActionListener(e -> removeVehicleFromDatabase(vehicleListComboBox));
        removeVehiclePanel.add(submitButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 150, 150, 25);
        cancelButton.addActionListener(e -> manageVehicles());
        removeVehiclePanel.add(cancelButton);

        frame.setContentPane(removeVehiclePanel);
        frame.revalidate();
    }

    private void removeVehicleFromDatabase(JComboBox<Vehicle> vehicleListComboBox) {
        Vehicle selectedVehicle = (Vehicle) vehicleListComboBox.getSelectedItem();

        try {
            DatabaseUtility.removeVehicle(selectedVehicle.getId());
            JOptionPane .showMessageDialog(frame, "Vehicle Removed Successfully");
            manageVehicles();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error removing vehicle: " + ex.getMessage());
        }
    }

    private void showViewVehiclesPanel() {
            
        JComboBox<String> vehicleTypeComboBox = new JComboBox<>(new String[]{ "Car", "Bike", "Truck", "Van"});
        
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Select Vehicle Type:"));
        filterPanel.add(vehicleTypeComboBox);
        JButton back=new JButton("<-");
        back.addActionListener(e->manageVehicles());
        filterPanel.add(back);
        JButton showButton = new JButton("Show Vehicles");
        showButton.addActionListener(e -> displayVehiclesByType((String) vehicleTypeComboBox.getSelectedItem()));
        filterPanel.add(showButton);
        JTextArea vehicleTextArea = new JTextArea(20, 40);
        vehicleTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(vehicleTextArea);

        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        viewPanel.add(filterPanel, BorderLayout.NORTH);
        viewPanel.add(scrollPane, BorderLayout.CENTER);

        frame.setContentPane(viewPanel);
        frame.revalidate();
        
    } 
    private void displayVehiclesByType(String vehicleType) {
        try {
            List<Vehicle> filteredVehicles;
                filteredVehicles = DatabaseUtility.getVehiclesByType(vehicleType); 
            
    
            StringBuilder vehicleInfo = new StringBuilder("List of Vehicles:\n");
            for (Vehicle vehicle : filteredVehicles) {
                vehicleInfo.append(vehicle.getName())
                        .append(" - Model: ").append(vehicle.getModel())
                        .append(" - Plate: ").append(vehicle.getLicensePlate())
                        .append(" - Type: ").append(vehicle.getType())
                        .append(" - Price: ").append(vehicle.getRentalPrice())
                        .append("\n");
            }
            JTextArea vehicleTextArea = new JTextArea(vehicleInfo.toString());
            vehicleTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(vehicleTextArea);
            JPanel viewPanel = (JPanel) frame.getContentPane();
            viewPanel.removeAll();
            JButton back=new JButton("<-");
            back.addActionListener(e->manageVehicles());
            viewPanel.add(back,BorderLayout.NORTH);
            viewPanel.add(scrollPane, BorderLayout.CENTER);
            frame.revalidate();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error fetching vehicles: " + ex.getMessage());
        }
    }
    
}