

using System.Net.Http;
using System.Text.Json.Serialization;
using WPFIntermodular.model;
using System.Text;
using Newtonsoft.Json;
using System.Windows;
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
            MessageBox.Show("ALLHABITACIONES");
            string _ulrEndpoint = "http://localhost:3000/api/habitaciones";

            API.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3OGZjOTkzOThjMjk5Y2Y3NmJlZDc4YiIsInRpcG8iOjMsImlhdCI6MTczOTMwMTc5MSwiZXhwIjoxNzM5MzAzNTkxfQ.rpVSv_1md1RpMWyNL5fhI1A5EldKBsqMU7tZtsQFEqY");


            var response = await API.GetAsync(_ulrEndpoint);


            var json = await response.Content.ReadAsStringAsync();
            MessageBox.Show($"JSON recibido: {json}");


            try
            {
                MessageBox.Show("Intentando deserializar JSON...");
                var habitaciones = JsonConvert.DeserializeObject<List<Habitacion>>(json);
                return habitaciones;
            }
            catch (JsonReaderException jsonEx)
            {
                MessageBox.Show($"Error de lectura JSON: {jsonEx.Message}\nJSON recibido: {json}");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error inesperado: {ex.Message}");
            }



            return null;
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex.Message);
            return null;
        }
    }
}