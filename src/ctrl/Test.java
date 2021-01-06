package ctrl;

import java.io.IOException;
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

import bean.BookBean;
import bean.cartItemBean;
import model.BOOKSTORE;

@WebServlet({ "/Test", "/Test/*" })
public class Test extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletContext context;
	private BOOKSTORE model;
	
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
		
		String category = request.getParameter("category");

		Map<String, BookBean> bookInfo = new HashMap<>();
		ArrayList<BookBean> books = new ArrayList<BookBean>();

		// access model and get all the books in record 
		if (category != null) {
			request.setAttribute("category", category);
			// access model and get book with specific category
			try {
				bookInfo = model.retriveBooksByCategory(category);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			request.setAttribute("category", "View All");
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

		request.setAttribute("books", books);
		request.getRequestDispatcher("/MainPage.jspx").forward(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	private void addToCart(BookBean book, HttpSession session) {
		if (session.getAttribute("items") == null && book != null) {// if items list is not made make one and add book
																	// to cart
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
