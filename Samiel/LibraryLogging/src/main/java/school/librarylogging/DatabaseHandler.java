package school.librarylogging;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

	/**
	 * Finds and displays books in a table-like format.
	 * Displays book_id, book_name, and borrowed (1 or 0) columns.
	 * 
	 * @param bookName Optional filter by book name (null or empty for all books)
	 */
	static void findBook(String bookName) {
		String query;
		if (bookName == null || bookName.trim().isEmpty()) {
			query = "SELECT book_id, book_name, borrowed FROM book_record";
		} else {
			query = "SELECT book_id, book_name, borrowed FROM book_record WHERE book_name LIKE ?";
		}
		
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			
			if (bookName != null && !bookName.trim().isEmpty()) {
				statement.setString(1, "%" + bookName.trim() + "%");
			}
			
			try (ResultSet result = statement.executeQuery()) {
				// Print table header
				System.out.println("+" + "-".repeat(10) + "+" + "-".repeat(32) + "+" + "-".repeat(10) + "+");
				System.out.printf("| %-8s | %-30s | %-8s |%n", "book_id", "book_name", "borrowed");
				System.out.println("+" + "-".repeat(10) + "+" + "-".repeat(32) + "+" + "-".repeat(10) + "+");
				
				boolean hasResults = false;
				while (result.next()) {
					hasResults = true;
					int bookId = result.getInt("book_id");
					String name = result.getString("book_name");
					int borrowed = result.getInt("borrowed");
					
					// Truncate book name if too long
					if (name != null && name.length() > 30) {
						name = name.substring(0, 27) + "...";
					}
					
					System.out.printf("| %-8d | %-30s | %-8d |%n", bookId, name, borrowed);
				}
				
				System.out.println("+" + "-".repeat(10) + "+" + "-".repeat(32) + "+" + "-".repeat(10) + "+");
				
				if (!hasResults) {
					System.out.println("No books found.");
				}
			}
		} catch (SQLException ex) {
			System.out.println("Error: " + ex.getMessage());
		}
	}
	
	/**
	 * Finds and returns book data for GUI display.
	 * Returns a 2D array with columns: book_id, book_name, borrowed
	 * 
	 * @param bookName Optional filter by book name (null or empty for all books)
	 * @return Object[][] containing book data for table display
	 */
	static Object[][] findBookAsTableData(String bookName) {
		String query;
		if (bookName == null || bookName.trim().isEmpty()) {
			query = "SELECT book_id, book_name, borrowed FROM book_record";
		} else {
			query = "SELECT book_id, book_name, borrowed FROM book_record WHERE book_name LIKE ?";
		}
		
		List<Object[]> dataList = new ArrayList<>();
		
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			
			if (bookName != null && !bookName.trim().isEmpty()) {
				statement.setString(1, "%" + bookName.trim() + "%");
			}
			
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					Object[] row = new Object[3];
					row[0] = result.getInt("book_id");
					row[1] = result.getString("book_name");
					row[2] = result.getInt("borrowed");
					dataList.add(row);
				}
			}
		} catch (SQLException ex) {
			System.out.println("Error: " + ex.getMessage());
		}
		
		return dataList.toArray(new Object[0][]);
	}
}
