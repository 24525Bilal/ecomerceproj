package com.homeelectronics.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

// because of the server is not respoinding to the out side path folder , this servlet do thatSS
//  ImageServlet is responsible for serving product images stored on disk.

 //Example: if DB stores "product-images/2/boat.jpg"
 //         and browser requests "http://localhost:8080/product-images/2/boat.jpg"

 // This servlet will fetch the actual file from "D:/product_image/2/boat.jpg"
 //and stream it back to the client.

@WebServlet("/product-images/*")  // Any request starting with /product-images will be handled here
public class ImageServlet extends HttpServlet {

    // The base directory on disk where images are stored
    private static final String BASE_DIR = "D:/product_image";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Extracts the part of the URL after "/product-images"
        // Example: for "/product-images/2/boat.jpg" → requestedPath = "/2/boat.jpg"
        String requestedPath = request.getPathInfo();

        if (requestedPath == null || requestedPath.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image path required.");
            return;
        }

        // Remove leading slash if present (to avoid absolute path issues)
        if (requestedPath.startsWith("/")) {
            requestedPath = requestedPath.substring(1);
        }

        // Build the absolute file path correctly
        File imageFile = new File(BASE_DIR, requestedPath);

        // for checking the image is laoding or not
       // System.out.println("Requested path = " + requestedPath);
      //  System.out.println("Resolved file = " + imageFile.getAbsolutePath());

        if (!imageFile.exists() || imageFile.isDirectory()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found.");
            return;
        }

        // Detect the MIME type (jpg, png, etc.)
        String mimeType = getServletContext().getMimeType(imageFile.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream"; // fallback
        }
        response.setContentType(mimeType);
        response.setContentLengthLong(imageFile.length());

        // Stream the file to client
        try (FileInputStream in = new FileInputStream(imageFile);
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
