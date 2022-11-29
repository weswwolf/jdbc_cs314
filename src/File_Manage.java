import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;

/* Functions
    static void delete_file(String to_delete)
    static void delete_all_files()
    static void write_to_file(String file_name, String service_details, String initial_details)
*/

public class File_Manage
{
    public static void delete_file(String to_delete)
    {
        File d_delete = new File(to_delete);
        if(d_delete.delete())
            System.out.println(to_delete + " Deleted");
    }

    public static void delete_all_files()
    {
        ArrayList<Member> m;
        ArrayList<Provider> p;
        m = myjdbc.get_all_members();
        p = myjdbc.get_all_providers();

        //iterate through array list and do the following for each
        for (Member member : m)
        {
            String file_name = member.name.replaceAll("\\s", "").concat("-" + member.member_id);
            File_Manage.delete_file(file_name);
        }
        for (Provider provider : p)
        {
            String file_name = provider.name.replaceAll("\\s", "").concat("-" + provider.provider_id);
            File_Manage.delete_file(file_name);
        }
        //delete EFT file
        String file_name = ("EFT-").concat(LocalDate.now().toString());
        File_Manage.delete_file(file_name);
        //delete Summary Report file
        file_name = "Summary-Report-".concat(LocalDate.now().toString());
        File_Manage.delete_file(file_name);
    }

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

    //TODO use the formatting in this function as a model to reformat other files
    static void append_summary_report(Provider p)
    {
        String file_name = "Summary-Report-".concat(LocalDate.now().toString());
        String start_details = "Summary Report\n\n" + String.format("%-20s %-15s %s\n", "Name", "Consults", "Total");
        String details = String.format("%-20s %-15s %-8.2f", p.name, p.consultations, p.total_fee);
        //System.out.println();
        write_to_file(file_name, details, start_details);
    }

    static void final_append_summary(int num_prov, int num_consults, float total)
    {
        String file_name = "Summary-Report-".concat(LocalDate.now().toString());
        String start_details = "";
        String details = String.format("\n\n%-20s %-15s %s\n", "Total Providers", "Total Consults", "Total Fees") +
                String.format("%-20d %-15d %-8.2f", num_prov, num_consults, total);
        //System.out.println();
        write_to_file(file_name, details, start_details);
    }
}
