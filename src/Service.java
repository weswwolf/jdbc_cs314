import java.time.LocalDate;

public class Service
{
    public int number;
    public String name;
    public LocalDate current_date;
    public LocalDate date_of_service;
    public String provider_name;
    public String provider_id;
    public String member_id;
    public String code;
    public String comments;
    public float fee;

    // basic constructor
    Service()
    {
        number = -1;
        name = "no-name";
        provider_id = "no-provider";
        code = "no-code";
        comments = "no-comments";
        fee = 0.0f;
    }

    //does not set every member
    public void set_insert_service(String prov_id, String mem_id, LocalDate dos, String com, String s_code)
    {
        provider_id = prov_id;
        member_id = mem_id;
        date_of_service = dos;
        comments = com;
        code = s_code;
    }

}
