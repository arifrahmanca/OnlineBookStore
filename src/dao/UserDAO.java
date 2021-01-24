package dao;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import bean.AccountBean;
import bean.AddressBean;
import bean.UserBean;

public class UserDAO {
	Connection con;
	String url = "jdbc:db2://dashdb-txn-sbox-yp-dal09-08.services.dal.bluemix.net:50000/BLUDB";
	String user = "sgr65162";
	String password = "7-z7882xkzvpmxth";

	public UserDAO() throws ClassNotFoundException {
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
	
	public List<UserBean> retrieveAllUsers() throws SQLException {
		String query = "SELECT * FROM USER";
		List<UserBean> users = new ArrayList<UserBean>();
		PreparedStatement p = con.prepareStatement(query);
		ResultSet r = p.executeQuery();

		while (r.next()) {
			int id = r.getInt("ID");
			String username = r.getString("USERNAME");
			String password = r.getString("PASSWORD");
			String email = r.getString("EMAIL");	
			UserBean user = new UserBean(id, username, password, email);
			users.add(user);
		}
		r.close();
		p.close();
		return users;
	}
	
	public AddressBean retrieveAddressById(int id) throws SQLException {
		String query = "SELECT * FROM SHIPPINGADDRESS WHERE ID=" + id;
		PreparedStatement p = con.prepareStatement(query);
		ResultSet r = p.executeQuery();		
		AddressBean address = null;
		
		while (r.next()) {
			String fName = r.getString("FNAME");
			String lName = r.getString("LNAME");
			String street = r.getString("STREET"); 
			String city = r.getString("CITY"); 
			String province = r.getString("PROVINCE"); 
			String zip = r.getString("ZIP");
			String country = r.getString("COUNTRY"); 
			String phone = r.getString("PHONE");
			address = new AddressBean(id, fName, lName, street, city, province, zip, country, phone);
		}
		
		r.close();
		p.close();
		return address;
	}
	
	public AccountBean retrieveAccountById(int id) throws SQLException, NoSuchAlgorithmException {
		String query = "SELECT * FROM USER WHERE ID=" + id;
		PreparedStatement p = con.prepareStatement(query);
		ResultSet r = p.executeQuery();		
		AccountBean account = null;
		
		while (r.next()) {
			String username = r.getString("USERNAME");
			String password = r.getString("PASSWORD");
			String email = r.getString("EMAIL");
			UserBean user = new UserBean(id, username, password, email);
			AddressBean address = retrieveAddressById(id);
			account = new AccountBean(user, address);			
		}
		
		r.close();
		p.close();
		return account;
	}
	
	public AccountBean retrieveAccount(String username) throws SQLException, NoSuchAlgorithmException {
		String query = "SELECT * FROM USER WHERE USERNAME='" + username + "'";
		PreparedStatement p = con.prepareStatement(query);
		ResultSet r = p.executeQuery();		
		AccountBean account = null;
		
		while (r.next()) {
			String password = r.getString("PASSWORD");
			String email = r.getString("EMAIL");
			int id = r.getInt("ID");
			
			UserBean user = new UserBean(id, username, password, email);
			AddressBean address = retrieveAddressById(id);
			account = new AccountBean(user, address);			
		}
		
		r.close();
		p.close();
		return account;
	}

	public int insertUserInfo(String userID, String firstName, String lastName, String addressOne, String addressTwo,
			String citySanitized, String provinceSanitized, String postal, String countrySanitized, String phone)
			throws SQLException {
		String query = "INSERT INTO USERINFO (USERNAME, FNAME, LNAME,ADDRESS,ADDRESS2, CITY,PROVINCE,POSTALCODE,COUNTRY, PHONENUMBER) VALUES (?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement p = con.prepareStatement(query);

		p.setString(1, userID);
		p.setString(2, firstName);
		p.setString(3, lastName);
		p.setString(4, addressOne);
		p.setString(5, addressTwo);
		p.setString(6, citySanitized);
		p.setString(7, provinceSanitized);
		p.setString(8, postal);
		p.setString(9, countrySanitized);
		p.setString(10, phone);

		int i = p.executeUpdate();
		return i;
	}

	public int insertUserInfoWithoutPhone(String userID, String firstName, String lastName, String addressOne,
			String addressTwo, String citySanitized, String provinceSanitized, String postal, String countrySanitized)
			throws SQLException {
		String query = "INSERT INTO USERINFO (USERNAME, FNAME, LNAME,ADDRESS,ADDRESS2, CITY,PROVINCE,POSTALCODE,COUNTRY) VALUES (?,?,?,?,?,?,?,?,?)";
		PreparedStatement p = con.prepareStatement(query);

		p.setString(1, userID);
		p.setString(2, firstName);
		p.setString(3, lastName);
		p.setString(4, addressOne);
		p.setString(5, addressTwo);
		p.setString(6, citySanitized);
		p.setString(7, provinceSanitized);
		p.setString(8, postal);
		p.setString(9, countrySanitized);

		int i = p.executeUpdate();
		return i;
	}

	public int insertUserInfoWithoutAddressTwo(String userID, String firstName, String lastName, String addressOne,
			String citySanitized, String provinceSanitized, String postal, String countrySanitized, String phone)
			throws SQLException {
		String query = "INSERT INTO USERINFO (USERNAME, FNAME, LNAME,ADDRESS, CITY,PROVINCE,POSTALCODE,COUNTRY, PHONENUMBER) VALUES (?,?,?,?,?,?,?,?,?)";
		PreparedStatement p = con.prepareStatement(query);

		p.setString(1, userID);
		p.setString(2, firstName);
		p.setString(3, lastName);
		p.setString(4, addressOne);
		p.setString(5, citySanitized);
		p.setString(6, provinceSanitized);
		p.setString(7, postal);
		p.setString(8, countrySanitized);
		p.setString(9, phone);

		int i = p.executeUpdate();
		return i;
	}

	public int insertUserInfoWithoutAddressNPhone(String userID, String firstName, String lastName, String addressOne,
			String citySanitized, String provinceSanitized, String postal, String countrySanitized)
			throws SQLException {
		String query = "INSERT INTO USERINFO (USERNAME, FNAME, LNAME,ADDRESS, CITY,PROVINCE,POSTALCODE,COUNTRY) VALUES (?,?,?,?,?,?,?,?)";
		PreparedStatement p = con.prepareStatement(query);

		p.setString(1, userID);
		p.setString(2, firstName);
		p.setString(3, lastName);
		p.setString(4, addressOne);
		p.setString(5, citySanitized);
		p.setString(6, provinceSanitized);
		p.setString(7, postal);
		p.setString(8, countrySanitized);

		int i = p.executeUpdate();
		return i;
	}

	public int insertUser(String username, String password, String email) throws SQLException {
		String query = "INSERT INTO USER (USERNAME, PASSWORD, EMAIL) VALUES  (?,?,?)";
		PreparedStatement p = con.prepareStatement(query);

		p.setString(1, username);
		p.setString(2, password);
		p.setString(3, email);

		int i = p.executeUpdate();
		return i;
	}

	public int insertAddress(String firstName, String lastName, String street, String city,
			String province, String zip, String country, String phone) throws SQLException {
		String query = "INSERT INTO SHIPPINGADDRESS(FNAME, LNAME, STREET, CITY, PROVINCE, ZIP, COUNTRY, PHONE) VALUES (?,?,?,?,?,?,?,?)";
		PreparedStatement p = con.prepareStatement(query);
		
		p.setString(1, firstName);
		p.setString(2, lastName);
		p.setString(3, street);
		p.setString(4, city);
		p.setString(5, province);
		p.setString(6, zip);
		p.setString(7, country);
		p.setString(8, phone);
		
		int i = p.executeUpdate();
		return i;		
	}
	
	public int deleteUser(String username) throws SQLException {
		String query = "DELETE FROM USER WHERE USERNAME=?";
		PreparedStatement p = con.prepareStatement(query);
		p.setString(1, username);

		int i = p.executeUpdate();
		return i;
	}

	public int insertPO(String lname, String fname, String status, String address) throws SQLException {
		String query = "INSERT INTO PO (lname, fname, status, address)  VALUES  (?,?,?,?)";
		PreparedStatement p = con.prepareStatement(query);
		p.setString(1, lname);
		p.setString(2, fname);
		p.setString(3, status);
		p.setString(4, address);

		int i = p.executeUpdate();
		return i;
	}
}
