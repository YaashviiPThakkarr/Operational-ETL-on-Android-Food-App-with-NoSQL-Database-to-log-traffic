//Name: Pawanjeet Singh, Yashvi Thakkar
//Andrew ID: pawanjes, ypt
//Email ID: pawanjes@andrew.cmu.edu, ypt@andrew.cmu.edu

package edu.cmu.project4;

//Importing the necessary packages
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class gets user inputs from the UI components and also updates the other UI component with the responses from the web service.
 */
public class FestBuddy extends AppCompatActivity {

    FestBuddy me = this; //Setting the fest buddy object to this
    private TableLayout mTableLayout; //Declaring the table layout for the UI
    private TextView errorMessageView; //Declaring a text view for the UI

    /**
     * This method sets the initial widgets that are displayed on the application when the user runs the app. This method also
     * populates the two spinner values with the range of countries and year values.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main); //Setting the app view to activity main

        Spinner spinCountries = findViewById(R.id.countrySpinner); //Finding the spinner by it's ID
        //Creating an array adapter to add country values which are described in the strings.xml file
        ArrayAdapter<CharSequence> adapterCountry = ArrayAdapter.createFromResource(this,R.array.countries, android.R.layout.simple_spinner_item);
        //Creating the drop down view for the spinner
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_item);
        //Populating the spinner values with country strings
        spinCountries.setAdapter(adapterCountry);

        Spinner spinYears = findViewById(R.id.yearSpinner); //Finding the spinner by it's ID
        //Creating an array adapter to add year values which are described in the strings.xml file
        ArrayAdapter<CharSequence> adapterYears = ArrayAdapter.createFromResource(this,R.array.years, android.R.layout.simple_spinner_item);
        //Creating the drop down view for the spinner
        adapterYears.setDropDownViewResource(android.R.layout.simple_spinner_item);
        //Populating the spinner values with year strings
        spinYears.setAdapter(adapterYears);

        //Find the submit button and attaching setOnClickListener to it so the needed methods can be called.
        Button submitButton = (Button)findViewById(R.id.submit);

        final FestBuddy ma = this;

        // Adding a listener to the submit button
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewParam) {
                //On clicking the submit button, the user inputs will be stored in the corresponding strings
                String country = ((Spinner)findViewById(R.id.countrySpinner)).getSelectedItem().toString(); //Getting the selected country name from the spinner
                String year = ((Spinner)findViewById(R.id.yearSpinner)).getSelectedItem().toString(); //Getting the selected year name from the spinner
                String month = ((EditText)findViewById(R.id.searchMonth)).getText().toString(); //Getting the typed month value from the edittext view
                GetFestsHandler gfh = new GetFestsHandler(); //Creating a new object of the GetFestHandler class that will handle the request to the webservice and fetch the response.
                gfh.getSignificantDays(country, year, month, me, ma); // All the processing is done asynchronously on a different thread
            }
        });
    }

    /**
     * This method sets the table values with special occasion names, descriptions and types or the error message in a text view.
     * This method handles displaying the response to the user on the app.
     * @param names - JSON array of names returned from the web service
     * @param descriptions - JSON array of descriptions returned from the web services
     * @param types - JSON array of the types of special occasion returned from the web services
     * @param errorMessage - String, relevant error message is returned if needed
     * @throws JSONException
     */
    public void detailsReady(JSONArray names, JSONArray descriptions, JSONArray types,
                             String errorMessage) throws JSONException {

        errorMessageView = (TextView) findViewById(R.id.errorMessage); //Find the text view to display the error message and attaches it to a variable
        mTableLayout = (TableLayout) findViewById(R.id.tableSignificantDays); //Find the table layout to display the data and attach it to a variable

        //Check if the error message isn't null then an error occurred
        if(errorMessage.equalsIgnoreCase("Incorrect Input by User. Please check your input for months.") || (names.length()==0)){
            errorMessageView.setText(""); //Setting null value to the clean the previous error message
            errorMessageView.setText(errorMessage); //Setting the updated error message
            errorMessageView.setVisibility(View.VISIBLE); //Setting the visibility of the textview to true
            mTableLayout.setVisibility(View.INVISIBLE); //Setting the visibility of the table layout to false
        }
        else {
            //The code below has been referenced from: www.tutorialspoint.com
            // The link is: https://www.tutorialspoint.com/how-to-add-table-rows-dynamically-in-android-layout
            int leftRowMargin = 0; //Initializing the left margin
            int topRowMargin = 0; //Initializing the top margin
            int rightRowMargin = 0; //Initializing the right margin
            int bottomRowMargin = 0; //Initializing the bottom margin

            errorMessageView.setVisibility(View.INVISIBLE); //Setting the visibility of the error message textview to false
            mTableLayout.setStretchAllColumns(true); //Stretching all columns as needed

            int length = names.length(); //Fetching the length of number of names returned from the web service

            mTableLayout.removeAllViews(); //Removing the current views

            //Running a for loop populate the table
            for (int i = -1; i < length; i++) {
                //Each textview is the table column, this one is the Index of the table, attaching a text view with it
                final TextView tv = new TextView(this);
                tv.setLayoutParams(new
                        TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT)); //Setting it's layout parameters
                tv.setGravity(Gravity.LEFT); //Setting the position of the text
                tv.setPadding(5, 15, 0, 15); //Setting the padding values
                if (i == -1) { //If the row is the header row
                    tv.setText("Index"); //Populating the column header
                    tv.setBackgroundColor(Color.parseColor("#f0f0f0")); //Setting the background color

                } else {
                    tv.setBackgroundColor(Color.parseColor("#f8f8f8")); //Setting the background color
                    tv.setText(String.valueOf(i+1)); //Setting the index if it is not the column header

                }

                //The second column takes care of names of the significant days, attaching a text view with it
                final TextView tv2 = new TextView(this);
                if (i == -1) { //If the row is the header row
                    tv2.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT)); //Setting it's layout parameters
                } else {
                    tv2.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.MATCH_PARENT)); //Setting it's layout parameters

                }
                tv2.setGravity(Gravity.LEFT); //Setting the position of the text
                tv2.setPadding(5, 15, 0, 15); //Setting the padding values
                if (i == -1) { //If the row is the header row
                    tv2.setText("Name"); //Populating the column header
                    tv2.setBackgroundColor(Color.parseColor("#f7f7f7")); //Setting the background color
                } else {
                    tv2.setBackgroundColor(Color.parseColor("#ffffff")); //Setting the background color
                    tv2.setTextColor(Color.parseColor("#000000")); //Setting the text color
                    tv2.setText(names.getString(i)); //Setting the names of the significant day if it is not the column header
                }

                //the 3rd column shows the description of the significant day, attaching a text view with it
                final TextView tv3 = new TextView(this);
                if (i == -1) { //If the row is the header row
                    tv3.setLayoutParams(new
                            TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT)); //Setting it's layout parameters
                } else {
                    tv3.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT
                    )); //Setting it's layout parameters

                }
                tv3.setGravity(Gravity.LEFT); //Setting the position of the text
                tv3.setPadding(5, 15, 0, 15); //Setting the padding values
                if (i == -1) { //If the row is the header row
                    tv3.setText("About"); //Populating the column header
                    tv3.setBackgroundColor(Color.parseColor("#f7f7f7")); //Setting the background color
                } else {
                    tv3.setBackgroundColor(Color.parseColor("#ffffff")); //Setting the background color
                    tv3.setTextColor(Color.parseColor("#000000")); //Setting the font color
                    tv3.setText(descriptions.getString(i)); //Setting the description of the significant day if it is not the column header
                }

                //the 4th column shows the type of the significant day, attaching a text view with it
                final TextView tv4 = new TextView(this);
                if (i == -1) { //If the row is the header row
                    tv4.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT)); //Setting it's layout parameters
                } else {
                    tv4.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.MATCH_PARENT)); //Setting it's layout parameters
                }
                tv4.setGravity(Gravity.LEFT); //Setting the position of the text
                tv4.setPadding(5, 15, 0, 15); //Setting the padding values
                if (i == -1) { //If the row is the header row
                    tv4.setText("Type"); //Populating the column header
                    tv4.setBackgroundColor(Color.parseColor("#f7f7f7")); //Setting the background color
                } else {
                    tv4.setBackgroundColor(Color.parseColor("#ffffff")); //Setting the background color
                    tv4.setTextColor(Color.parseColor("#000000")); //Setting the background color
                    //Replacing the special characters with null
                    tv4.setText(types.getString(i).replaceAll("\\[","").replaceAll("\\]","").replace("\""," ").replace("\\",""));
                }

                // Adding a table row to the table layout
                final TableRow tr = new TableRow(this);
                tr.setId(i + 1); //Setting it's ID
                TableLayout.LayoutParams trParams = new
                        TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT); //Setting it's layout parameters
                trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin,
                        bottomRowMargin); //Setting the margins
                tr.setPadding(0, 0, 0, 0); //Setting the padding
                tr.setLayoutParams(trParams); //Setting it's layout parameters

                //Adding populated columns to the table
                tr.addView(tv); //Adding the index column
                tr.addView(tv2); //Adding the names column
                tr.addView(tv3); //Adding the description column
                tr.addView(tv4); //Adding the types column
                mTableLayout.addView(tr, trParams); //Adding the layout parameters to the populated table
            }

            mTableLayout.setVisibility(View.VISIBLE); //Setting the table visibility to true
        }
    }





}