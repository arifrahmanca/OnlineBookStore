package ctrl;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BOOKSTORE;

@WebServlet({"/Bookstore", "/Bookstore/*"})
public class Bookstore extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletContext context;
	private BOOKSTORE model;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Bookstore() {
        super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		request.getRequestDispatcher("/MainPage.jspx").forward(request, response);
	}	 

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
