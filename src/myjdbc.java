import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;


/*
Connection -- a session with a specific database
Statement -- an object used for executing a static SQL statement and returning the results
ResultSet -- a table of data representing a database result set usually generated by executing a query to the database.
*/
/*
  // CONNECTIONS
    boolean connect_to_database()
    void end_connection()
    // GETTING DATA
    Boolean fill_member_data(String mem_id, Member m)
    Boolean fill_provider_data(String pro_id, Provider fill)
    Boolean fill_service_data(String serv_code, Service s)
    // VALIDATE
    int validate_provider(String p_id)
    int validate_member(String m_id)
    int validate_service_code(String to_validate)
    // UTILITY
    int insert_service_record(Service s)
    int get_next_service_number()
    ArrayList<Service> get_service_directory()
    void generate_individual_report(String mem_id)
    void append_eft(Service s)
    void weekly_services()
*/

public class myjdbc {
    public static Connection conn;
    public static Statement stmt;
    public static ResultSet rs;

    // close the connection to the database.
    // should be done once at the end of the session
    // also useful for unit testing to create a database error.
    static void end_connection()
    {
        try
        {
            conn.close();
            stmt.close();
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    public static int get_count_serv_dir(){
        String query = "select count(*) from `Service Directory`";
        try{
            rs = stmt.executeQuery(query);
            if(rs.next()) {
               return rs.getInt(1);
            }
        }
        catch(Exception e){
            return -1;
        }
        return 0;
    }

    public static int get_count_wkly_svc(){
        String query = "select count(*) from `Weekly Service Record`";
        try{
            rs = stmt.executeQuery(query);
            if(rs.next()) {
                return rs.getInt(1);
            }
        }
        catch(Exception e){
            return -1;
        }
        return 0;
    }

    // query the database for a member_id matching the argument
    // if found, fill that member data into the argument Member m
    static Boolean fill_member_data(String mem_id, Member m)
    {
        try
        {
            Statement mem_stmt = conn.createStatement();
            String query = "select * from Members where id=" + mem_id;
            //String member_name, member_combined_address;

            ResultSet member_search = mem_stmt.executeQuery(query);
            if (member_search.next())
            {   // hit
                m.set_all_personal(member_search.getString("name"),
                        member_search.getString("address"),
                        member_search.getString("city"),
                        member_search.getString("state"),
                        member_search.getString("zip"));
                m.member_id = mem_id;
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // no matching member
        return false;
    }
    // return false if no matching provider found -- otherwise put the provider data into fill
    static Boolean fill_provider_data(String pro_id, Provider fill)
    {
        try
        {
            Statement pro_stmt = conn.createStatement();
            String query = "select * from Providers where id=" + pro_id;
            ResultSet provider_search = pro_stmt.executeQuery(query);
            if (provider_search.next())
            {   // hit
                fill.set_all_personal(provider_search.getString("name"),
                        provider_search.getString("address"),
                        provider_search.getString("city"),
                        provider_search.getString("state"),
                        provider_search.getString("zip"));
                fill.provider_id = pro_id;
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // no matching provider was found...
        return false;
    }

    //refactored to take a Service object and fill the data into the Service object.
    static Boolean fill_service_data(String serv_code, Service s)
    {
        try
        {
            Statement serv_stmt = conn.createStatement();
            String query = "select * from `Service Directory` where service_code=" + serv_code;
            ResultSet serv_search = serv_stmt.executeQuery(query);
            if (serv_search.next())
            {
                s.name = serv_search.getString("service_name");
                s.fee = serv_search.getFloat("service_fee"); // reference value is not changed
                serv_stmt.close();
                return true;
            }
            serv_stmt.close();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        return false;
    }

    /* REFACTORED ABOVE, deprecated
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
                f.f = serv_search.getInt("service_fee"); // reference value is changed
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
    */

    /*
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
    */

    // for one service, writes the service to the member report file.
    // if the file does not yet exist, appends initial details at the start.
    static void member_report(String prov_name, String dos, String serv_name, String mem_id)
    {
        Member m = new Member();
        // write to a file that may already exist, details of the service.
        // lookup the member with the associated member id for their personal details (name, address)
        //String[] member_info = fill_member_data(mem_id);
        if (!fill_member_data(mem_id, m))
        {
            System.out.println("error in filling member data. exiting member report for mem_id" + mem_id);
            return;
        }
        //String member_name = member_info[0]; // alias for easy reading below


        String file_name = m.name.replaceAll("\\s", "").concat("-"+mem_id);
        // member_info[1] == address, city, state, zip
        // check whether the member has an existing file (current directory)
        // if they don't then we need to append the member information at the start.
        // it is convenient to use the members name as their file name, as long as no two members have the exact same name.
        String service_details = serv_name + " was done with provider " + prov_name + " on date " + dos;
        String initial_details = "Record of Service for " + m.name +
                                 "\nMember Number: " + mem_id +
                                 "\nAddress: " + m.combined_address() + '\n';
        // write to file the details of service and optionally the initial details
        File_Manage.write_to_file(file_name, service_details, initial_details);
    }
    static void provider_report(Member n, Service s, Provider p)
    {
        String file_name = p.name.replaceAll("\\s", "").concat("-"+p.provider_id);
        // member_info[1] == address, city, state, zip
        // check whether the member has an existing file (current directory)
        // if they don't then we need to append the member information at the start.
        // it is convenient to use the members name as their file name, as long as no two members have the exact same name.
        String service_details = s.code + " was completed with patient " + n.name + " (" + n.member_id + ") on date " +
                s.date_of_service + " entered into computer at " + s.current_date_time + " with fee charge of " + s.fee;
        String initial_details = "Record of Service for " + p.name +
                "\nProvider Number: " + p.provider_id +
                "\nAddress: " + p.combined_address() + '\n';
        // write to file the details of service and optionally the initial details
        File_Manage.write_to_file(file_name, service_details, initial_details);
    }
    static void append_provider_report(Provider p, int num_consults, float total_fee)
    {
        String file_name = p.name.replaceAll("\\s", "").concat("-"+p.provider_id);
        // member_info[1] == address, city, state, zip
        // check whether the member has an existing file (current directory)
        // if they don't then we need to append the member information at the start.
        // it is convenient to use the members name as their file name, as long as no two members have the exact same name.
        String service_details = "Total Consultations: " + num_consults + "\tTotal Fees: " + total_fee;
        String initial_details = "";
        // write to file the details of service and optionally the initial details
        File_Manage.write_to_file(file_name, service_details, initial_details);
    }

    // append the given argument information to the eft. if the eft does not yet exist, append the initial information.
    public static void append_eft(Service s)
    {
        String date = String.valueOf(LocalDate.now());
        String file_name = "EFT-"+date;
        String initial_details = "Start date: " + LocalDate.now().minusDays(7)+"\n"
                + "End date: " + LocalDate.now() + '\n';
        String service_details = "provider " + s.provider_name + " with provider id " + s.provider_id
                + " has fee: " + s.fee + " for service on " + s.date_of_service;
        File_Manage.write_to_file(file_name, service_details, initial_details);
    }

    /* DEPRECATED, see REFACTORED above
    // append the given argument information to the eft. if the eft does not yet exist, append the initial information.
    public static void append_eft(String prov_name, String pro_id, String dos, float provider_fee)
    {
        String date = String.valueOf(LocalDate.now());
        String file_name = "EFT-"+date;
        String initial_details = "Start date: " + LocalDate.now().minusDays(7)+"\n"
                + "End date: " + LocalDate.now() + '\n';
        String service_details = "provider " + prov_name + " with provider id " + pro_id
                + " has fee: " + provider_fee + " for service on " + dos;
        File_Manage.write_to_file(file_name, service_details, initial_details);
    }
    */

    // function to read Weekly Services Record table one at a time to do the main accounting procedure, EFT, and summary report
    //TODO JohnSmith is the ONLY provider currently being added to the array list
    static void weekly_services()
    {
        File_Manage.delete_all_files();
        Provider p = new Provider();
        Member m = new Member();
        Service s = new Service();
        ArrayList <Provider> arp = new ArrayList<Provider>();
        try
        {
            // create provider and member files for the services in the Weekly Service record.
            rs = stmt.executeQuery("select * from `Weekly Service Record`");
            // iterate through the weekly service table
            while (rs.next())
            {
                // these variables are good for looking up info in other tables
                // get the date of service, provider name, and service name in preparation for appending to file
                s.number = rs.getInt("service_number"); // primary key for service
                //String srv_code = rs.getString("service_code");
                s.code = rs.getString("service_code");
                s.member_id = rs.getString("member_id"); // primary key for member
                s.provider_id = rs.getString("provider_id"); // primary key for provider
                s.date_of_service = rs.getDate("service-date").toLocalDate();
                // fill out a service object using the service code.
                if (!fill_service_data(s.code,s))
                {
                    System.out.println("error filling service data.");
                }
                // get the provider information for this service
                if (!fill_provider_data(s.provider_id,p))
                {
                    System.out.println("error filling provider data.");
                }

                // lookup the member with their member id for their personal details (name, address)
                // then write to file about the service details
                member_report(p.name, String.valueOf(s.date_of_service), s.name, s.member_id);

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
                myjdbc.fill_member_data(s.member_id, m);
                provider_report(m, s, p);
//                if (!arp.contains(p.name))
//                {
//                    arp.add(p);
//                }
                boolean found = false;
                for (int i = 0; i < arp.size(); i++)
                {
                    if (arp.get(i).name == p.name)
                        found = true;
                }
                if (!found)
                {
                    arp.add(p);
                }
                arp.get(arp.indexOf(p)).consultations++;
                arp.get(arp.indexOf(p)).total_fee += s.fee;

                // EFT
                //append_eft(p.name, p.provider_id, String.valueOf(s.date_of_service), s.fee);
                append_eft(s);
                {   // SUMMARY REPORT
                    // lists providers and total fees
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < arp.size(); i++)
        {
            System.out.println(arp.get(i).name);
        }
        for (int i = 0; i < arp.size(); i++)
        {
            append_provider_report(arp.get(i), arp.get(i).consultations, arp.get(i).total_fee);
        }
        // end weekly_function
    }

    // avoid using java preferences by calculating the service number based on how many services are in the db
    public static int get_next_service_number()
    {
        try
        {
            // find the largest service number, and return a value that is one larger.
            rs = stmt.executeQuery("select MAX(service_number) from `Weekly Service Record`");
            if (rs.next())
            {
                return rs.getInt(1)+1; // add one to the count to get to the next
            }
            return 0; // no services in weekly service table.
        }
        catch (Exception e)
        {
            // sql exception
        }
        return -1;
    }

    // refactored function to insert a service
    public static int insert_service_record(Service s)
    {
        try
        {

            // this is the query to insert a service record into the database
            String query = "INSERT INTO `ChocAn`.`Weekly Service Record` (`service_number`, `current-date-time`, `service-date`, `provider_id`, `member_id`, `service_code`, `comments`)  " +
                    //"VALUES ('" +String.valueOf(service_number)+"', '"+ LocalDateTime.now()+"', '"+service_date+"', '"+provider_id+"', '"+member_id+"', '"+service_code+"', '" +comments +"');";
                    //"VALUES ('" + userPreferences.getInt("service_number", 0)+"', '"+ LocalDateTime.now()+"', '"+service_date+"', '"+provider_id+"', '"+member_id+"', '"+service_code+"', '" +comments +"');";
                    "VALUES ('" + get_next_service_number()+"', '"+ LocalDateTime.now()+"', '"+s.date_of_service+"', '"+s.provider_id+"', '"+s.member_id+"', '"+s.code+"', '" +s.comments +"');";
            // instead of doing a query, we are doing an update because we are updating a value in the database.
            //rs = stmt.executeQuery(query);
            stmt.executeUpdate(query);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 1; // problem with query
        }
        return 0; // success
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

            // this is the query to insert a service record into the database
            String query = "INSERT INTO `ChocAn`.`Weekly Service Record` (`service_number`, `current-date-time`, `service-date`, `provider_id`, `member_id`, `service_code`, `comments`)  " +
                    //"VALUES ('" +String.valueOf(service_number)+"', '"+ LocalDateTime.now()+"', '"+service_date+"', '"+provider_id+"', '"+member_id+"', '"+service_code+"', '" +comments +"');";
                    //"VALUES ('" + userPreferences.getInt("service_number", 0)+"', '"+ LocalDateTime.now()+"', '"+service_date+"', '"+provider_id+"', '"+member_id+"', '"+service_code+"', '" +comments +"');";
                    "VALUES ('" + get_next_service_number()+"', '"+ LocalDateTime.now()+"', '"+service_date+"', '"+provider_id+"', '"+member_id+"', '"+service_code+"', '" +comments +"');";
                    // instead of doing a query, we are doing an update because we are updating a value in the database.
            //rs = stmt.executeQuery(query);
            stmt.executeUpdate(query);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 1; // problem with query
        }
        return 0; // success
    }

    /* try to validate a provider by querying the database with the provider's id.
        return:
        provider validated = 0
        invalid provider id = 2
        database issue = 1
     */
    public static int validate_provider(String p_id)
    {
        boolean exist;
        try
        {
            rs = stmt.executeQuery("select * from Providers where id=" + p_id);
            exist = rs.next();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            return 1;
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

    /* try to validate a member by querying the database with the member's id.
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
            //suspended = rs.getInt("suspended");
            //String member_name = rs.getString("name");
            if (first_index_exists)
            {
                suspended = rs.getInt("suspended");
                // the member is found
                // check member suspended
                if (suspended == 1) {
                    // member is suspended
                    return 4;
                }
                //member is validated
                return 0;
            }
            return 3;
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            // problem connecting to database!
            return 1;
        }
        // unreachable
    }

    // THIS SHOULD ONLY BE DONE ONCE. WE ONLY NEED ONE CONNECTION.
    public static boolean connect_to_database()
    {
        try // initialize connection to database
        {
            // enter ip address of server and user/password
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/ChocAn", "root", "cs314");
            stmt = conn.createStatement();
            return true;
        }
        catch (Exception e) // there was a problem in the connection to database. (probably the driver)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static int validate_service_code(String to_validate)
    {
        boolean exist;
        try
        {
            rs = stmt.executeQuery("select * from `service directory` where service_code=" + to_validate);
            exist = rs.next();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            //database connection error
            return 1;
        }
        if (exist)
        {
            //service code exists in db
            return 0; // success
        }
        else
        {
            // terminal side output removed
            // invalid service code
            return 2;
        }
    }

    //function that gets the service directory from the database and returns it
    public static ArrayList<Service> get_service_directory()
    {
        ArrayList<Service> directory = new ArrayList<Service>();
        try
        {
            rs = stmt.executeQuery("select * from `Service Directory`");
            while (rs.next())
            {
                Service new_service = new Service();
                //service_code, service_name, service_desc, service_fee
                new_service.code = rs.getString("service_code");
                new_service.name = rs.getString("service_name");
                new_service.description = rs.getString("service_desc");
                new_service.fee = rs.getFloat("service_fee");

                directory.add(new_service);
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return directory;
    }

    public static void generate_individual_member_report(String mem_id)
    {
        Service s = new Service();
        Member n = new Member();
        Provider p = new Provider();
        myjdbc.fill_member_data(mem_id, n);
        String file_name = n.name.replaceAll("\\s", "").concat("-" + mem_id);
        File_Manage.delete_file(file_name);
        try
        {
            rs = stmt.executeQuery("select * from `Weekly Service Record` where member_id=" + mem_id);
            while (rs.next())
            {
                s.member_id = mem_id;
                s.date_of_service = rs.getDate("service-date").toLocalDate();
                s.code = rs.getString("service_code");
                s.provider_id = rs.getString("provider_id");
                myjdbc.fill_provider_data(s.provider_id, p);
                fill_service_data(s.code, s);
                member_report(p.name, s.date_of_service.toString(), s.name, s.member_id);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void generate_individual_provider_report(String prov_id)
    {
        int num_consults = 0;
        float total_fee = 0.0F;
        Service s = new Service();
        Member n = new Member();
        Provider p = new Provider();
        myjdbc.fill_provider_data(prov_id, p);
        String file_name = n.name.replaceAll("\\s", "").concat("-" + prov_id);
        File_Manage.delete_file(file_name);
        try
        {
            rs = stmt.executeQuery("select * from `Weekly Service Record` where provider_id=" + prov_id);
            while (rs.next())
            {
                s.provider_id = prov_id;
                s.date_of_service = rs.getDate("service-date").toLocalDate();
                s.code = rs.getString("service_code");
                s.current_date_time = rs.getTimestamp("current-date-time").toLocalDateTime();
                s.member_id = rs.getString("member_id");
                myjdbc.fill_service_data(s.code, s);
                myjdbc.fill_member_data(s.member_id, n);
                myjdbc.fill_provider_data(s.provider_id, p);
                provider_report(n, s, p);
                num_consults++;
                total_fee += s.fee;
            }
            append_provider_report(p, num_consults, total_fee);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //function that returns array list of providers name and id
    static ArrayList<Provider> get_all_providers()
    {
        ArrayList<Provider> p = new ArrayList<Provider>();
        try
        {
            rs = stmt.executeQuery("select * from Providers");
            while (rs.next()) {
                Provider np = new Provider();
                np.name = rs.getString("name");
                np.provider_id = rs.getString("id");
                p.add(np);
            }
        }
        catch (Exception e)
        {
        }
        return p;
    }

    //function that returns array list of Members name and id
    static ArrayList<Member> get_all_members()
    {
        ArrayList<Member> m = new ArrayList<Member>();
        try
        {
            rs = stmt.executeQuery("select * from Members");
            while (rs.next()) {
                Member nm = new Member();
                nm.name = rs.getString("name");
                nm.member_id = rs.getString("id");
                m.add(nm);
            }
        }
        catch (Exception e)
        {
        }
        return m;
    }

    public static void main(String[] args)
    {
        connect_to_database();

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
        */
        //insert_service_record(LocalDate.now(), "112233445", "123456789", "101010", "mission complete");



        weekly_services(); // do the main accounting procedure, EFT, and summary report
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
