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

		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}

	}

	/**
	 * Helper method to build book query based on filter.
	 * Reduces code duplication between findBook() and findBookAsTableData().
	 * 
	 * @param bookName Optional filter by book name (null or empty for all books)
	 * @return SQL query string
	 */
	private static String buildBookQuery(String bookName) {
		if (bookName == null || bookName.trim().isEmpty()) {
			return "SELECT book_id, book_name, borrowed FROM book_record";
		} else {
			return "SELECT book_id, book_name, borrowed FROM book_record WHERE book_name LIKE ?";
		}
	}

	/*
	 * USE TO ADD DATA
	 */
	static void addBookDataToDatabase(String bookName, int isBorrowed) {
		String query = "INSERT INTO book_record (book_name, borrowed) VALUES (?, ?)";
		// Ensure isBorrowed is a valid value (0 or 1)
		if (isBorrowed < 0 || isBorrowed > 1) {
			isBorrowed = 0;
		}
		try (PreparedStatement statement = conn.prepareStatement(query)) {
			statement.setString(1, bookName);
			statement.setInt(2, isBorrowed);
			statement.executeUpdate();
			System.out.println("Data added successfully!");
		} catch (SQLException ex) {
			System.out.println("Error!" + ex.getMessage());
		}
	}

	/**
	 * Removes a book from the database by its ID.
	 * 
	 * @param bookId The ID of the book to remove
	 */
	static void removeBookFromDatabase(int bookId) {
		String query = "DELETE FROM book_record WHERE book_id = ?";
		try (PreparedStatement statement = conn.prepareStatement(query)) {
			statement.setInt(1, bookId);
			int rowsAffected = statement.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Book removed successfully!");
			} else {
				System.out.println("No book found with ID: " + bookId);
			}
		} catch (SQLException ex) {
			System.out.println("Error! " + ex.getMessage());
		}
	}

	/**
	 * Updates a book in the database.
	 * 
	 * @param bookId The ID of the book to update
	 * @param newBookName The new name for the book (empty to keep unchanged)
	 * @param isBorrowed The borrowed status (0 or 1)
	 */
	static void updateBookInDatabase(int bookId, String newBookName, int isBorrowed) {
		String query;
		if (newBookName != null && !newBookName.trim().isEmpty()) {
			query = "UPDATE book_record SET book_name = ?, borrowed = ? WHERE book_id = ?";
		} else {
			query = "UPDATE book_record SET borrowed = ? WHERE book_id = ?";
		}
		
		try (PreparedStatement statement = conn.prepareStatement(query)) {
			if (newBookName != null && !newBookName.trim().isEmpty()) {
				statement.setString(1, newBookName.trim());
				statement.setInt(2, isBorrowed);
				statement.setInt(3, bookId);
			} else {
				statement.setInt(1, isBorrowed);
				statement.setInt(2, bookId);
			}
			int rowsAffected = statement.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Book updated successfully!");
			} else {
				System.out.println("No book found with ID: " + bookId);
			}
		} catch (SQLException ex) {
			System.out.println("Error! " + ex.getMessage());
		}
	}

	/*
	 * USE TO VIEW DATA
	 */
	static void viewBookData() {
		String query = "SELECT * FROM book_record";
		try (PreparedStatement statement = conn.prepareStatement(query);
			 ResultSet result = statement.executeQuery()) {
			while (result.next()) {
				System.out.println(result.getString("book_name") + " " + result.getInt("borrowed"));
			}
		} catch (SQLException ex) {
			System.out.println("Error! " + ex.getMessage());
		}
	}

	static void updateBookDataOnDataBase(String bookName, int isBorrowed) {
		String query = "UPDATE book_record SET borrowed = ? WHERE book_name = ?";
		/*
		 * place logic to check if book is being borrowed or being returned here
		 */

		try (PreparedStatement statement = conn.prepareStatement(query)) {
			statement.setInt(1, isBorrowed);
			statement.setString(2, bookName);
			statement.executeUpdate();
			System.out.println("Data updated successfully!");
		} catch (SQLException ex) {
			System.out.println("Error! " + ex.getMessage());
		}
	}

	/**
	 * Finds and displays books in a table-like format.
	 * Displays book_id, book_name, and borrowed (1 or 0) columns.
	 * 
	 * @param bookName Optional filter by book name (null or empty for all books)
	 */
	static void findBook(String bookName) {
		String query = buildBookQuery(bookName);
		
		try (PreparedStatement statement = conn.prepareStatement(query)) {
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
		String query = buildBookQuery(bookName);
		
		List<Object[]> dataList = new ArrayList<>();
		
		try (PreparedStatement statement = conn.prepareStatement(query)) {
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
