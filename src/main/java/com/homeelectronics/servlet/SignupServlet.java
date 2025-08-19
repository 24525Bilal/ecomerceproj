import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


import com.homeelectronics.dao.UserDAO;
import com.homeelectronics.model.User;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        User user = new User(email, password);
        UserDAO userDAO = new UserDAO();

        boolean success = userDAO.saveUser(user);

        if (success) {
            // Redirect to the sign-in page on successful signup
            response.sendRedirect("account-signin.html");
        } else {
            // Show a popup message if signup failed
            response.setContentType("text/html");
            response.getWriter().println("<script type='text/javascript'>");
            response.getWriter().println("alert('❌ Signup failed. Email allready registered.');");
            response.getWriter().println("window.location.href = 'account-signup.html';"); // Redirect back to the signup page
            response.getWriter().println("</script>");
        }
    }
}