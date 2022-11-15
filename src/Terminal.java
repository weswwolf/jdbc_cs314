import java.time.LocalDate;
import java.util.Scanner;

public class Terminal
{
    protected static Scanner input = new Scanner(System.in);

    /* commented until we need to use it to stop the warning flood
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
     */

    public int menu_selection()
    {
        int selection;
        System.out.print("Terminal Menu\n\n");
        System.out.println("1 - View Service Directory");
        System.out.println("2 - Validate a Member");
        System.out.println("3 - Bill a Member");

        System.out.println("9 - LOGOUT");

        selection = input.nextInt();

        return selection;
    }

    public int verify_provider(String prov_id)
    {
        return myjdbc.validate_provider(prov_id);
    }

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
    public void handle_selection(int selection, Member to_service)
    {
        String mem_id;
        String dos;
        String s_code;
        int returned;
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
                System.out.print("Enter Date of Service (yyyy-mm-dd): ");
                input.nextLine();
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
                if (returned == 0)
                    System.out.println("Service code accepted");
                else if (returned == 1)
                    System.out.println("Database Problem");
                else if (returned == 2)
                    System.out.println("Service code not found");
                break;
            default:
                break;
        }
    }

    /*
    public void bill_member(Member to_bill, Provider billing)
    {

    }
     */

    public void handle_member(int to_handle)
    {
        if (to_handle == 0)
            System.out.println("Member Validated");
        else if (to_handle == 1)
            System.out.println("Problem Connecting to Database");
        else if (to_handle == 3)
            System.out.println("Invalid Member");
        else if (to_handle == 4)
            System.out.println("Member Suspended");
    }

    static public void main(String[] args)
    {
        myjdbc.connect_to_database();
        Terminal main_terminal = new Terminal();
        int validation = -1;
        int selection = 0;
        Member member_serviced = new Member();
        Provider logged_in = new Provider();
        String prov_id;

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
            main_terminal.handle_selection(selection, member_serviced);
        }
        System.out.println("\nGOODBYE");

    }
}
