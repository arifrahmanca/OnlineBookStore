package ctrl;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.AccountBean;
import bean.AddressBean;
import bean.BookBean;
import bean.BookInfoBean;
import bean.BookReviewBean;
import bean.UserBean;
import bean.cartItemBean;
import model.BOOKSTORE;

@WebServlet({ "/OnlineBookStore", "/OnlineBookStore/*" }) 
public class OnlineBookStore extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletContext context;
	private BOOKSTORE model;
	private String removeFromCart;
	private String incrementQuantity;
	private String decrementQuantity;
	private String bid; 
	private String cartButton;
	private String confirmOrder;
	private int itemCounter = 0;
	private boolean isShowCounter = false;
	private String proceedToPayment;
	private boolean isCartEmpty = true;
	private String checkoutButton;
	boolean isLoginSuccess = false;
	boolean isLogged = false;
	boolean isLoginFailed = true;
	boolean isFailedSignup = false;
	boolean isPasswordMismatched = false;
	boolean isInvalidCC = false;
	String errorMgsCC = "";
	
	public OnlineBookStore() {
		super();
	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		context = getServletContext();

		try { 
			model = BOOKSTORE.getInstance();
			context.setAttribute("model", model);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/plain");
		HttpSession session = request.getSession();
		removeFromCart = request.getParameter("removeFromCart");
		incrementQuantity = request.getParameter("incrementQuantity");
		decrementQuantity = request.getParameter("decrementQuantity");
		bid = request.getParameter("bid");
		cartButton = request.getParameter("cartButton");
		confirmOrder = request.getParameter("confirmOrder");
		proceedToPayment = request.getParameter("proceedToPayment");
		checkoutButton = request.getParameter("checkoutButton");
		
		String login = request.getParameter("login");
		String logout = request.getParameter("logout");
		String loginButton = request.getParameter("login-button");
		String paymentLogin = request.getParameter("payment-login");
		String paymentRegister = request.getParameter("payment-register");
		String createAccountButton = request.getParameter("createAccountButton");
		String viewItem = request.getParameter("viewItem");
		String username = request.getParameter("paymentUsername");
		String password = request.getParameter("loginPassword");
		String addReview = request.getParameter("addReview");
		boolean isRedirected = false;
		String paymentButton = request.getParameter("paymentButton");
		
		if (logout != null) {
			isLogged = false;
			isLoginFailed = true;
		}
		
		String category = request.getParameter("category");
		ArrayList<BookBean> books = getBooksByCategory(category);
		request.setAttribute("books", books);
		request.setAttribute("category", category);
		
		// Set homepage with all books
		setBooks(session); 
		
		// Adding book to the cart
		if (request.getParameter("addToCart") != null) {// when user presses add to cart button
			String bookBID = request.getParameter("bookBID");

			BookBean bookItem = null; 
			try {
				bookItem = model.retrieveBookByBid(bookBID);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			addToCart(bookItem, session); // adds book to cart
		}
		
		// Empty cart after placing an order
		if (confirmOrder != null) {
			itemCounter = 0;
			List<cartItemBean> cartItems = new ArrayList<>();
			updateCartPrices(cartItems, session);
			session.setAttribute("items", cartItems);
		}
		
		if (itemCounter > 0) {
			isShowCounter = true;
			isCartEmpty = false;
		} else {
			isShowCounter = false;
			isCartEmpty = true;
		}		
		session.setAttribute("itemCounter", itemCounter);
		session.setAttribute("isCartEmpty", isCartEmpty);
		session.setAttribute("isShowCounter", isShowCounter);
		session.setAttribute("isLogged", isLogged); 
		
		// Redirect to corresponding pages
		if (login != null) {
			isRedirected = false;
			session.setAttribute("isRedirected", isRedirected);
			session.setAttribute("isLogged", isLogged); 
			request.getRequestDispatcher("/Login.jspx").forward(request, response);
		} else if(cartButton != null || removeFromCart != null || incrementQuantity != null || decrementQuantity != null) {
			session.setAttribute("itemCounter", itemCounter);
			request.getRequestDispatcher("/Cart.jspx").forward(request, response);
		} else if (confirmOrder != null) {
			request.getRequestDispatcher("/PaymentSuccessful.jspx").forward(request, response);
		} else if (proceedToPayment != null) { 
			setAddress(request, session);
			request.getRequestDispatcher("/PaymentPage.jspx").forward(request, response);
		} else if (paymentRegister != null) {
			request.getRequestDispatcher("/Register.jspx").forward(request, response);
		} else if (paymentLogin != null) {
			request.getRequestDispatcher("/Login.jspx").forward(request, response);
		} else if (createAccountButton != null) {
			try {
				createAccount(request);
			} catch (SQLException e) {
				e.printStackTrace(); 
			}
			request.setAttribute("isFailedSignup", isFailedSignup);
			request.setAttribute("isPasswordMismatched", isPasswordMismatched);
			
			if (isFailedSignup || isPasswordMismatched) { 
				request.getRequestDispatcher("/Register.jspx").forward(request, response);
			} else {
				request.getRequestDispatcher("/Login.jspx").forward(request, response);
			}
		} else if (viewItem != null) {
			String bookID = viewItem;
			setBookInfo(bookID, session); 
			request.getRequestDispatcher("/ProductPage.jspx").forward(request, response);
		} else if (checkoutButton != null) { 
			isRedirected = true;
			session.setAttribute("isRedirected", isRedirected);
			AccountBean account = null;
			 
			if (isLogged) {
				try {
					account = model.retrieveAccount(username);
				} catch (Exception e) {
					// TODO: handle exception
					e.getStackTrace();
				}
				session.setAttribute("account", account);
				request.getRequestDispatcher("/Checkout.jspx").forward(request, response);
			} else {
				request.getRequestDispatcher("/Redirect.jspx").forward(request, response);
			}  
		} else if (loginButton != null) {
			validateLogin(username, password);
			isRedirected = (boolean) session.getAttribute("isRedirected");
			session.setAttribute("isLogged", isLogged);
			session.setAttribute("isLoginFailed", isLoginFailed);
			
			AccountBean account = null;
			if (isLogged) {
				try {
					account = model.retrieveAccount(username);
				} catch (Exception e) {
					// TODO: handle exception
					e.getStackTrace();
				}
				session.setAttribute("account", account);
				
				if (isRedirected) {
					request.getRequestDispatcher("/Checkout.jspx").forward(request, response);
				} else {
					request.getRequestDispatcher("/MainPage.jspx").forward(request, response);
				} 
			} else {
				request.getRequestDispatcher("/Login.jspx").forward(request, response);
			}
			
		} else if (addReview != null) {
			if(isLogged) {
				try {
					insertReview(request);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} 			
			request.getRequestDispatcher("/ProductPage.jspx").forward(request, response);
			
		} else if(paymentButton != null) {
			validateCC(request);
			if (isInvalidCC) {
				request.getRequestDispatcher("/PaymentPage.jspx").forward(request, response);
			} else {
				request.getRequestDispatcher("/ConfirmOrder.jspx").forward(request, response);
			}
			
		} else {
			request.getRequestDispatcher("/MainPage.jspx").forward(request, response);
		}		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response); 
	}
	
	private ArrayList<BookBean> getBooksByCategory(String category) {
		Map<String, BookBean> bookInfo = new HashMap<>();
		ArrayList<BookBean> books = new ArrayList<BookBean>();

		if (category != null) {
			// access model and get book with specific category
			try {
				bookInfo = model.retriveBooksByCategory(category);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			// access model and get all the books in record
			try {
				bookInfo = model.retriveAllBooks();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (Map.Entry<String, BookBean> entry : bookInfo.entrySet()) {
			books.add(entry.getValue());
		}
		
		return books;
	}
	
	public void setBooks(HttpSession session) {

		if (removeFromCart != null) {
			removeFromCart(bid, session);// removes book from cart
		}

		if (incrementQuantity != null) {
			incrementQuantityOfBook(bid, session);
		}

		if (decrementQuantity != null) {
			decrementQuantityOfBook(bid, session);
		}
	}

	private void addToCart(BookBean book, HttpSession session) {
		itemCounter++;
		if (session.getAttribute("items") == null && book != null) {// if items list is not made make one and add book to cart			
			List<cartItemBean> books = new ArrayList<>();

			cartItemBean item = new cartItemBean(book.getBid(), book.getTitle(), book.getPrice(), book.getCategory(),
					1);
			books.add(item);

			updateCartPrices(books, session);

			session.setAttribute("items", books);
		} else if (book != null) { // if items list is already made just add book to cart
			@SuppressWarnings("unchecked")
			List<cartItemBean> books = (List<cartItemBean>) session.getAttribute("items");
			boolean found = false;

			for (cartItemBean item : books) {
				if (item.getBid().equals(book.getBid())) {
					item.incrementQuantity();
					found = true;
				}
			}

			if (!found) { 
				cartItemBean item = new cartItemBean(book.getBid(), book.getTitle(), book.getPrice(),
						book.getCategory(), 1);
				books.add(item);
			}

			updateCartPrices(books, session);
			session.setAttribute("items", books);
		}
	}

	private void removeFromCart(String bid, HttpSession session) {
		if (session.getAttribute("items") != null) {
			@SuppressWarnings("unchecked")
			List<cartItemBean> books = (List<cartItemBean>) session.getAttribute("items");
			cartItemBean book = null;
			boolean found = false;

			for (cartItemBean item : books) { 
				if (item.getBid().equals(bid)) {
					found = true;
					book = item;
					int quantity = item.getQuantity();
					itemCounter -= quantity; 
				}
			}

			if (found) {
				books.remove(book);
			}

			updateCartPrices(books, session);
			session.setAttribute("items", books);
		}
	}

	private void updateCartPrices(List<cartItemBean> books, HttpSession session) {
		double subtotal = model.getSubtotal(books);
		double shipping = 0;
		double tax = model.getTax(subtotal, shipping);
		double total = model.getTotal(subtotal, shipping, tax);

		session.setAttribute("subtotalPrice", subtotal);
		session.setAttribute("shippingPrice", shipping);
		session.setAttribute("taxPrice", tax);
		session.setAttribute("totalPrice", total);
	}

	private void incrementQuantityOfBook(String bid, HttpSession session) {
		if (session.getAttribute("items") != null) {
			@SuppressWarnings("unchecked")
			List<cartItemBean> books = (List<cartItemBean>) session.getAttribute("items");

			for (cartItemBean item : books) {
				if (item.getBid().equals(bid)) {
					item.incrementQuantity();
					itemCounter++;
				}
			}
			updateCartPrices(books, session);
			session.setAttribute("items", books);
		}
	}

	private void decrementQuantityOfBook(String bid, HttpSession session) {
		if (session.getAttribute("items") != null) {
			@SuppressWarnings("unchecked")
			List<cartItemBean> books = (List<cartItemBean>) session.getAttribute("items");
			int quantity = 1;

			for (cartItemBean item : books) {
				if (item.getBid().equals(bid)) {
					item.decrementQuantity();
					quantity = item.getQuantity();
					itemCounter--;
				}
			} 

			updateCartPrices(books, session);
			session.setAttribute("items", books);

			if (quantity <= 0) {
				removeFromCart(bid, session);
			}
		}
	}
	
	private void setBookInfo(String bid, HttpSession session) {
		BookInfoBean book = null;
		try {
			book = model.retrieveBookInfo(bid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//retrieve reviews
		double rating = 0.0;
		int numOfBooks = 0;
		List<BookReviewBean> bookReviews = new ArrayList<BookReviewBean>();
		try {
			bookReviews = model.retrieveBookReviewsByBID(bid);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	
		for(BookReviewBean e: bookReviews) {
			rating += e.getRating();
			numOfBooks++;
		}
		double avgRating = 0.0;
		if (numOfBooks > 0) {
			avgRating = rating/numOfBooks;
		}
		
		//put book info and reviews in request scope for visitors to see
		session.setAttribute("avgRating", avgRating);
		session.setAttribute("info", book);
		session.setAttribute("reviews", bookReviews);
	}
	
	private void setAddress(HttpServletRequest request, HttpSession session) {
		String shippingFirstName = request.getParameter("shippingFirstName");
		String shippingLastName = request.getParameter("shippingLastName");
		String shippingAddress = request.getParameter("shippingAddress");
		String shippingCity = request.getParameter("shippingCity");
		String shippingProvince = request.getParameter("shippingProvince");
		String shippingPostalCode = request.getParameter("shippingPostalCode");
		String shippingCountry = request.getParameter("shippingCountry"); 
		String shippingPhone = request.getParameter("shippingPhone");
		
		AddressBean shipping = new AddressBean(1, shippingFirstName, shippingLastName, shippingAddress,
				shippingCity, shippingProvince, shippingCountry, shippingPostalCode, shippingPhone);

		// all billing info provided
		String billingFirstname = request.getParameter("billingFirstname");
		String billingLastname = request.getParameter("billingLastname");
		String billingAddress = request.getParameter("billingAddress"); 
		String billingCity = request.getParameter("billingCity");
		String billingProvince = request.getParameter("billingProvince");
		String billingPostalCode = request.getParameter("billingPostalCode");
		String billingCountry = request.getParameter("billingCountry");
		String billingPhone = request.getParameter("billingPhone");
		String sameAs = request.getParameter("hideBilling");
		
		AddressBean billing;
		if (sameAs != null) {
			billing = shipping;
		} else {
			billing = new AddressBean(1, billingFirstname, billingLastname, billingAddress, billingCity, 
					billingProvince, billingCountry, billingPostalCode, billingPhone);
		}
		session.setAttribute("shipping", shipping);
		session.setAttribute("billing", billing);
	}
	
	private List<UserBean> getUsers(){
		List<UserBean> users = new ArrayList<UserBean>();
		try {
			users = model.retrieveAllUsers();
		} catch (SQLException e) {
			e.printStackTrace(); 
		}
		return users;
	}
	
	private void validateLogin(String username, String password) {
		if (isValidLogin(username, password)) {
			isLoginFailed = false;
			isLogged = true;
		} else {
			isLoginFailed = true;
			isLogged = false;
		}
	}
	
	private boolean isValidLogin(String username, String password) {
		List<UserBean> users = getUsers();
		for (UserBean user : users) {
			if (user.getUsername().equals(username)) {
				if (user.getPassword().equals(password)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isUserExist(String username) {
		List<UserBean> users = getUsers();
		for (UserBean user : users) {
			if (user.getUsername().equals(username)) {
					return true;
			}
		}
		return false;
	}
	
	private void createAccount(HttpServletRequest request) throws SQLException {
		String registerUsername = request.getParameter("registerUsername");
		String registerPassword = request.getParameter("registerPassword");
		String retypePassword = request.getParameter("retype-password");
		String firstName = request.getParameter("registerFirstName");
		String lastName = request.getParameter("registerLastName");
		String street = request.getParameter("registerStreet");		
		String city = request.getParameter("registerCity");
		String province = request.getParameter("registerProvince");
		String zip = request.getParameter("registerPostalCode");
		String country = request.getParameter("registerCountry"); 
		String phone = request.getParameter("registerPhone");
		String email = request.getParameter("email");
		 
		if (isUserExist(registerUsername)) {
			isFailedSignup = true;
			isPasswordMismatched = false;
		} else if (!registerPassword.equals(retypePassword)) {
			isPasswordMismatched = true;
			isFailedSignup = false;
		} else {
			isFailedSignup = false;
			isPasswordMismatched = false; 
			model.insertUser(registerUsername, registerPassword, email);
			model.insertAddress(firstName, lastName, street, city, province, zip, country, phone);
		}
	}
	
	private void insertReview(HttpServletRequest request) throws SQLException{
		String review = request.getParameter("bookComment");
		int rating = Integer.parseInt(request.getParameter("rating"));
		
		AccountBean account = (AccountBean) request.getSession().getAttribute("account");
		String firstname = account.getAddress().getFirstName();
		String lastname = account.getAddress().getLastName();
		
		model.insertReview(bid, review, rating, firstname, lastname);
		setBookInfo(bid, request.getSession());
	}
	
	private void setErrorMgsCC(String msg) {
		this.errorMgsCC = msg;
	}
	
	private boolean isAllDigits(String s) {
		for (int i = 0; i < s.length(); i++) {
			if(!Character.isDigit(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	private void validateCC(HttpServletRequest request) {
		LocalDate date = java.time.LocalDate.now();
		int y = date.getYear(); 
		int m = date.getMonthValue();
		
		int year = Integer.parseInt(request.getParameter("year"));
		int month = Integer.parseInt(request.getParameter("month"));
		
		String cardNumber = request.getParameter("cardNumber");
		boolean isValidCard = isAllDigits(cardNumber);
		
		String cvv = request.getParameter("cvv");
		boolean isValidCVV = isAllDigits(cvv); 
		
		if (!isValidCard || cardNumber.length() < 16) {
			isInvalidCC = true;
			setErrorMgsCC("Credit Card Error! Please enter valid Card number");
		} else if (year < y) {
			isInvalidCC = true;
			setErrorMgsCC("Credit Card Error! Please select valid Year.");
		} else if (year >= y && month < m) {
			isInvalidCC = true;
			setErrorMgsCC("Credit Card Error! Please select valid Month.");
		} else if (!isValidCVV) {
			isInvalidCC = true;
			setErrorMgsCC("Credit Card Error! Please enter valid CVV.");
		} else {
			isInvalidCC = false;
		}
		request.getSession().setAttribute("isInvalidCC", isInvalidCC);
		request.getSession().setAttribute("errorMgsCC", errorMgsCC);
	}

}
