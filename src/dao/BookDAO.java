package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import bean.AddressBean;
import bean.BookBean;
import bean.BookInfoBean;
import bean.BookReviewBean;
import bean.OrderBean;
import bean.OrderItemBean;

public class BookDAO {
	DataSource ds;
	
	public BookDAO() throws ClassNotFoundException{
		try{
			ds = (DataSource) (new InitialContext()).lookup("java:/comp/env/jdbc/EECS");
		} catch(NamingException e) {
				e.printStackTrace();
		}
	}
	
	//Returns all books in db
	public Map<String, BookBean> retrieveAllBooks() throws SQLException {
		String query = "select * from BOOK order by CATEGORY";
		
		Map<String, BookBean> rv= new HashMap<String, BookBean>();
		Connection con = (Connection) this.ds.getConnection();
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
		con.close();
		return rv;
		}

	public Map<String, BookBean> retrieveBooksByCategory(String category) throws SQLException {
		String query = "select * from BOOK where category='" + category + "'";
		
		Map<String, BookBean> rv= new HashMap<String, BookBean>();
		Connection con = (Connection) this.ds.getConnection();
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
		con.close();
		return rv;
	}
	
	// Retrieving a BookBean for bid (a single instance will return)
	public BookBean retrieveBook(String bid) throws SQLException {
		String query = "select * from BOOK where bid='" + bid + "'";		
		BookBean book = null;
		
		Connection con = (Connection) this.ds.getConnection();
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
		con.close();
		return book;
	}
	
	public BookInfoBean retrieveBookInfoByBid(String bid) throws SQLException{
		String query = "select B.BID, B.TITLE,B.PRICE,B.CATEGORY,C.DESCRIPTION from BOOK B, BOOKEXTRA C where B.BID = '" + bid + "' and C.BID = '" + bid + "'";
		BookInfoBean bookInfo = null;
		
		Connection con = (Connection) this.ds.getConnection();
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
		con.close();
		return bookInfo;
	}
	
	public List<BookReviewBean> retrieveReviewsByBID(String bid) throws SQLException {
		String query = "select * from BOOKREVIEWS WHERE BID='" + bid + "'";
		
		List<BookReviewBean> rv = new ArrayList<BookReviewBean>();
		Connection con = (Connection) this.ds.getConnection();
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
		con.close();
		return rv;
		}
	
	public int insertReview(String bid, String firstname, String lastname, int rating, String review) throws SQLException {
		String query = "insert into BOOKREVIEWS (bid, review,rating, firstname, lastname) VALUES (?, ?, ?,?, ?)";
		
		Connection con = (Connection) this.ds.getConnection();
		PreparedStatement p = con.prepareStatement(query);
		
		p.setString(1, bid);
		p.setString(2, review);
		p.setInt(3, rating);
		p.setString(4, firstname);
		p.setString(5, lastname);
		
		int i = p.executeUpdate();
		
		con.close();
		
		return i;
	}
}
