import java.time.LocalDate;
import java.time.LocalDateTime;

/*
    public Service(LocalDate _date, String _id, String _mem_id, String _svc_code, String _comments){
    public void print_service()
    public void set_insert_service(String prov_id, String mem_id, LocalDate dos, String com, String s_code)
 */

public class Service
{
    //generic data member for all service objects
    public String name;
    public String description;
    public String code;
    public float fee;

    //specific data members for a record of service
    public String provider_name;
    public String provider_id;
    public String member_id;
    public LocalDate date_of_service;
    public LocalDateTime current_date_time;
    public String comments;
    public int number;

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

    public Service(LocalDate _date, String _id, String _mem_id, String _svc_code, String _comments){
        this.date_of_service = _date;
        this.provider_id = _id;
        this.member_id = _mem_id;
        this.code = _svc_code;
        this.comments = _comments;
    }

    public void print_service()
    {
        System.out.printf("%-10s %-29s %-8.2f %s", code, name, fee, description);
        System.out.println();
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
