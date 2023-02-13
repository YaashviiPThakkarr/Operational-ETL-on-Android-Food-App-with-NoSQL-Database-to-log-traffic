/**Name (andrewid): Pawanjeet Singh (pawanjes) and Yashvi Thakkar (ypt)
 * Email IDs: pawanjes@andrew.cmu.edu and ypt@andrew.cmu.edu
 **/
package com.example.project4task2;

//importing the relevant libraries for the code
import java.io.*;
import java.util.ArrayList;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.simple.JSONArray;

/**This is the SignificantDaysServlet class that accepts requests from our Android application and
 * then makes a GET request and receives data from the 3rd party API called Calendarific.
 * It then cleans the data retrieved from the 3rd party API and sends it back to our android application
 * This class also calls the Model class to make Mongo protocol requests to persistently
 * store logs data on MongoDB on Atlas. It also accepts HTTP request to display the logged data
 * to an online HTML dashboard.
 **/
@WebServlet(name = "SignifcantDaysServlet", urlPatterns = {"/getDetails", "/getDashboard"})
public class SignificantDaysServlet extends HttpServlet {

    /** the doGet method is used to get read the data received from the android application
     * it then sends the requests from the user to the 3rd party API
     * after retrieving the data it updates the MongoDB database
     * it also updates the dashboard accordingly
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //instantiating the SignificantDaysModel class as sdm
        SignificantDaysModel sdm = new SignificantDaysModel();

        //getting the header of the request and the user-agent
        String userAgent = request.getHeader("User-Agent");

        //setting the boolean mobile as true. It wll be set to false if someone tries to
        //query the API or database using desktop, laptop or phone browser
        //mobile will be true only when the user is making requests thru the Android application
        boolean mobile = true;
        //checking if user agent is not null and contains mozilla
        if ((userAgent != null) && (userAgent.toLowerCase().contains("mozilla"))) {
            mobile = false; //setting the boolean flag to false
        }

        //if the user makes request using the android application.
        if (mobile) {
            /**checking if the request contains "getDetails", then take the input parameters which are -
             * country, year and month; get the responses from the API and then send them back to the
             * Android Application
             */
            if (request.getServletPath().equalsIgnoreCase("/getDetails")) {
                //getting the requested country
                String country = request.getParameter("country");
                //getting the requested year
                String year = request.getParameter("year");
                //getting the requested month
                String month = request.getParameter("month");
               //flag will be true if the month input by the user is valid/correct.
                Boolean flag = sdm.checkInput(month);

                //if the flag is true, that is, the user input is valid
                if (flag) {
                    //get the url from the appropriate method in the SignificantDaysModel class
                    //this is the url that is used to connect to the 3rd party API
                    String url_link = sdm.extractHolidays(country, year, month);
                    //using that link to fetch the data from the 3rd party API
                    JSONArray responses = sdm.fetch(url_link); //extracted information which is a list of lists stored in a JSON Array
                    //setting the content type of the response object
                    response.setContentType("application/json");
                    //preparing to write out the responses received from the 3rd party API
                    PrintWriter out = response.getWriter();
                    //sending the responses back to the Android Application
                    out.println(responses);
                    //flushing the output writer
                    out.flush();

                    //getting the connection type from the request header since we want to store that as log information
                    String connection = request.getHeader("Connection");

                    //updating the MongoDB database with the relevant information by making the
                    //appropriate function call
                    sdm.updateDatabase(userAgent, country, year, month, connection, new org.json.JSONArray(responses));

                }
            }
        }
        //if the request URL has getDashboard, then make the appropriate calls to get the dashboard
        else if (request.getServletPath().equalsIgnoreCase("/getDashboard")) {

            //set the content type of response to text/html
            response.setContentType("text/html");
            //data is an arraylist of arraylists and it stores the documents retrieved from the
            //MongoDB database
            ArrayList<ArrayList> data = sdm.retrieveDocuments();

            //calling the getAnalytics function which returns an arraylist of maps
            ArrayList<Map<String, Integer>> logData = sdm.getAnalytics();

            //setting the request's attribute user agents with the 1st element of the arraylist data
            request.setAttribute("UserAgent", data.get(0));
            //setting the request's attribute connection type with the 2nd element of the arraylist data
            request.setAttribute("ConnectionType", data.get(1));
            //setting the request's attribute Country with the 3rd element of the arraylist data
            request.setAttribute("Country", data.get(2));
            //setting the request's attribute Year with the 4th element of the arraylist data
            request.setAttribute("Year", data.get(3));
            //setting the request's attribute Month with the 5th element of the arraylist data
            request.setAttribute("Month", data.get(4));
            //setting the request's attribute FestivalDays with the 6th element of the arraylist data
            request.setAttribute("FestivalDays", data.get(5));
            //setting the request's attribute Types with the 7th element of the arraylist data
            request.setAttribute("Types", data.get(6));

            //setting the request's attribute UniqueCountry with the 1st element of the logData
            request.setAttribute("UniqueCountry", logData.get(0));
            //setting the request's attribute UniqueYear with the 2nd element of the logData
            request.setAttribute("UniqueYear", logData.get(1));
            //setting the request's attribute UniqueMonth with the 3rd element of the logData
            request.setAttribute("UniqueMonth", logData.get(2));
            //setting the request's attribute UniqueDays with the 4th element of the logData
            request.setAttribute("UniqueDays", logData.get(3));
            //setting the request's attribute FrequentDays with the 5th element of the logData
            request.setAttribute("FrequentDays", logData.get(4));
            //setting the request's attribute UniqueTypes with the 6th element of the logData
            request.setAttribute("UniqueTypes", logData.get(5));
            //setting the request's attribute UniqueUserAgent with the 7th element of the logData
            request.setAttribute("UniqueUserAgent", logData.get(6));

            //dispatchng the view to the festBuddyDashboard.jsp, which holds the front-end view
            //of the log and analytics dashboard we display to the user
            RequestDispatcher view = request.getRequestDispatcher("festBuddyDashboard.jsp");
            //forwarding the request and  response.
            view.forward(request, response);
        }
    }
}