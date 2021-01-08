package ctrl;

import java.io.IOException;
import java.sql.SQLException;
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

import bean.AddressBean;
import bean.BookBean;
import bean.cartItemBean;
import model.BOOKSTORE;

@WebServlet({ "/Test", "/Test/*" })
public class Test extends HttpServlet {
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
	
	public Test() {
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
		boolean isRedirected = false;
		
		if (logout != null) {
			isLogged = false;
		}
		
		String category = request.getParameter("category");
		ArrayList<BookBean> books = getBooksByCategory(category);
		request.setAttribute("books", books);
		request.setAttribute("category", category);
		
		// Get all items in the cart
		getCart(session);
		
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
		
		
		// Getting Shipping and Billing Address
		if (proceedToPayment != null) {
			String shippingFirstName = request.getParameter("shippingFirstName");
			String shippingLastName = request.getParameter("shippingLastName");
			String shippingAddress = request.getParameter("shippingAddress");
			String shippingAddress2 = request.getParameter("shippingAddress2");
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
			String billingAddress2 = request.getParameter("billingAddress2");
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
			request.getRequestDispatcher("/Cart.jspx").forward(request, response);
		} else if (confirmOrder != null) {
			request.getRequestDispatcher("/PaymentSuccessful.jspx").forward(request, response);
		} else if (proceedToPayment != null) { 
			request.getRequestDispatcher("/PaymentPage.jspx").forward(request, response);
		} else if (checkoutButton != null) {
			isRedirected = true;
			session.setAttribute("isRedirected", isRedirected);
			if (isLogged) {
				request.getRequestDispatcher("/Checkout.jspx").forward(request, response);
			} else {
				request.getRequestDispatcher("/Redirect.jspx").forward(request, response);
			}  
		} else if (paymentLogin != null) {
			request.getRequestDispatcher("/Login.jspx").forward(request, response);
		} else if (loginButton != null) {
			isLogged = true;
			isRedirected = (boolean) session.getAttribute("isRedirected");
			session.setAttribute("isLogged", isLogged); 
			if (isRedirected) {
				request.getRequestDispatcher("/Checkout.jspx").forward(request, response);
			} else {
				request.getRequestDispatcher("/MainPage.jspx").forward(request, response);
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
	
	public void getCart(HttpSession session) {

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
}
