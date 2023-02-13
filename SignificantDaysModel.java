/**Name (andrewid): Pawanjeet Singh (pawanjes) and Yashvi Thakkar (ypt)
 * Email IDs: pawanjes@andrew.cmu.edu and ypt@andrew.cmu.edu
 **/

package com.example.project4task2;

//calling the import statements
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Month;
import java.util.*;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

/**
 * This class SignificantDaysModel actually makes the API calls to the 3rd party, checks
 * if the user inputs are valid and correct, connects to the
 * MongoDB database on Atlas, updates it with the new user requests, makes calls to the mongodb database
 * to retrieve the documents to display them on the dashboard, and finally also conducts some analytics
 * on the logged data.
 */
public class SignificantDaysModel {

    //this arraylist stores the list of documents which will be retrieved from the MongoDB database
    ArrayList<ArrayList> documents = new ArrayList<>();
    //this Arraylist of Strings store the user Agent from whom the request is coming
    ArrayList<String> userAgents = new ArrayList<>();
    //This arraylist of strings store the connection type of the request made
    ArrayList<String> connectionType = new ArrayList<>();
    //this arraylist of strings stores the name of the country which the user requested
    ArrayList<String> country = new ArrayList<>();
    //this arraylist of strings stores the year requested for by the user
    ArrayList<String> year = new ArrayList<>();
    //this arraylist of strings stores the month requested for by the user
    ArrayList<String> month = new ArrayList<>();
    //this arraylist of strings stores the name of all the festival/significant days in a particular country for a
    //particular month and year requested by the user.
    ArrayList<String> festivalDays = new ArrayList<>();
    //this arraylist of strings stores the type of all the festival/significant days in a particular country for a
    //particular month and year requested by the user.
    ArrayList<String> types = new ArrayList<>();

    //this is the link to connect to the appropriate database collection in the MongoDB cluster
    String mongoDB = "mongodb://pawanjes:pawanjes2022@ac-v0fevae-shard-00-00.eg0o3xs.mongodb.net:27017,ac-v0fevae-shard-00-01.eg0o3xs.mongodb.net:27017,ac-v0fevae-shard-00-02.eg0o3xs.mongodb.net:27017/FestBuddyDashboard?w=majority&retryWrites=true&tls=true&authMechanism=SCRAM-SHA-1";

    //this connection string will be assigned the mongoDB link defined above
    ConnectionString connectionString;

    /**fetch method used in the API calls for extracting the relevant info
     *It uses JSONObjects to store data and JSONParsers to navigate through them
     * It accepts as input the URL that will be required to connect to the 3rd party API
    **/
    public static JSONArray fetch(String urlString) {

        //This string will store all the data fetched from the API. Initialized to empty string for now
        String response = "";
        //jsonResponse is a JSONArray object which will store the names, descriptions, types and errorMessage
        //retrieved from the 3rd party API.
        JSONArray jsonResponse = new JSONArray();

        try {
            URL url = new URL(urlString);
            /**
             * Create an HttpURLConnection.  This is useful for setting headers
             * and for getting the path of the resource that is returned (which
             * may be different than the URL above if redirected).
             * HttpsURLConnection (with an "s") can be used if required by the site.
             */
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //setting up the GET request
            connection.setRequestMethod("GET");
            //establishing the connection
            connection.connect();

            //responseCode holds the response code of the connection like 200, 404 etc.
            int responseCode=connection.getResponseCode();

            //names arraylist stores the names of all the significant days obtained from the search results
            ArrayList<String> names=new ArrayList<String>();
            //descrpitions arraylist stores the description of each corresponding significant day returned
            //from the API
            ArrayList<String> descriptions=new ArrayList<String>();
            //types arraylist stores the type of each corresponding significant day returned
            //from the API
            ArrayList<String> types=new ArrayList<String>();

            //initializing the errorMessage to "OK" for now. It will later change to an appropriate
            //error message based on the response code received.
            String errorMessage="OK";

            //if the response code is NOT 200
            if(responseCode!=200){

                //set the appropriate error message
                errorMessage="Service Down. Server Error.";
                //throw new RuntimeException("HttpResponseCode: " + responseCode);
            }
            //otherwise, proceed in the following way
            else{
                //This bufferedreader object will read the data coming from the connection
                //established with the 3rd party API
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                //temp string object to read each line
                String str;
                //while the next line is not null
                while ((str = in.readLine()) != null) {
                    // str is one line of text readLine() strips newline characters
                    //attaching every line to the response
                    response += str;
                }
                //closing the connection
                in.close();

                //initializing a parser to parse thru the String objects and convering to JSON objects
                JSONParser parse =new JSONParser();
                //parsing through the response String and converting to JSON object
                JSONObject jsonObject=(JSONObject) parse.parse(response);

                //getting the actual response data (relevant to us) from the entire JSON
                //object retrieved from the connection
                JSONObject responseObject=(JSONObject) jsonObject.get("response");

                //storing the holidays information in the holidayArray object.
                //this array holds the information about the name of the day, description, type etc.
                JSONArray holidayArray=(JSONArray) responseObject.get("holidays");

                //looping through the entire holidayArray to get names, descr, types etc.
                for(int i=0;i<holidayArray.toArray().length;i++){

                    //getting the holiday JSON object
                    JSONObject holidayObj= (JSONObject) holidayArray.get(i);

                    //accessing the name of the significant days and
                    //adding them to the "names" array.
                    names.add(holidayObj.get("name").toString());

                    //accessing the description of the significant days and
                    //adding them to the "descriptions" array.
                    descriptions.add(holidayObj.get("description").toString());
                    //accessing the type of the significant days and
                    //adding them to the "types" array.
                    types.add(holidayObj.get("type").toString());
                }

            }

            //if no names were returned after the search completed
            if(names.size()==0)
            {
                //displaying the appropriate message
                errorMessage="No special days found in this month. Please try different parameters";
            }

            //otherwise, add all the arraylists to the jsonResponse object which is a JSON Array
            jsonResponse.add(0,names); //the 1st index holds all the names
            jsonResponse.add(1,descriptions);//the 2nd index holds all the respective descriptions
            jsonResponse.add(2,types);//the 3rd index holds the respective types
            jsonResponse.add(3,errorMessage);//the 4th index holds the error message if any

        } catch (IOException e) {
            // Do something reasonable.  This is left for students to do.
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        //returning the JSON array
        return jsonResponse;
    }

    //this method takes as input the user requests like country, year and month
    //it then forms and returns the appropriate URL that will be used to connect to the 3rd party API
    public static String extractHolidays(String country, String year_string, String month_string) throws IOException {

        //this map will hold the country name and country code pairs.
        //the user input country names like "Australia" will be mapped to the API appropriate code like "AU".
        HashMap<String,String> countryMap=new HashMap<String,String>(); //using the HashMap to store the

        //putting the following 7 countries to the map with the corresponding code
        countryMap.put("UK","UK");
        countryMap.put("USA","US");
        countryMap.put("Australia","AU");
        countryMap.put("India","IN");
        countryMap.put("Canada","CA");
        countryMap.put("Argentina","AR");
        countryMap.put("Germany","DE");

        //storing the API-appropriate country code in country_code
        String country_code = countryMap.get(country);
        //concatenating the link for extracting the holidays info, with the country, year, month and API authorization key
        String url_link="https://calendarific.com/api/v2/holidays?&api_key=20912e8c78839c50b16a2c6701123930b963021b&country=" + country_code + "&year=" + year_string+"&month="+month_string;

        //returning this url_link
        return url_link;
    }

    /**this method verifies whether the user request is correct or riddled with invalid inputs
     * this method takes as input the month entered by the user in the free text
     * and returns a boolean true or false
     * @param month
     * @return
     */
    public Boolean checkInput(String month){
        //setting the flag to true
        Boolean flag = true;

        //if only alphabetical character were entered for month
        if(month.matches("[a-zA-Z]+")){
            flag=false;
        }
        //if a special character was entered for month
        else if(month.matches("[^A-Za-z0-9]+"))
        {
            flag=false;
        }
        //if alphanumeric string was entered for month
        else if((month.matches(".*[0-9].*")) && (month.matches(".*[A-Za-z].*"))){
            flag=false;
        }

        //if user entered a numerical digit
        else if(month.matches("[0-9]+")){
            //if number entered is more than 12 or less than 1
            if((Integer.parseInt(month)>12) || (Integer.parseInt(month)<1)){
                flag=false;
            }
        }

        //returning the flag. It will be true if input was correct, otherwise false
        return flag;
    }

    /**
     * this method updates the MongoDB database if the user has entered inputs correctly and
     * if the API returned valid response. It takes as input the user agent, country, year, month, connection,
     * and the JSOnArray containing responses from the API
     * @param userAgent
     * @param country
     * @param year
     * @param month
     * @param connection
     * @param responses
     */
    public void updateDatabase(String userAgent, String country, String year, String month, String connection, org.json.JSONArray responses) {
        //storing the connection URL to MongoDB database
        connectionString = new ConnectionString(mongoDB);

        //establishing connection to MongoDB
        //the below code has been referred to from the following link:
        //https://mongodb.github.io/mongo-java-driver/3.7/driver/tutorials/connect-to-mongodb/
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
                .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build();
        MongoClient mongoClient = MongoClients.create(settings);

        //specifying the database name in the mongoDB cluster
        MongoDatabase database = mongoClient.getDatabase("FestBuddyDashboard");
        //specifying the collection name which we want from the database cluster
        MongoCollection<Document> festBuddyLogs = database.getCollection("FestBuddyLogs");


        //creating a new Document which will store all the information we will write to the mongoDB database

        //the first datapoint written will be userAgent
        Document doc = new Document("UserAgent", userAgent);
        //followed by connection type
        doc.append("ConnectionType",connection);
        //followed by country name
        doc.append("Country", country);
        //followed by year
        doc.append("Year", year);
        //followed by month
        doc.append("Month", month);
        //followed by all the festival days which was stored in the JSON array
        doc.append("FestivalDays", responses.getJSONArray(0));
        //followed by all the types of corresponding holidays which were also stored in the JSONArray
        doc.append("Types", responses.getJSONArray(2));

        //inserting the document to the festBuddyLogs collection
        festBuddyLogs.insertOne(doc);
    }

    /**
     * this method retrieves the existing documents in the mongoDB database
     * which are returned as an arraylist of arraylists.
     * @return
     */
    public ArrayList<ArrayList> retrieveDocuments(){
        //storing the connection URL to MongoDB database
        connectionString = new ConnectionString(mongoDB);

        //establishing connection to MongoDB
        //the below code has been referred to from the following link:
        //https://mongodb.github.io/mongo-java-driver/3.7/driver/tutorials/connect-to-mongodb/
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
                .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build();
        MongoClient mongoClient = MongoClients.create(settings);

        //specifying the database name in the mongoDB cluster
        MongoDatabase database = mongoClient.getDatabase("FestBuddyDashboard");

        //specifying the collection name which we want from the database cluster
        MongoCollection<Document> festBuddyLogs = database.getCollection("FestBuddyLogs");

        //creating a BSON object that will be used to iterative over the documents in the collection
        Bson documentStrings = Projections.fields(Projections.excludeId());
        //creating a cursor to loop over the documents in the collection
        MongoCursor<Document> cursor = festBuddyLogs.find().projection(documentStrings).iterator();

        //while there is a next document existing in the collection
        while(cursor.hasNext()){
            //storing the collection's document in a JSONObject
            org.json.JSONObject doc = new org.json.JSONObject(cursor.next());

            //storing the useragents retrieved from the document in the global variable
            userAgents.add(doc.get("UserAgent").toString());
            //storing the connection type retrieved from the document in the global variable
            connectionType.add(doc.get("ConnectionType").toString());
            //storing the country retrieved from the document in the global variable
            country.add(doc.get("Country").toString());
            //storing the year retrieved from the document in the global variable
            year.add(doc.get("Year").toString());
            //storing the month retrieved from the document in the global variable
            month.add(doc.get("Month").toString());
            //storing the festivalDays retrieved from the document in the global variable
            festivalDays.add(doc.get("FestivalDays").toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("\""," "));
            //storing the types retrieved from the document in the global variable
            types.add(doc.get("Types").toString().replaceAll("\\[","").replaceAll("\\]","").replace("\""," ").replace("\\",""));
        }
        //closing the cursor
        cursor.close();

        //adding the respective arraylists to the documents arraylists which will be returned.
        documents.add(userAgents); //adding the useragents
        documents.add(connectionType);//adding the connection type
        documents.add(country);//adding the country
        documents.add(year);//adding the year
        documents.add(month);//adding the month
        documents.add(festivalDays);//adding the festivalDays
        documents.add(types);//adding the types of festivaldays

        //returning the list of lists
        return documents;
    }

    /**
     * This method computes analytics on the data retrieved from the dashboard
     * it returns an arraylist, each element of which is a map.
     * @return
     */
    public ArrayList<Map<String, Integer>> getAnalytics() {

        //initializing the arraylist which will be returned in the end
        ArrayList<Map<String,Integer>> logAnalysis = new ArrayList<>();

        //initializing the hashmap that will hold the unique country name and respective counts
        //the format will be, for example -
        //Australia : 10
        //USA : 5
        HashMap<String, Integer> countryUnique = new HashMap();
        //looping through the country arraylist
        for (int i = 0; i < country.size(); i++) {
            //storing the country name
            String s = country.get(i);
            //checking if country already exists in the map
            if (countryUnique.containsKey(s.trim())) {
                //if yes then add 1 to its count
                countryUnique.put(s.trim(), countryUnique.get(s.trim()) + 1);
            } else {
                //otherwise assign its occurence count to 1
                countryUnique.put(s.trim(), 1);
            }
        }

        //the below code sorts the map in a descending order based on the "Value" of the map.
        // "Values" the frequency of each country
        // The below code has been referenced from www.javacodegeeks.com.
        //The link is: https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
        Map<String, Integer> sortedCountry = countryUnique.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)
                );

        //initializing the hashmap that will hold the unique year and respective counts
        //the format will be, for example -
        //2020 : 10
        //2017: 5
        HashMap<String, Integer> yearUnique = new HashMap();
        //looping through the year arraylist
        for (int i = 0; i < year.size(); i++) {
            //storing the year name
            String s = year.get(i);
            //checking if year already exists in the map
            if (yearUnique.containsKey(s.trim())) {
                //if yes then add 1 to its count
                yearUnique.put(s.trim(), yearUnique.get(s.trim()) + 1);
            } else {
                //otherwise assign its occurence count to 1
                yearUnique.put(s.trim(), 1);
            }
        }

        //the below code sorts the map in descending order based on the "Value" of the map.
        // The below code has been referenced from www.javacodegeeks.com.
        //The link is: https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
        Map<String, Integer> sortedYear = yearUnique.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)
                );

        //initializing the hashmap that will hold the unique type and respective counts
        //the format will be, for example -
        //Observance : 10
        //Public Holiday: 5
        HashMap<String, Integer> typeUnique = new HashMap();
        //looping through the types arraylist
        for (int i = 0; i < types.size(); i++) {
            //flattening the types arraylist for holidays that are of multiple types
            //storing the subtypes in a string array
            String[] subtypes = types.get(i).split(",");
            //looping through the subtypes list
            for (String s : subtypes) {
                //checking if type already exists in the map
                if (typeUnique.containsKey(s.trim())) {
                    //if it already exists, add 1 to its current count
                    typeUnique.put(s.trim(), typeUnique.get(s.trim()) + 1);
                } else {
                    //otherwise assign 1 to its frequency
                    typeUnique.put(s.trim(), 1);
                }
            }
        }
        //the below code sorts the HashMap in a descending order based on the "value" of the map.
        // The below code has been referenced from www.javacodegeeks.com.
        //The link is: https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
        Map<String, Integer> sortedTypes = typeUnique.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)
                );

        //initializing the hashmap that will hold the unique userAgent and respective counts
        //the format will be, for example -
        //Android : 10
        //Mozilla: 5
        HashMap<String, Integer> userAgentUnique = new HashMap();
        //looping through the year arraylist
        for (int i = 0; i < userAgents.size(); i++) {
            //storing the userAgent name
            String s = userAgents.get(i);
            //checking if useragent already exists in the map
            if (userAgentUnique.containsKey(s.trim())) {
                //if yes, add 1 to its count
                userAgentUnique.put(s.trim(), userAgentUnique.get(s.trim()) + 1);
            } else {
                //otherwise, assign 1 to its count
                userAgentUnique.put(s.trim(), 1);
            }
        }

        //the below code sorts the HashMap in a descending order based on the "value" of the map.
        // The below code has been referenced from www.javacodegeeks.com.
        //The link is: https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
        Map<String, Integer> sortedAgent = userAgentUnique.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)
                );

        //initializing the hashmap that will hold the unique festival days and respective counts
        //the format will be, for example -
        //New Years : 10
        //Independence Day: 5
        HashMap<String, Integer> festivalDaysUnique = new HashMap();
        //looping through the year arraylist
        for (int i = 0; i < festivalDays.size(); i++) {
            //flattening the names arraylist since there are multiple holidays each month
            //storing the days in a string array
            String[] subtypes = festivalDays.get(i).split(",");
            //looping through the sub days list
            for (String s : subtypes) {
                //checking if day already exists in the map as a key
                if (festivalDaysUnique.containsKey(s.trim())) {
                    //if yes, add 1 to its count
                    festivalDaysUnique.put(s.trim(), festivalDaysUnique.get(s.trim()) + 1);
                } else {
                    //if not, assign 1 to its occurence count
                    festivalDaysUnique.put(s.trim(), 1);
                }
            }
        }

        //the below code sorts the HashMap in ascending order based on the "value" of the map.
        // The below code has been referenced from www.javacodegeeks.com.
        //The link is: https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
        Map<String, Integer> ascSortedDays = festivalDaysUnique.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)
                );

        //the below code sorts the HashMap in descending order based on the "value" of the map.
        // The below code has been referenced from www.javacodegeeks.com.
        //The link is: https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
        Map<String, Integer> descSortedDays = festivalDaysUnique.entrySet().stream().sorted(comparingByValue())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)
                );

        //initializing the hashmap that will hold the unique month types and respective counts
        //the format will be, for example -
        //July: 10
        //December: 5
        HashMap<String, Integer> monthsUnique = new HashMap();

        //looping through the year arraylist
        for (int i = 0; i < month.size(); i++) {
            //storing the month name
            String s = month.get(i);
            //converting the month number to month string.
            //for example, 1 will become JANUARY
            String monthName = String.valueOf(Month.of(Integer.parseInt(s.trim())));
            //checking if the map already contains the month
            if (monthsUnique.containsKey(monthName)) {
                //if yes, add 1 to its count
                monthsUnique.put(monthName, monthsUnique.get(monthName) + 1);
            } else {
                //if not, assign 1 to its occurence
                monthsUnique.put(monthName, 1);
            }
        }

        //sorting the map in descending order based on the value of the map.
        // The below code has been referenced from www.javacodegeeks.com.
        //The link is: https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
        Map<String, Integer> sortedMonth = monthsUnique.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)
                );

        //adding the sorted hashmaps to the logAnalsis arraylist which will be returned later
        logAnalysis.add(sortedCountry);//adding the sorted countries
        logAnalysis.add(sortedYear);//adding the sorted years
        logAnalysis.add(sortedMonth);//adding the sorted months
        logAnalysis.add(ascSortedDays);//adding the sorted days (ascending order)
        logAnalysis.add(descSortedDays);//adding the sorted dats (descending order)
        logAnalysis.add(sortedTypes);//adding the sorted types
        logAnalysis.add(sortedAgent);//adding the agent names

        //returning the arraylist of hash maps
        return logAnalysis;
    }

}


