

using System.Net.Http;
using System.Text.Json.Serialization;
using WPFIntermodular.model;
using System.Text;
using Newtonsoft.Json;
using System.Windows;
using Microsoft.AspNetCore.Connections;
using System.IO.Compression;
using System.IO;
public class ConexionApi
{
    private readonly HttpClient _httpClient;
    
    public ConexionApi()
    {
        _httpClient = new HttpClient();
    }

    public HttpClient API => _httpClient;

    private string token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3OTJhY2M4NDcxYzI5MDk3NDgxMWQ2ZiIsInRpcG8iOjUsImlhdCI6MTczOTUzNDM0MiwiZXhwIjoxNzM5NTM2MTQyfQ.HqsSCh0In1Ggq7ddmOZelOyX24uv-fWa5tYStORCCD0";




    public async Task<List<Habitacion>> AllHabitaciones()
    {
        try
        {
            string _ulrEndpoint = "http://localhost:3000/api/habitaciones";

            API.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);


            var response = await API.GetAsync(_ulrEndpoint);


            var json = await response.Content.ReadAsStringAsync();
            MessageBox.Show($"JSON recibido: {json}");


            try
            {
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

    public async Task<bool> addHab(Habitacion habitacion)
    {
        try
        {
            const string urlEndpoint = "http://localhost:3000/api/habitaciones";

            // Configurar el header de autorización
            API.DefaultRequestHeaders.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

            // Serializar el objeto a JSON
            string json = JsonConvert.SerializeObject(habitacion);
            MessageBox.Show(json);

            // Crear el contenido sin comprimir usando StringContent
            var content = new StringContent(json, Encoding.UTF8, "application/json");

            MessageBox.Show(json);

            // Realizar el POST
            var response = await API.PostAsync(urlEndpoint, content);
            if (response.IsSuccessStatusCode)
            {
                MessageBox.Show("Habitación creada con éxito en la API.");
                return true;
            }
            else
            {
                string error = await response.Content.ReadAsStringAsync();
                MessageBox.Show($"Error al crear la habitación: {response.StatusCode}\n{error}");
                return false;
            }
        }
        catch (Exception ex)
        {
            MessageBox.Show($"Error de conexión: {ex.Message}");
            return false;
        }
    }
    public async Task<bool> deleteHab(Habitacion h)
    {
        string _ulrEndpoint = $"http://localhost:3000/api/habitaciones/{h.Id}";
        API.DefaultRequestHeaders.Authorization =
            new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        // Enviar la solicitud DELETE
        HttpResponseMessage response = await API.DeleteAsync(_ulrEndpoint);

        if (response.IsSuccessStatusCode)
        {
            MessageBox.Show("Habitación eliminada correctamente.");
            return true;
        }
        else
        {
            string error = await response.Content.ReadAsStringAsync();
            MessageBox.Show($"Error al eliminar la habitación: {response.StatusCode}\n{error}");
            return false;
        }
    }

    public async Task<bool> editHab(Habitacion h)
    {

        string _ulrEndpoint = $"http://localhost:3000/api/habitaciones/{h.Id}";


        string json = JsonConvert.SerializeObject(h);

        var content = new StringContent(json, Encoding.UTF8, "application/json");



        API.DefaultRequestHeaders.Authorization =
            new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

        
        HttpResponseMessage response = await API.PutAsync(_ulrEndpoint,content);

        if (response.IsSuccessStatusCode)
        {
            MessageBox.Show("Habitación edita correctamente.");
            return true;
        }
        else
        {
            string error = await response.Content.ReadAsStringAsync();
            MessageBox.Show($"Error al editar la habitación: {response.StatusCode}\n{error}");
            return false;
        }
    }
}