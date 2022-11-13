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
}
