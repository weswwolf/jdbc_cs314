import java.time.LocalDate;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Terminal
{
    private static Scanner input;
    private static ArrayList<Service> directory;

    public Terminal(){
        input = new Scanner(System.in);
        directory = new ArrayList<>();
    }

    //displays the service directory
    public int view_service_directory()
    {
        if (directory == null || directory.size() == 0) {
            return 1;
        }
        for (Service service : directory) {
            service.print_service();
        }
        return 0;
    }


    //validates that a provider id is in the database
    public int verify_provider(String prov_id)
    {
        return myjdbc.validate_provider(prov_id);
    }

    //function that handles the return value from validating a provider
    public int login_handle(int validation)
    {
        if (validation == 1)
        {
            System.out.println("Database Error");
            return validation;
        }
        else if (validation == 2)
        {
            System.out.println("Invalid Provider");
            return validation;
        }
        else if (validation == 0)
        {
            System.out.println("Access Granted");
            return validation;
        }
        return 3;
    }

    //function to bill a member
    public void bill_a_member(String prov_id, String mem_id)
    {
        int returned = 1;
        char check;
        String dos;
        String s_code;
        char flag;
        String comments;
        Service service = new Service();

        Service new_service = new Service();


        while (returned == 1)
        {
            System.out.println("Member Id to Bill: ");
            mem_id = input.next();
            returned = handle_member(myjdbc.validate_member(mem_id));
            //if member is suspended, return to main menu
            if (returned == 2)
                return;
        }
        do
        {
            returned = 0;
            System.out.println("Enter Date of Service (yyyy-mm-dd): ");
            dos = input.next();
            try
            {
                LocalDate.parse(dos);
            } catch (Exception E) {
                System.out.println("Invalid Date/Format (yyyy-mm-dd)");
                returned = 1;
            }
            view_service_directory();
        } while (returned != 0);
        do
        {
            do {
                System.out.print("Enter Service Code: ");
                s_code = input.next();
                returned = myjdbc.validate_service_code(s_code);

                if (returned == 1)
                    System.out.println("Database Problem");
                else if (returned == 2)
                    System.out.println("Service code not found");
            } while (returned != 0);

            myjdbc.fill_service_data(s_code, service);
            System.out.println(service.name);
            do {
                System.out.print("Is this the correct service? (Y or N) ");
                check = input.next().charAt(0);
                if(check != 'y' && check != 'Y' && check != 'N' && check != 'n') {
                    System.out.print("Not a valid answer, please try again.");
                }
            } while(check != 'y' && check != 'n' && check != 'N' && check !='Y');
        } while (check != 'y' && check != 'Y');
        System.out.println("Service code accepted");
        System.out.println("Enter Comments? (Y or N): ");
        flag = input.next().charAt(0);
        if (flag == 'Y' || flag == 'y')
        {
            input.nextLine();
            System.out.println("Enter any comments: ");
            comments = input.nextLine();
            new_service.set_insert_service(prov_id, mem_id, LocalDate.parse(dos), comments, s_code);
        }
        else
        {
            new_service.set_insert_service(prov_id, mem_id, LocalDate.parse(dos), " ", s_code);
        }
        returned = myjdbc.insert_service_record(new_service);
        System.out.println(String.format("Fee: $%-6.2f", service.fee));

        if (returned == 1)
            System.out.println("There was a problem with billing the member, please try again");
        else if (returned == 0)
            System.out.println("Member Successfully Billed");
    }

    //function that handles the user selection from the menu_selection
    public void handle_provider_menu_selection(int selection, String prov_id, String mem_id)
    {
        switch(selection)
        {
            case 1:
                view_service_directory();
                break;
            case 2:
                System.out.print("Member Id: ");
                mem_id = input.next();
                handle_member(myjdbc.validate_member(mem_id));
                break;
            case 3:
                bill_a_member(prov_id, mem_id);
                break;
            default:
                break;
        }
    }

    //function that handles the user selection from the manager menu_selection
    public void handle_manager_menu_selection(int selection, String prov_id, String mem_id)
    {
        switch(selection)
        {
            case 1:
                individual_member_report();
                break;
            case 2:
                individual_provider_report();
                break;
            case 3:
                myjdbc.weekly_services();
                break;
            default:
                break;
        }
    }

    //main menu
    public int main_menu()
    {
        int selection;
        do {
            try {
                System.out.print("Terminal Menu\n\n");
                System.out.println("1 - Provider Terminal");
                System.out.println("2 - Manager Terminal");
                System.out.println("9 - LOGOUT");

                selection = input.nextInt();
                input.nextLine();
            } catch (InputMismatchException inputMismatchException) {
                System.err.println("Not an integer. Please try again.");
                input.nextLine();
                selection = 0;
            }

            if(selection != 1 && selection != 2 && selection != 9) {
                System.err.println("Please enter a valid option.");
            }
        }while(selection != 1 && selection != 2 && selection !=9);
        return selection;
    }

    //main user interface of the provider terminal
    public int provider_menu_selection()
    {
        int selection;
        try {
            System.out.print("Provider Menu\n\n");
            System.out.println("1 - View Service Directory");
            System.out.println("2 - Validate a Member");
            System.out.println("3 - Bill a Member");
            System.out.println("9 - LOGOUT");

            selection = input.nextInt();
            input.nextLine();
        }

        catch(InputMismatchException inputMismatchException) {
            System.err.println("Not an integer. Please try again.");
            input.nextLine();
            selection = 0;
        }
        return selection;
    }

    //main user interface of the manager terminal
    public int manager_menu_selection()
    {
        int selection;
        try {
            System.out.print("Manager Menu\n\n");
            System.out.println("1 - Individual Member Report");
            System.out.println("2 - Individual Provider Report");
            System.out.println("3 - Run Weekly Report");
            System.out.println("9 - LOGOUT");

            selection = input.nextInt();
            input.nextLine();
        }

        catch(InputMismatchException inputMismatchException) {
            System.err.println("Not an integer. Please try again.");
            input.nextLine();
            selection = 0;
        }
        return selection;
    }

    //this function handles the return value that is returned when validating a member id. Return 0 if member valid,
    //2 in member suspended, 1 for all other reasons
    public int handle_member(int to_handle)
    {
        if (to_handle == 0)
        {
            System.out.println("Member Validated");
            return 0;
        }
        else if (to_handle == 4)
        {
            System.out.println("Member Suspended");
            return 2;
        }
        else if (to_handle == 1)
            System.out.println("Problem Connecting to Database");
        else if (to_handle == 3)
            System.out.println("Invalid Member");

        return 1;
    }

    //creates a report of services rendered for an individual member
    public void individual_member_report()
    {
        String mem_id;
        int returned;
        do
        {
            System.out.println("Member Id: ");
            mem_id = input.next();
            returned = handle_member(myjdbc.validate_member(mem_id));
        } while (returned == 1);
        myjdbc.generate_individual_member_report(mem_id);
        System.out.print("report created\n");
    }
    public void individual_provider_report()
    {
        String prov_id;
        int returned;
        do
        {
            System.out.println("Provider Id: ");
            prov_id = input.next();
            returned = verify_provider(prov_id);
        } while (returned != 0);
        myjdbc.generate_individual_provider_report(prov_id);
        System.out.print("report created\n");
    }

    static public void main(String[] args)
    {
        myjdbc.connect_to_database();
        Terminal main_terminal = new Terminal();
        //loads the service directory from database to terminal
        directory = myjdbc.get_service_directory();
        int validation = -1;
        int selection;
        selection = main_terminal.main_menu();
        Provider logged_in = new Provider();
        String prov_id = null;
        String mem_id = null;
        if (selection == 1)
        {
            while (validation != 0) {
                System.out.println("Enter Provider Id: ");
                prov_id = input.next();
                validation = main_terminal.verify_provider(prov_id);
                main_terminal.login_handle(validation);
                if (validation == 0) {
                    logged_in.provider_id = prov_id;
                }
            }
            while (selection != 9) {
                selection = main_terminal.provider_menu_selection();
                main_terminal.handle_provider_menu_selection(selection, prov_id, mem_id);
            }
            System.out.println("\nGOODBYE");
        }
        if (selection == 2)
        {
            while (selection != 9) {
                selection = main_terminal.manager_menu_selection();
                main_terminal.handle_manager_menu_selection(selection, prov_id, mem_id);
            }
            System.out.println("\nGOODBYE");
        }

    }
}
