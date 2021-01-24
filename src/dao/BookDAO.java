package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.BookBean;
import bean.BookInfoBean;
import bean.BookReviewBean;

public class BookDAO {
	Connection con;
	String url = "jdbc:db2://dashdb-txn-sbox-yp-dal09-08.services.dal.bluemix.net:50000/BLUDB";
	String user = "sgr65162";
	String password = "7-z7882xkzvpmxth";
	
	public BookDAO() throws ClassNotFoundException{
		try {
			// Load the IBM Data Server Driver for JDBC and SQLJ with DriverManager
			Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Create Connection
		try {
			con = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	//Returns all books in db
	public Map<String, BookBean> retrieveAllBooks() throws SQLException {
		String query = "select * from BOOK order by CATEGORY";		
		Map<String, BookBean> rv= new HashMap<String, BookBean>();
		PreparedStatement p = con.prepareStatement(query);		
		ResultSet r = p.executeQuery();
		
		while(r.next()){
			String bid = r.getString("BID");
			String title = r.getString("TITLE");
			int price = r.getInt("PRICE");
			String category = r.getString("CATEGORY");			
			BookBean s = new BookBean(bid, title, price,category);			
			rv.put(bid, s);
		}
		r.close();
		p.close();
		return rv;
		}

	public Map<String, BookBean> retrieveBooksByCategory(String category) throws SQLException {
		String query = "select * from BOOK where category='" + category + "'";		
		Map<String, BookBean> rv= new HashMap<String, BookBean>();
		PreparedStatement p = con.prepareStatement(query);		
		ResultSet r = p.executeQuery();
		
		while(r.next()){
			String bid = r.getString("BID");
			String title = r.getString("TITLE");
			int price = r.getInt("PRICE");
			String bookCategory = r.getString("CATEGORY");			
			BookBean s = new BookBean(bid, title, price,bookCategory);			
			rv.put(bid, s);
		}
		r.close();
		p.close();
		return rv;
	}
	
	// Retrieving a BookBean for bid (a single instance will return)
	public BookBean retrieveBook(String bid) throws SQLException {
		String query = "select * from BOOK where bid='" + bid + "'";		
		BookBean book = null;
		PreparedStatement p = con.prepareStatement(query);		
		ResultSet r = p.executeQuery();
		
		while(r.next()){
			String bookID = r.getString("BID");
			String title = r.getString("TITLE");
			int price = r.getInt("PRICE");
			String bookCategory = r.getString("CATEGORY");			
			book = new BookBean(bookID, title, price,bookCategory);
		}
		r.close();
		p.close();
		return book;
	}
	
	public BookInfoBean retrieveBookInfoByBid(String bid) throws SQLException{
		String query = "select B.BID, B.TITLE,B.PRICE,B.CATEGORY,C.DESCRIPTION from BOOK B, BOOKEXTRA C where B.BID = '" + bid + "' and C.BID = '" + bid + "'";
		BookInfoBean bookInfo = null;		
		PreparedStatement p = con.prepareStatement(query);		
		ResultSet r = p.executeQuery();
		
		while(r.next()){
			String bookID = r.getString("BID");
			String title = r.getString("TITLE");
			int price = r.getInt("PRICE");
			String bookCategory = r.getString("CATEGORY");
			String description = r.getString("DESCRIPTION");			
			bookInfo = new BookInfoBean(bookID, title, price, bookCategory, description);
		}
		r.close();
		p.close();
		return bookInfo;
	}
	
	public List<BookReviewBean> retrieveReviewsByBID(String bid) throws SQLException {
		String query = "select * from BOOKREVIEWS WHERE BID='" + bid + "'";		
		List<BookReviewBean> rv = new ArrayList<BookReviewBean>();
		PreparedStatement p = con.prepareStatement(query);		
		ResultSet r = p.executeQuery();
		
		while(r.next()){
			String bookID = r.getString("BID");
			String review = r.getString("REVIEW");
			int rating = r.getInt("RATING");
			String firstname = r.getString("firstname");
			String lastname = r.getString("lastname");			
			BookReviewBean s = new BookReviewBean(bookID, firstname, lastname, rating, review);			
			rv.add(s);
		}
		r.close();
		p.close();
		return rv;
		}
	
	public int insertReview(String bid, String review, int rating, String firstname, String lastname) throws SQLException {
		String query = "insert into BOOKREVIEWS (bid, review, rating, firstname, lastname) VALUES (?, ?, ?,?, ?)";		
		PreparedStatement p = con.prepareStatement(query);
		
		p.setString(1, bid);
		p.setString(2, review);
		p.setInt(3, rating);
		p.setString(4, firstname);
		p.setString(5, lastname);		
		int i = p.executeUpdate();		
		return i;
	}
}
