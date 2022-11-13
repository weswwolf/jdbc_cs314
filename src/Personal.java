abstract class Personal
{

    public String name;
    public String address;
    public String city;
    public String state;
    public String zip;

    Personal()
    {
            name = "no-name";
            address = "no-address";
            city = "no-city";
            state = "no-state";
            zip = "no-zip";
    }


    public void set_all_personal(String name_, String address_, String city_, String state_, String zip_)
    {
        name = name_;
        address = address_;
        city = city_;
        state = state_;
        zip = zip_;
    }

    // return the combined address as one string
    public String combined_address()
    {
        return address + '\t' + city + '\t' + state + '\t' + zip;
    }

}
