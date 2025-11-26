package school.librarylogging;

import java.sql.*;
import java.util.Scanner;

public class DatabaseHandler {

	private static final String url = "jdbc:mysql://localhost:3306/library_logging_system";
	private static final String username = "root";
	private static final String password = "";
	protected static final Scanner in = new Scanner(System.in);
	public static Connection conn;

	public static void main(String[] args) {

		try {
			conn = DriverManager.getConnection(url, username, password);
			System.out.println("Connected to database!");

			// viewBookDataOnDatabase(); // TESTING PURPOSES
			addBookDataToDatabase("Science", 1);
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}

	}

	/*
	 * USE TO ADD DATA
	 */
	static void addBookDataToDatabase(String bookName, int isBorrowed) {
		String query = "INSERT INTO book_record (book_name, borrowed) VALUES (?, ?)";
		if (isBorrowed > 1) {
			while (true) {
				System.out.println("Is this book borrowed?");
				isBorrowed = in.nextInt();
				in.nextLine();
				if (isBorrowed <= 1) {
					break;
				}
			}
		}
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, bookName);
			statement.setInt(2, isBorrowed);
			statement.executeUpdate();
			System.out.println("Data added successfully!");

			if (conn != null) {
				conn.close();
				System.out.println("Connection closed!");
			} else {
				System.out.println("Failed to close the connection!");
			}
		} catch (SQLException ex) {
			System.out.println("Error!" + ex.getMessage());
		}
	}

	/*
	 * USE TO VIEW DATA
	 */
	static void viewBookData() {
		String query = "SELECT * FROM book_record";
		try {
			PreparedStatement statement = conn.prepareStatement(query);

			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					System.out.println(result.getString("book_name") + " " + result.getInt("borrowed"));
				}
			} catch (SQLException ex) {
				System.out.println("Error! " + ex.getMessage());
			}
		} catch (SQLException ex) {
			System.out.println("Error! " + ex.getMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	static void updateBookDataOnDataBase(String bookName, int isBorrowed) {
		String query = "UPDATE book_record SET borrowed = ? WHERE book_name = ?";
		/*
		 * place logic to check if book is being borrowed or being returned here
		 */

		try { // Execute query
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, isBorrowed);
			statement.setString(2, bookName);
			statement.executeUpdate();
			System.out.println("Data updated successfully!");
		} catch (SQLException ex) {
			System.out.println("Error! " + ex.getMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	static void findBook(String bookName) {
		String query = "SELECT book_name, borrowed FROM book_record"; // Finds book in record.
		try {
			PreparedStatement statement = conn.prepareStatement(query);

			statement.getMetaData();
		} catch (SQLException ex) {
			System.out.println("Error: " + ex.getMessage());
		}
	}
}
