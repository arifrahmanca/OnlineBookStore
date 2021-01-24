package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import bean.AddressBean;
import bean.BookBean;
import bean.OrderBean;
import bean.OrderItemBean;

public class OrderDAO {

	Connection con;
	String url = "jdbc:db2://dashdb-txn-sbox-yp-dal09-08.services.dal.bluemix.net:50000/BLUDB";
	String user = "sgr65162";
	String password = "7-z7882xkzvpmxth";

	public OrderDAO() throws ClassNotFoundException {
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

	public AddressBean getAddressById(int addressId) throws SQLException {
		String addressQuery = "SELECT * FROM PO, SHIPPINGADDRESS WHERE ADDRESS.ID=? AND PO.ID=?";
		PreparedStatement stmt = con.prepareStatement(addressQuery);
		stmt.setInt(1, addressId);
		stmt.setInt(2, addressId);

		ResultSet result = stmt.executeQuery();
		AddressBean addressBean = null;

		while (result.next()) {
			int id = result.getInt("ID");
			String fName = result.getString("FNAME");
			String lName = result.getString("LNAME");
			String street = result.getString("STREET");
			String city = result.getString("CITY");
			String province = result.getString("PROVINCE");
			String country = result.getString("COUNTRY");
			String zip = result.getString("ZIP");
			String phone = "";
			addressBean = new AddressBean(id, fName, lName, street, city, province, country, zip, phone);
		}
		result.close();
		stmt.close();
		return addressBean;
	}

	public AddressBean retirveAddressByBid(String bid) throws SQLException {
		String query = "SELECT * FROM BOOK, PO, POITEM, SHIPPINGADDRESS WHERE BOOK.BID=? "
				+ "AND POITEM.BID=? AND PO.ID=POITEM.ID AND  SHIPPINGADDRESS.ID=PO.ID";
		AddressBean address = null;
		PreparedStatement p = con.prepareStatement(query);
		p.setString(1, bid);
		p.setString(2, bid);
		ResultSet r = p.executeQuery();

		while (r.next()) {
			int id = r.getInt("ID");
			String firstName = r.getString("FNAME");
			String lastName = r.getString("LNAME");
			String street = r.getString("STREET");
			String city = r.getString("CITY");
			String province = r.getString("PROVINCE");
			String country = r.getString("COUNTRY");
			String zip = r.getString("ZIP");
			String phone = "";

			address = new AddressBean(id, firstName, lastName, street, city, province, country, zip, phone);
		}
		r.close();
		p.close();
		return address;
	}

	public String getCommentByBid(String bid) throws SQLException {
		String query = "SELECT COMMENT FROM PO, POItem, SHIPPINGADDRESS WHERE PO.ID=POItem.ID AND POItem.BID=? AND SHIPPINGADDRESS.ID=PO.ID";
		PreparedStatement p = con.prepareStatement(query);
		p.setString(1, bid);

		ResultSet r = p.executeQuery();
		String comment = r.getString("COMMENT");
		return comment;
	}

	public List<OrderItemBean> purchasedBookOrders(String bid) throws SQLException {
		String query = "SELECT * FROM BOOK, PO, POITEM, SHIPPINGADDRESS WHERE BOOK.BID=? "
				+ "AND POITEM.BID=? AND PO.ID=POITEM.ID AND  SHIPPINGADDRESS.ID=PO.ID";
		List<OrderItemBean> bookList = new ArrayList<OrderItemBean>();
		PreparedStatement p = con.prepareStatement(query);
		p.setString(1, bid);
		p.setString(2, bid);
		ResultSet r = p.executeQuery();

		while (r.next()) {
			int id = r.getInt("ID");
			String lName = r.getString("LNAME");
			String fName = r.getString("FNAME");
			String status = r.getString("STATUS");
			int quantity = r.getInt("QUANTITY");
			String street = r.getString("STREET");
			String city = r.getString("CITY");
			String province = r.getString("PROVINCE");
			String country = r.getString("COUNTRY");
			String zip = r.getString("ZIP");
			String phone = r.getString("PHONE");

			AddressBean billing = new AddressBean(id, fName, lName, street, city, province, country, zip, phone);
			AddressBean shipping = retirveAddressByBid(bid);
			OrderBean order = new OrderBean(id, lName, fName, status, quantity, shipping, billing);

			String bookID = r.getString("BID");
			String title = r.getString("TITLE");
			int price = r.getInt("PRICE");
			String bookCategory = r.getString("CATEGORY");
			BookBean book = new BookBean(bookID, title, price, bookCategory);

			OrderItemBean item = new OrderItemBean(order, book);
			bookList.add(item);
		}
		r.close();
		p.close();
		return bookList;
	}

	public List<OrderBean> getOrderByBookId(String bid) throws SQLException {
		String query = "SELECT * FROM PO, POItem WHERE PO.ID=POItem.ID AND POItem.BID=?";
		PreparedStatement p = con.prepareStatement(query);
		p.setString(1, bid);
		ResultSet results = p.executeQuery();

		List<OrderBean> orderLists = new ArrayList<OrderBean>();
		while (results.next()) {
			int id = results.getInt("ID");
			String lastName = results.getString("LNAME");
			String firstName = results.getString("FNAME");
			String status = results.getString("STATUS");
			int quantity = results.getInt("QUANTITY");
			AddressBean shipping = getAddressById(id);
			AddressBean billing = shipping;
			OrderBean ob = new OrderBean(id, lastName, firstName, status, quantity, shipping, billing);
			orderLists.add(ob);
		}
		results.close();
		p.close();
		return orderLists;
	}

	public int insertAddress(String street, String province, String country, String zip, String phone)
			throws SQLException {
		String query = "INSERT INTO SHIPPINGADDRESS (street, city, province, country, zip, phone) VALUES (?,?,?,?,?)";
		PreparedStatement p = con.prepareStatement(query);

		p.setString(1, street);
		p.setString(2, province);
		p.setString(3, country);
		p.setString(4, zip);
		p.setString(5, phone);

		int i = p.executeUpdate();
		return i;
	}

	public String retrieveAddressNumber(String street, String province, String country, String zip)
			throws SQLException {
		String query = "select SHIPPINGADDRESS.ID FROM SHIPPINGADDRESS where street=? AND province=? AND country=? AND zip=?";
		PreparedStatement p = con.prepareStatement(query);
		p.setString(1, street);
		p.setString(2, province);
		p.setString(3, country);
		p.setString(4, zip);
		ResultSet results = p.executeQuery();

		String answer = "";
		while (results.next()) {
			String id = results.getString("ID");
			answer = id;
		}
		results.close();
		p.close();
		return answer;
	}

	public int insertPO(String lname, String fname, String status, String address) throws SQLException {
		String query = "INSERT INTO PO (lname, fname, status, address)  VALUES (?,?,?,?)";
		PreparedStatement p = con.prepareStatement(query);

		p.setString(1, lname);
		p.setString(2, fname);
		p.setString(3, status);
		p.setString(4, address);

		int i = p.executeUpdate();
		return i;
	}

	public int insertPOitem(String id, String bookID, int price, int quantity) throws SQLException {
		String query = "INSERT INTO POItem (id, bid, price, quantity)  VALUES (?,?,?,?)";
		PreparedStatement p = con.prepareStatement(query);

		p.setString(1, id);
		p.setString(2, bookID);
		p.setInt(3, price);
		p.setInt(4, quantity);

		int i = p.executeUpdate();
		return i;
	}

	public int insertBillingAddress(String street, String city, String province, String country, String zip, String phone) throws SQLException {
		String query = "INSERT INTO BILLINGADDRESS (street, city, province, country, zip, phone) VALUES (?, ?, ?, ? ,?, ?)";
		PreparedStatement p = con.prepareStatement(query);

		p.setString(1, street);
		p.setString(2, city);
		p.setString(3, province);
		p.setString(4, country);
		p.setString(5, zip);
		p.setString(6, phone);

		int i = p.executeUpdate();
		return i;
	}
}
