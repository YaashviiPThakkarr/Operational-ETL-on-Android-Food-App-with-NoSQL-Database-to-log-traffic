//Name: Pawanjeet Singh, Yashvi Thakkar
//Andrew ID: pawanjes, ypt
//Email ID: pawanjes@andrew.cmu.edu, ypt@andrew.cmu.edu

package edu.cmu.project4;

//Importing the necessary  packages
import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * GetFestHandler class is a public class that handles the requests made by the user on the app. This class has methods that
 * connects with the servlet on heroku and sends the user's requests. This method also validates user input and switches
 * to a background thread where main work of sending the user request and getting the response is done.
 *
 */
public class GetFestsHandler {
    FestBuddy fb=null;  //Creating an object of FestBuddy and instantiating it to null
    JSONArray names = new JSONArray(); //Creating a JSON array to store names of the special occasions
    JSONArray descriptions= new JSONArray(); //Creating a JSON array to store descriptions of the special occasions
    JSONArray types= new JSONArray(); //Creating a JSON array to store types of the special occasions
    String errorMessage= ""; //Declaring a string for error message
    String country; //Declaring a string for country message
    String year; //Declaring a string for year message
    String month; //Declaring a string for month message

    /**
     * This method instantiates the needed global variables according to the user request and starts a new background thread where the main
     * processing of the sending request and getting the data is done.
     * @param country
     * @param year
     * @param month
     * @param activity
     * @param fb
     */

    public void getSignificantDays(String country, String year, String month, Activity activity,FestBuddy fb){
        this.fb=fb;   //Instantiating the fest buddy object
        this.country=country; //Instantiating the country string
        this.year=year; //Instantiating the year string
        this.month=month; //Instantiating the month string
        new BackgroundTask(activity).execute(); //C
    }

    /**
     *  Class BackgroundTask
     *  Implements a background thread for a long running task like connecting with the servlet and waiting until it responds, that should not be performed on the UI thread.
     *  It creates a new Thread object, then calls doInBackground() to actually do the work. When done, it calls onPostExecute(), which runs
     *  on the UI thread to update some UI widget with the responses received from the server.
     */
    private class BackgroundTask {

        private Activity activity; //The UI Thread

        public BackgroundTask(Activity activity) {
            this.activity = activity; //Instantiating our global activity variable with the parameter passed
        }

        /**
         * This method starts the background thread that does the main connecting and data getting work. This background thread will ensure that
         * the UI is responsive.
         */
        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        doInBackground(); //Calls the background method to connect which will handle the connection and call other methods accordingly
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException | ParseException e) { //Catching any input/output or parsing exceptions
                        e.printStackTrace();
                    }
                    // This is magic: activity should be set to MainActivity.this
                    //    then this method uses the UI thread
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                onPostExecute(names, descriptions, types, errorMessage); //Calling the on post execute method to display the response to the user
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
        }

        /**
         * A background thread will call this method to start the thread.
         */
        private void execute(){
            // There could be more setup here, which is why
            //    startBackground is not called directly
            startBackground();
        }

        /**
         * This method calls the search method which deals with connecting with the web service/servlet on heroku.
         * @throws JSONException
         * @throws IOException
         * @throws ParseException
         */

        private void doInBackground() throws JSONException, IOException, ParseException {
            JSONArray details = search(country, year, month); //Calling the search method.

            //Checking if details json array has some data as response from the web service
            if (details.length()==4){
                names = details.getJSONArray(0); //Updating values of names JSON array
                descriptions = details.getJSONArray(1); //Updating values of description JSON array
                types = details.getJSONArray(2); //Updating values of types JSON array
                errorMessage= (String)details.get(3); //Updating error message string
            }
        }

        /**
         * This method calls the UI thread method to display the response to the users.
         * @param names - JSON array of names of the special occasion
         * @param descriptions - JSON array of description of the special occasion
         * @param types - JSON array of types of special occasion
         * @param errorMessage - String errorMessage
         * @throws JSONException
         */
        public void onPostExecute(JSONArray names, JSONArray descriptions, JSONArray types,
                                  String errorMessage) throws JSONException {
            fb.detailsReady(names,descriptions,types,errorMessage); //Calling the details ready method to display response on the UI.
        }

        /**
         * This method connects with the web service on Heroku to and send the user's request. It gets the response and sends it
         * back to the background thread method.
         * @param country - String, country selected by the user
         * @param year - String, year selected by the user
         * @param month - String, month typed in by the user
         * @return JSONArray - Response from the webservice
         * @throws JSONException
         * @throws IOException
         */
        private JSONArray search(String country, String year, String month) throws JSONException, IOException {

            Boolean flag = checkInput(month); //Checking whether the user input is correct or not
            String herokuURL = "https://gentle-gorge-81012.herokuapp.com/"; //Web service link that hosts the servlet

            if(flag){ //if the input is validated and true
                URL url = new URL(herokuURL+"getDetails?country="+country+"&year="+year+"&month="+month); //Send a get request to the web service

                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection(); //Opening the connection
                httpURLConnection.setRequestMethod("GET"); //Setting the request method as GET
                httpURLConnection.connect(); //Connecting with the webservice

                String servletResponse=""; //String for the servlet response
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8")); //Creating a buffered reader for to get
                // the response from the web service
                String str;
                while ((str = in.readLine()) != null) { //While the string is not null
                    // str is one line of text readLine() strips newline characters
                    servletResponse += str; // Concatenating the webservice response to the servlet response string
                }
                in.close(); //Closing the buffered reader

                JSONArray jsonArray=new JSONArray(servletResponse); //Creating a JSON array of the response
                return jsonArray; //Returning the response
            }
            else{
                errorMessage = "Incorrect Input by User. Please check your input for months."; //Setting appropriate error message
                JSONArray jsonArray = new JSONArray(); //Creating a new JSON array to send an empty response as user input is invalid
                return  jsonArray; //Returning the response
            }

        }

        /**
         * This method checks and validates the user input of the user for the month field. Month should be between 1-12.
         * Boolean value is returned accordingly.
         * @param month
         * @return Boolean flag
         */
        public Boolean checkInput(String month){
            Boolean flag = true; // Setting the flag to true initially

            //If only alphabetical character were entered for month
            if(month.matches("[a-zA-Z]+")){
                flag=false;
            }
            //If a special character was entered for month
            else if(month.matches("[^A-Za-z0-9]+"))
            {
                flag=false;
            }
            //If alphanumeric string was entered for month
            else if((month.matches(".*[0-9].*")) && (month.matches(".*[A-Za-z].*"))){
                flag=false;
            }
            //If numbers are entered for month
            else if(month.matches("[0-9]+")){
                if((Integer.parseInt(month)>12) || (Integer.parseInt(month)<1)){ //Checking the range of the month
                    flag=false;
                }
            }
            return flag; //Returning the flag
        }
    }
}
