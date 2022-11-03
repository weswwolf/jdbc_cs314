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

    -----------------------
    future functions
    -----------------------
    -- can read from the weekly file of services
    -----------------------

*/

class fee
{
    float f;
}

public class myjdbc {
    private static Connection conn;
    private static Statement stmt;
    private static ResultSet rs;

    // use the member id to find the member name and combined_address
    static String [] lookup_member_name_address(String mem_id)
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

    // get the provider name given the provider id
    static String[] lookup_provider_name_address(String pro_id)
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

    static String lookup_service_name_and_fee(String serv_code, fee f)
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
    // service_number is primary key for weekly services
    static int service_number = 1;

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
                String serv_name = lookup_service_name_and_fee(srv_code, provider_fee);

                // get the provider information for this service
                String[] provider_info = lookup_provider_name_address(pro_id);
                String prov_name = provider_info[0];
                //String provider_address = provider_info[1]; unused so far

                /*
                // simple output for debug
                System.out.println("service number " + service_number +" retrieved from table");
                System.out.println("with service code: " + srv_code);
                System.out.println( "with provider id: " + pro_id);
                System.out.println("with member_id: " + mem_id);
                */

                { // MEMBER REPORT (possibly its own function)
                    // lookup the member with the associated member id for their personal details (name, address)
                    String[] member_info = lookup_member_name_address(mem_id);
                    String member_name = member_info[0]; // alias for easy reading below
                    // member_info[1] == address, city, state, zip
                    // check whether the member has an existing file (current directory)
                    // if they don't then we need to append the member information at the start.
                    // it is convenient to use the members name as their file name, as long as no two members have the exact same name.
                    File member_file = new File(member_name.replaceAll("\\s", "")); // get rid of spaces in the name for the file
                    if (!member_file.exists())
                    {
                        // member file doesn't exist. we will make a new file
                        System.out.println("generating new file for member report...");
                        // write to the file only the initial part -- maybe there is a better way but this currently works
                        FileWriter fw = new FileWriter(member_name.replaceAll("\\s",""), true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write("Name: " + member_name +
                                     "\nMember Number: " + mem_id +
                                     "\nAddress: " + member_info[1] + '\n');
                        bw.close();
                        fw.close();
                    }

                    // open another writer to write only the service information part
                    FileWriter fw = new FileWriter(member_name.replaceAll("\\s",""), true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write("service " + serv_name + " on date " + dos + " with provider " + prov_name);
                    bw.newLine();
                    bw.close();
                    fw.close();
                }

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

                {   // EFT FILE
                    // get a string for the current date to mark at start of file and use for file name
                    String date = String.valueOf(LocalDate.now());
                    File eft_file = new File("EFT"+date); // get rid of spaces in the name for the file
                    if (!eft_file.exists())
                    {
                        // member file doesn't exist. we will make a new file
                        System.out.println("generating new file for weekly EFT...");
                        // write to the file only the initial part -- maybe there is a better way but this currently works
                        FileWriter fw = new FileWriter("EFT" + date, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write("Start date: " + LocalDate.now().minusDays(7)+"\n");
                        bw.write("End date: " + date + '\n');
                        bw.close();
                        fw.close();
                    }

                    // open another writer to write only the service information part
                    FileWriter fw = new FileWriter("EFT" + date, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write("provider " + prov_name + " with provider id " + pro_id
                            + " has fee: " + provider_fee.f + " for service on " + dos );

                    bw.newLine();
                    bw.close();
                    fw.close();                    // provider name, provider id, and fee
                }

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

    
    
    public static void insert_service_record(LocalDate service_date, String provider_id, String member_id, String service_code, String comments)
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
        }
    }



    // return true if the provider id is found
    public static boolean try_provider_id(String p_id)
    {
        boolean exist = false; // return value
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
            System.out.println("provider id accepted by database: " + p_id);
        }
        else
        {
            System.out.println("did not find provider id: " + p_id);
            System.out.println("please check that you have the right number");
        }
        return exist;
    }

    // return true if the member id is found (AND not suspended)
    public static boolean try_member_id(String m_id)
    {
        boolean valid_member; // return value
        int suspended; // flag changes to 1 when suspended

        try
        {
            rs = stmt.executeQuery("select * from Members where id=" + m_id);
            valid_member = rs.next(); // move to first row
            suspended = rs.getInt("suspended");
            String member_name = rs.getString("name");
            if (valid_member)
            {
                System.out.println("found member id: " + m_id);
                if (suspended == 1) {
                    System.out.println("member " + member_name + " with id: " + m_id + " is suspended");
                    return false;
                }
                System.out.println("member " + member_name + " with id: " + m_id + " is validated");
                return true;
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        System.out.println("did not find member id: " + m_id);
        System.out.println("please check that you have the right number");
        return false;
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
        boolean provider_access = try_provider_id(p_id);
        boolean member_billing = try_member_id(m_id);
        if (provider_access && member_billing)
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
