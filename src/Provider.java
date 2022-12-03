public class Provider extends Personal
{
    public String provider_id;
    public int consultations;
    public float total_fee;

    Provider()
    {
        provider_id = "no-id";
        consultations = 0;
        total_fee = 0.0f;
    }
}
