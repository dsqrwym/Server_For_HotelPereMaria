

using System.Net.Http;
using System.Text.Json.Serialization;
using WPFIntermodular.model;
using System.Text;
using Newtonsoft.Json;
using System.Windows;
using Microsoft.AspNetCore.Connections;
using System.IO.Compression;
using System.IO;
using System.Security.Cryptography;
using System.Net.Http.Headers;
public class ConexionApi
{
    private readonly HttpClient _httpClient;

    public ConexionApi()
    {
        _httpClient = new HttpClient();
    }

    public HttpClient API => _httpClient;

    public string token;


    public async Task<HttpResponseMessage> PostAsync(string url, object data)
    {
        token = CargarToken();
        var json = JsonConvert.SerializeObject(data);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        return await _httpClient.PostAsync(url, content);
    }

    public async Task<List<Habitacion>> AllHabitaciones()
    {
        token = CargarToken();
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
        token = CargarToken();
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
        token = CargarToken();
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
        token = CargarToken();

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

    public async Task<bool> Login(string email, string password)
    {
        token = CargarToken();
        string url = "http://localhost:3000/api/auth/login";
        var userData = new { email, password };
        string json = JsonConvert.SerializeObject(userData);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        try
        {
            HttpResponseMessage response = await _httpClient.PostAsync(url, content);
            string result = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
            {
                MessageBox.Show($"Error en la solicitud: {response.StatusCode}\nRespuesta: {result}");
                return false;
            }

            var responseData = JsonConvert.DeserializeObject<dynamic>(result);

            if (responseData?.accessToken != null)
            {
                token = responseData.accessToken;
                GuardarToken(token);
                MessageBox.Show("Inicio de sesión exitoso." + " " + token);
                return true;
            }
            else
            {
                MessageBox.Show("Respuesta sin token.");
            }
        }
        catch (HttpRequestException ex)
        {
            MessageBox.Show($"Error de conexión: {ex.Message}");
        }
        return false;
    }

    public async Task<List<Usuario>> AllUsuarios()
    {
        try
        {
            token = CargarToken();
            string urlEndpoint = "http://localhost:3000/api/usuarios/";

            API.DefaultRequestHeaders.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

            var response = await API.GetAsync(urlEndpoint);
            string json = await response.Content.ReadAsStringAsync();

            MessageBox.Show($"JSON recibido: {json}");

            if (!response.IsSuccessStatusCode)
            {
                MessageBox.Show($"Error al obtener usuarios: {response.StatusCode}\n{json}");
                return new List<Usuario>();
            }

            try
            {
                var usuarios = JsonConvert.DeserializeObject<List<Usuario>>(json);
                if (usuarios != null) return usuarios;
            }
            catch (JsonSerializationException)
            {

                var apiResponse = JsonConvert.DeserializeObject<dynamic>(json);
                if (apiResponse != null && apiResponse.data != null)
                {
                    var usuarios = JsonConvert.DeserializeObject<List<Usuario>>(apiResponse.data.ToString());
                    return usuarios ?? new List<Usuario>();
                }
            }
        }
        catch (Exception ex)
        {
            MessageBox.Show($"Error inesperado: {ex.Message}");
        }

        return new List<Usuario>();
    }

    public async Task<bool> ReemplazarUsuarioAsync(string idUsuario, Usuario usuario)
    {
        try
        {
            token = CargarToken();

            string url = $"http://localhost:3000/api/usuarios/{idUsuario}";
            MessageBox.Show($" URL formada: {url}");

            string jsonBody = JsonConvert.SerializeObject(usuario);
            var content = new StringContent(jsonBody, Encoding.UTF8, "application/json");
            MessageBox.Show(jsonBody);

            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

            HttpResponseMessage response = await _httpClient.PutAsync(url, content);

            string responseContent = await response.Content.ReadAsStringAsync();
            Console.WriteLine($"Respuesta API: {response.StatusCode} - {responseContent}");


            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            Console.WriteLine($" Error en la actualización: {ex.Message}");
            return false;
        }
    }

    public async Task CrearUsuarioAsync(Usuario usuario)
    {
        try
        {
            token = CargarToken();
            string url = "http://localhost:3000/api/usuarios/new";

            string jsonBody = JsonConvert.SerializeObject(usuario);
            var content = new StringContent(jsonBody, Encoding.UTF8, "application/json");

            _httpClient.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);


            HttpResponseMessage response = await _httpClient.PostAsync(url, content);

            if (response.IsSuccessStatusCode)
            {
                string responseBody = await response.Content.ReadAsStringAsync();
                Console.WriteLine("Usuario creado: " + responseBody);
            }
            else
            {
                string errorBody = await response.Content.ReadAsStringAsync();
                Console.WriteLine($"Error al crear usuario: {response.StatusCode} - {errorBody}");
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Excepción al conectar con la API: " + ex.Message);
        }
    }

    public string CargarToken()
    {
        string path = "token.dat";
        if (!File.Exists(path)) return null;

        byte[] encryptedToken = File.ReadAllBytes(path);
        byte[] decryptedToken = ProtectedData.Unprotect(
            encryptedToken,
            null,
            DataProtectionScope.CurrentUser
        );

        return Encoding.UTF8.GetString(decryptedToken);
    }
    public void GuardarToken(string token)
    {
        string path = "token.dat";
        byte[] encryptedToken = ProtectedData.Protect(
            Encoding.UTF8.GetBytes(token),
            null,
            DataProtectionScope.CurrentUser
        );
        File.WriteAllBytes(path, encryptedToken);
    }
}