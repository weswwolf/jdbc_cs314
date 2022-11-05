import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.prefs.Preferences;


/*
Connection -- a session with a specific database
Statement -- an object used for executing a static SQL statement and returning the results
ResultSet -- a table of data representing a database result set usually generated by executing a query to the database.
Preferences -- a way to store key value pairs persistently so that between processes the value does not reset.
*/

/*
    current functions
    -----------------------
    -- can look up whether a provider exists with try_provider_id(p_id)
    -- can look up whether a member exists AND is not suspended with try_member_id(m_id);
    -- can insert a new table entry into the services table with arguments for the data.
    -- can read from the weekly table of services
    -----------------------
    future functions
    -----------------------
    -- make provider files
    -- do summary report
    -----------------------

*/

// the fee class is useful because we can't pass primitives by reference. We can pass objects by reference though.
class fee
{
    float f;
}

public class myjdbc {
    private static Connection conn;
    private static Statement stmt;
    private static ResultSet rs;
    static int service_number = 1;

    // use the member id to find the member name and combined_address and return them in a string array
    // this could be refactored to return the data instead to a member object instead of a string array
    static String [] fill_member_data(String mem_id)
    {
        try
        {
        Statement mem_stmt = conn.createStatement();
        String query = "select * from Members where id=" + mem_id;
        String member_name, member_combined_address;

            ResultSet member_search = mem_stmt.executeQuery(query);
            if (member_search.next())
            {
                member_name = member_search.getString("name");
                member_combined_address = member_search.getString("address")
                        + "\t" +member_search.getString("city")
                        + "\t" +member_search.getString("state")
                        + "\t" +member_search.getString("zip");
                return new String[] {member_name, member_combined_address};
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new String[] {"missing-member-name", "missing-member-combined-address"};
    }

    // get the provider name and combined address given the provider id and return them in a string array
    // this could be refactored to return the data to a provider object instead of a string array
    static String[] fill_provider_data(String pro_id)
    {
        try
        {
            Statement pro_stmt = conn.createStatement();
            String query = "select * from Providers where id=" + pro_id;
            ResultSet provider_search = pro_stmt.executeQuery(query);
            if (provider_search.next())
            {
                String provider_name = provider_search.getString("name");
                String member_combined_address = provider_search.getString("address")
                        + "\t" +provider_search.getString("city")
                        + "\t" +provider_search.getString("state")
                        + "\t" +provider_search.getString("zip");
                return new String[] {provider_name, member_combined_address};
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new String[] {"missing-provider-name", "missing-provider-combined-address"};
    }

    // use the service code to return the name as a string and fill the fee with the value from the service directory.
    // this could be refactored to return a Service object instead of a fee f and string name
    static String fill_service_data(String serv_code, fee f)
    {
        try
        {
            Statement serv_stmt = conn.createStatement();
            String query = "select * from `Service Directory` where service_code=" + serv_code;
            ResultSet serv_search = serv_stmt.executeQuery(query);
            if (serv_search.next())
            {
                String service_name = serv_search.getString("service_name");
                f.f = serv_search.getInt("service_fee"); // reference value is not changed
                serv_stmt.close();
                return service_name;
            }
            serv_stmt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "missing-service-name";
    }

    //  if the file does not exist, writes the initial details then the service details
    // if the file does exist, only writes the service details. this pattern is followed for the main accounting procedure.
    static void write_to_file(String file_name, String service_details, String initial_details)
    {
        try
        {
            File output_file = new File(file_name); // get rid of spaces in the name for the file
            if (!output_file.exists())
            {
                // member file doesn't exist. we will make a new file
                //System.out.println("generating new file for member report...");
                // write to the file only the initial part -- maybe there is a better way but this currently works
                FileWriter fw = new FileWriter(file_name, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(initial_details);
                bw.close();
                fw.close();
            }

            // open another writer to write only the service information part
            FileWriter fw = new FileWriter(file_name, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(service_details);
            bw.newLine();
            bw.close();
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // for one service, writes the service to the member report file.
    // if the file does not yet exist, appends initial details at the start.
    static void member_report(String prov_name, String dos, String serv_name, String mem_id)
    {
        // write to a file that may already exist, details of the service.
        // lookup the member with the associated member id for their personal details (name, address)
        String[] member_info = fill_member_data(mem_id);
        String member_name = member_info[0]; // alias for easy reading below
        String file_name = member_name.replaceAll("\\s", "");
        // member_info[1] == address, city, state, zip
        // check whether the member has an existing file (current directory)
        // if they don't then we need to append the member information at the start.
        // it is convenient to use the members name as their file name, as long as no two members have the exact same name.
        String service_details = serv_name + " was done with provider " + prov_name + " on date " + dos;
        String initial_details = "Record of Service for " + member_name +
                                 "\nMember Number: " + mem_id +
                                 "\nAddress: " + member_info[1] + '\n';
        // write to file the details of service and optionally the initial details
        write_to_file(file_name, service_details, initial_details);
    }

    // append the given argument information to the eft. if the eft does not yet exist, append the initial information.
    public static void eft(String prov_name, String pro_id, String dos, fee provider_fee)
    {
        String date = String.valueOf(LocalDate.now());
        String file_name = "EFT-"+date;
        String initial_details = "Start date: " + LocalDate.now().minusDays(7)+"\n"
                + "End date: " + LocalDate.now() + '\n';
        String service_details = "provider " + prov_name + " with provider id " + pro_id
                + " has fee: " + provider_fee.f + " for service on " + dos;
        write_to_file(file_name, service_details, initial_details);
    }


    // function to read Weekly Services Record table one at a time to do the main accounting procedure, EFT, and summary report
    static void read_weekly_services()
    {
        try
        {
            // create provider and member files for the services in the Weekly Service record.
            rs = stmt.executeQuery("select * from `Weekly Service Record`");
            // iterate through the weekly service table
            while (rs.next())
            {
                // these variables are good for looking up info in other tables
                //int service_number = rs.getInt("service_number"); // primary key for service
                String srv_code = rs.getString("service_code");
                String mem_id = rs.getString("member_id"); // primary key for member
                String pro_id = rs.getString("provider_id"); // primary key for provider
                // get the date of service, provider name, and service name in preparation for appending to file
                String dos = rs.getString("service-date");
                //we can do another query to the service directory to get the service name.
                fee provider_fee = new fee();
                String serv_name = fill_service_data(srv_code, provider_fee);
                // get the provider information for this service
                String[] provider_info = fill_provider_data(pro_id);
                String prov_name = provider_info[0];
                //String provider_address = provider_info[1]; unused so far
                /*
                // simple output for debug
                System.out.println("service number " + service_number +" retrieved from table");
                System.out.println("with service code: " + srv_code);
                System.out.println( "with provider id: " + pro_id);
                System.out.println("with member_id: " + mem_id);
                */

                // lookup the member with their member id for their personal details (name, address)
                // then write to file about the service details
                member_report(prov_name, dos, serv_name, mem_id);

                // do the same for the provider, but with slightly different information (look at member report for inspiration)
                {   // PROVIDER REPORT -- for the same service
                    // append to the provider file with the information:
                    // -current date/time
                    // -date of service
                    // -member name
                    // -member number
                    // -service code
                    // -fee to be paid
                    // add to the total consultations
                    // add the fee to the total provider fees.
                }

                // EFT
                eft(prov_name, pro_id, dos, provider_fee);

                {   // SUMMARY REPORT
                    // lists providers and total fees
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // end weekly_function
    }

    
    // this should be refactored so that it takes only a service as argument
    // takes the information for one service as argument, and inserts it into the weekly service record.
    // returns:
    //  success = 0
    //  database issue = 1
    public static int insert_service_record(LocalDate service_date, String provider_id, String member_id, String service_code, String comments)
    {
        try
        {
            // get the preferences to set the service_record number (primary key)
            Preferences userPreferences = Preferences.userRoot();
            service_number = userPreferences.getInt("service_number", 0) + 1; // service_number++;
            // set the new service number
            userPreferences.putInt("service_number", service_number);

            // this is the query to insert a service record into the database
            // one of the entries is the service_number which is stored persistently using Preferences
            String query = "INSERT INTO `ChocAn`.`Weekly Service Record` (`service_number`, `current-date-time`, `service-date`, `provider_id`, `member_id`, `service_code`, `comments`)  " +
                    //"VALUES ('" +String.valueOf(service_number)+"', '"+ LocalDateTime.now()+"', '"+service_date+"', '"+provider_id+"', '"+member_id+"', '"+service_code+"', '" +comments +"');";
                    "VALUES ('" + userPreferences.getInt("service_number", 0)+"', '"+ LocalDateTime.now()+"', '"+service_date+"', '"+provider_id+"', '"+member_id+"', '"+service_code+"', '" +comments +"');";
                    // instead of doing a query, we are doing an update because we are updating a value in the database.
            //rs = stmt.executeQuery(query);
            stmt.executeUpdate(query);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 1; // problem with database query
        }
        return 0; // success
    }



    /* try to validate a provider by querying the database with the providers id.
        return:
        provider validated = 0
        invalid provider id = 2
        database issue = 1
     */
    public static int validate_provider(String p_id)
    {
        boolean exist = false;
        try
        {
            rs = stmt.executeQuery("select * from Providers where id=" + p_id);
            exist = rs.next();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        if (exist)
        {
            //System.out.println("provider id accepted by database: " + p_id);
            return 0; // success
        }
        else
        {
            // terminal side output removed
            // invalid provider
            return 2;
        }
    }

    /* try to validate a member by querying the database with the members id.
        return:
        success = 0
        suspended = 4
        invalid member id = 3
        database issue = 1
     */
    public static int validate_member(String m_id)
    {
        boolean first_index_exists; // set to true if the first index of the table exists. false otherwise.
        int suspended; // flag changes to 1 when suspended
        try
        {
            rs = stmt.executeQuery("select * from Members where id=" + m_id);
            first_index_exists = rs.next(); // move to first row
            suspended = rs.getInt("suspended");
            //String member_name = rs.getString("name");
            if (first_index_exists)
            {
                // the member is found
                // check member suspended
                if (suspended == 1) {
                    // member is suspended
                    return 4;
                }
                //member is validated
                return 0;
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            // problem connecting to database!
            return 1;
        }
        // Invalid member
        return 3;
    }

    public static void main(String[] args)
    {
        // load preferences for weekly_service table -- persistent value
        Preferences userPreferences = Preferences.userRoot();
        service_number = userPreferences.getInt("service_number", 0);
        if (service_number == 0) // there was no service_number saved
        {
            // save the service number key and value
            userPreferences.putInt("service_number", service_number);
        }


        try // initialize connection to database
        {
            // enter ip address of server and user/password
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/ChocAn", "wes", "potato");
            stmt = conn.createStatement();
        }
        catch (Exception e) // there was a problem in the connection to database. (probably the driver)
        {
            e.printStackTrace();
        }


        // example input to database to validate a member or provider
        String p_id = "123456789";
        String m_id = "112233";
        int provider_access = validate_provider(p_id);
        int member_billing = validate_member(m_id);

        // return value of zero indicates success
        if (provider_access == 0 && member_billing == 0)
        {
            System.out.println("Access granted to member billing\n");
        }

        /* example information to input into service table
        String provider_id = "123456788";
        String member_id = "123456788";
        String service_code = "656565";
        String comments = "example comment";
        insert_service_record(LocalDate.now(), provider_id, member_id, service_code, comments);
        */

        read_weekly_services(); // do the main accounting procedure, EFT, and summary report

        // close connection to database
        try
        {
            conn.close();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
