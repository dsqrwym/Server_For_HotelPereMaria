

using System.Net.Http;
using System.Text.Json.Serialization;
using WPFIntermodular.model;
using System.Text;
using Newtonsoft.Json;
public class ConexionApi
{
    private readonly HttpClient _httpClient;
    
    public ConexionApi()
    {
        _httpClient = new HttpClient();
    }

    public HttpClient API => _httpClient;




    public async Task<List<Habitacion>> AllHabitaciones()
    {
        try
        {
            string _ulrEndpoint = "http://localhost:3000/api/habitaciones";

            var response = await API.GetAsync(_ulrEndpoint);
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            var habitaciones = JsonConvert.DeserializeObject<List<Habitacion>>(json);

            return habitaciones;
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex.Message);
            return new List<Habitacion>();
        }
    }
}