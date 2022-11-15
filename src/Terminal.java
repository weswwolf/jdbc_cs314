import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Terminal
{
    protected static Scanner input = new Scanner(System.in);

    public int view_service_directory()
    {
        return 0;
    }

    public int bill_member(String mem_id, String service_code)
    {
        return 0;
    }

    public int generate_weekly_report()
    {
        return 0;
    }

    public int generate_individual_provider_report(String prov_id)
    {
        return 0;
    }

    public int generate_individual_member_report(String mem_id)
    {
        return 0;
    }

    //main terminal view
    public int menu_selection()
    {
        int selection = 0;
        System.out.print("Terminal Menu\n\n");
        System.out.println("1 - View Service Directory");
        System.out.println("2 - Validate a Member");
        System.out.println("3 - Bill a Member");

        System.out.println("9 - LOGOUT");

        selection = input.nextInt();

        return selection;
    }

    //this function checks if a provider id matches in the database
    public int verify_provider(String prov_id)
    {
        return myjdbc.validate_provider(prov_id);
    }

    //function that handles the return code from validating a provider
    public void login_handle(int validation)
    {
        if (validation == 1)
        {
            System.out.println("Database Error");
        }
        else if (validation == 2)
        {
            System.out.println("Invalid Provider");
        }
        else if (validation == 0)
        {
            System.out.println("Access Granted");
        }
    }

    //function that handles the selection option from the menu f
    public void handle_selection(int selection, String prov_id, String mem_id)
    {
        String dos = null;
        String s_code = null;
        String comments = null;
        int returned = -1;
        switch(selection)
        {
            case 1:
                System.out.println("Does not exist!");
                break;
            case 2:
                System.out.print("Member Id: ");
                input.nextLine();
                mem_id = input.nextLine();
                handle_member(myjdbc.validate_member(mem_id));
                break;
            case 3:
                returned = 1;
                while (returned == 1)
                {
                    System.out.print("Member Id to Bill: ");
                    input.nextLine();
                    mem_id = input.nextLine();
                    returned = handle_member(myjdbc.validate_member(mem_id));
                }
                System.out.print("Enter Date of Service (yyyy-mm-dd): ");
                dos = input.nextLine();
                try
                {
                    LocalDate localDate = LocalDate.parse(dos);
                }
                catch (Exception E)
                {
                    System.out.println("Invalid Date/Format (yyyy-mm-dd)");
                }
                System.out.print("Enter Service Code: ");
                s_code = input.nextLine();
                returned = myjdbc.validate_service_code(s_code);

                if (returned == 1)
                    System.out.println("Database Problem");
                else if (returned == 2)
                    System.out.println("Service code not found");
                else if (returned == 0)
                {
                    System.out.println("Service code accepted");
                    System.out.println("Enter any comments:");
                    comments = input.nextLine();
                    returned = myjdbc.insert_service_record(LocalDate.parse(dos), prov_id, mem_id, s_code, comments);
                    if (returned == 1)
                        System.out.println("There was a problem with billing the member, please try again");
                    else if (returned == 0)
                        System.out.println("Member Successfully Billed");
                }
                break;
            default:
                break;
        }
    }

    public void bill_member(Member to_bill, Provider billing)
    {

    }

    //function that handles the return code from verifying a member id
    //return 0 if validated, else return 1
    public int handle_member(int to_handle)
    {
        if (to_handle == 0)
        {
            System.out.println("Member Validated");
            return 0;
        }
        else if (to_handle == 1)
            System.out.println("Problem Connecting to Database");
        else if (to_handle == 3)
            System.out.println("Invalid Member");
        else if (to_handle == 4)
            System.out.println("Member Suspended");
        return 1;
    }

    static public void main(String[] args)
    {
        myjdbc.load_preferences();
        myjdbc.connect_to_database();
        Terminal main_terminal = new Terminal();
        int validation = -1;
        int selection = 0;
        Member member_serviced = new Member();
        Provider logged_in = new Provider();
        String prov_id = null;
        String mem_id = null;

        while (validation != 0)
        {
            System.out.print("Enter Provider Id: ");
            prov_id = input.nextLine();
            validation = main_terminal.verify_provider(prov_id);
            main_terminal.login_handle(validation);
            if (validation == 0)
            {
                logged_in.provider_id = prov_id;
            }
        }
        while (selection != 9)
        {
            selection = main_terminal.menu_selection();
            main_terminal.handle_selection(selection, prov_id, mem_id);
        }
        System.out.println("\nGOODBYE");

    }
}
