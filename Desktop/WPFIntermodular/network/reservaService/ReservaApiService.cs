using Microsoft.AspNetCore.Routing.Matching;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;
using WPFIntermodular.model;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace WPFIntermodular.network.reservaService
{
    public static class ReservaApiService
    {
        private static readonly HttpClient _httpClient = new HttpClient();
        private static string baseUrl = "http://localhost:3000/api/reservas/";
        private static string _authToken;

        public static void SetAuthToken(string token)
        {
            _authToken = token;
        }

        public static HttpClient ReservaAPIService => _httpClient;

        public async static Task<List<Reserva>> ObtenerReservas()
        {
            SetAuthToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3OTJhY2M4NDcxYzI5MDk3NDgxMWQ2ZiIsInRpcG8iOjUsImlhdCI6MTczOTUzMzU1NSwiZXhwIjoxNzM5NTM1MzU1fQ.EKo1WiQYvk2JACANd3neT5S8ngsefsjPGoMBk9eTaPU");
            try
            {
                string _urlEndpoint = baseUrl+"?fields=id_usuario,id_habitacion,huespedes,fecha_entrada,fecha_salida,precio_total,extrasSeleccionados,altUser&page=0&limit=5&usuarioFields=nombre,tipo,email&habitacionFields=tipo_capacidad,precio_noche,extras"; // Cambia esto por tu URL real

                if (!string.IsNullOrEmpty(_authToken))
                {
                    _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _authToken);
                }

                var response = await ReservaAPIService.GetAsync(_urlEndpoint);
                response.EnsureSuccessStatusCode();

                var json = await response.Content.ReadAsStringAsync();
                var apiResponse = JsonConvert.DeserializeObject<ApiResponse<ReservaData>>(json);

                if (apiResponse?.Data?.Reservas == null)
                {
                    DialogService.ShowWarning("No hay reservas", "Datos vacios");
                    return new List<Reserva>();
                }
                else
                {
                    return apiResponse.Data.Reservas;
                }
            }
            catch (HttpRequestException httpEx)
            {
                DialogService.ShowError(httpEx.Message, "Error de conexión:");
                return new List<Reserva>();
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error inesperado:");
                return new List<Reserva>();
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }

        public static async Task<bool> EliminarReservaAsync(string reservaId)
        {
            SetAuthToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3OTJhY2M4NDcxYzI5MDk3NDgxMWQ2ZiIsInRpcG8iOjUsImlhdCI6MTczOTQ5MTA1MiwiZXhwIjoxNzM5NDkyODUyfQ.BTg0ktY934rH19i4KXNwOUjnJLrcvd-Qi5rcI2fqUXo");
            try
            {
                // 设置认证头
                _httpClient.DefaultRequestHeaders.Authorization =
                    new AuthenticationHeaderValue("Bearer", _authToken);

                // 构造请求 URL
                var requestUrl = $"{baseUrl}{reservaId}";

                // 发送 DELETE 请求
                var response = await _httpClient.DeleteAsync(requestUrl);

                // 处理响应状态
                if (response.IsSuccessStatusCode)
                {
                    return true;
                }

                // 处理特定错误状态码
                switch (response.StatusCode)
                {
                    case HttpStatusCode.NotFound:
                        DialogService.ShowError("Reserva no encontrada");
                        break;
                    case HttpStatusCode.Conflict:
                        DialogService.ShowError($" Versión de reserva no coincide, reserva{reservaId} ya eliminada!");
                        break;
                    default:
                        DialogService.ShowError($"Error HTTP: {(int)response.StatusCode}");
                        break;
                }
                return false;
            }
            catch (HttpRequestException httpEx)
            {
                DialogService.ShowError(httpEx.Message, "Error de conexión:");
                return false;
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error inesperado:");
                return false;
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }
        public static async Task<List<CalendarDateRange>> ObtenerFechasReservadasAsync()
        {
            SetAuthToken("your_token_here");
            try
            {
                const string endpoint = "date/?";
                var requestUrl = $"id_habitacion={baseUrl}&&id={endpoint}";

                _httpClient.DefaultRequestHeaders.Authorization =
                    new AuthenticationHeaderValue("Bearer", _authToken);

                var response = await _httpClient.GetAsync(requestUrl);
                response.EnsureSuccessStatusCode();

                var json = await response.Content.ReadAsStringAsync();
                var apiResponse = JsonConvert.DeserializeObject<ApiResponse<ReservedDatesData>>(json);

                return apiResponse?.Data?.ReservedDates?
                    .Select(d => new CalendarDateRange(d.FechaEntrada, d.FechaSalida))
                    .ToList() ?? new List<CalendarDateRange>();
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error obteniendo fechas reservadas");
                return new List<CalendarDateRange>();
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }
    }

    public class ApiResponse<T>
    {
        [JsonProperty("statusCode")]
        public int StatusCode { get; set; }

        [JsonProperty("message")]
        public string Message { get; set; }

        [JsonProperty("data")]
        public T Data { get; set; }

        [JsonProperty("error")]
        public string Error { get; set; }
    }
    public class ReservedDatesData
    {
        [JsonProperty("reservedDates")]
        public List<ReservedDate> ReservedDates { get; set; }
    }

    public class ReservedDate
    {
        [JsonProperty("fecha_entrada")]
        public DateTime FechaEntrada { get; set; }

        [JsonProperty("fecha_salida")]
        public DateTime FechaSalida { get; set; }
    }
    public class ReservaData
    {
        [JsonProperty("reservas")]
        public List<Reserva> Reservas { get; set; }
    }

    public class Reserva : INotifyPropertyChanged
    {
        private bool _isSelected;
        [JsonProperty("_id")]
        public string Id { get; set; }

        [JsonProperty("id_usuario")]
        private Usuario usuario;
        public Usuario Usuario {
            get => usuario;
            set
            {
                usuario = value;
                OnPropertyChanged();
            } 
        }

        [JsonProperty("id_habitacion")]
        private Habitacion habitacion;
        public Habitacion Habitacion {
            get => habitacion;
            set
            {
                habitacion = value;
                OnPropertyChanged();
            }
        }

        [JsonProperty("huespedes")]
        private int cantidadHuesped;
        public int CantidadHuesped {
            get => cantidadHuesped;
            set
            {
                cantidadHuesped = value;
                OnPropertyChanged();
            }
        }

        [JsonProperty("fecha_entrada")]
        private DateTime fecha_ini;
        public DateTime Fecha_ini {
            get => fecha_ini;
            set {
                fecha_ini = value;
                OnPropertyChanged();        
            }
        }

        [JsonProperty("fecha_salida")]
        private DateTime fecha_fin;
        public DateTime Fecha_fin {
            get => fecha_fin;
            set
            {
                fecha_fin = value;
                OnPropertyChanged();
            }
        }

        [JsonProperty("precio_total")]
        private decimal precio_total;
        public decimal Precio_total {
            get => precio_total;
            set
            {
                precio_total = value;
                OnPropertyChanged();
            }
        }
        [JsonProperty("extrasSeleccionados")]
        private List<Extras> extrasSeleccionadas;
        public List<Extras> ExtrasSeleccionadas
        {
            get => extrasSeleccionadas;
            set {  
                extrasSeleccionadas = value;
                OnPropertyChanged();
            }
        }

        [JsonProperty("altUser")]
        private string altUser;
        public string AltUser
        {
            get => altUser;
            set
            {
                altUser = value;
                OnPropertyChanged();
            }
        }
        public bool IsSelected
        {
            get => _isSelected;
            set
            {
                _isSelected = value;
                OnPropertyChanged();
                Debug.WriteLine($"Reserva {Id} IsSelected changed to {value}");
            }
        }


        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }

    public class Usuario : INotifyPropertyChanged
    {
        [JsonProperty("_id")]
        public string Id { get; set; }

        [JsonProperty("nombre")]
        public string Nombre { get; set; }

        [JsonProperty("email")]
        public string Email { get; set; }
        [JsonProperty("tipo")]
        public int Tipo { get; set; }
        public string TipoString => Tipo switch
        {
            1 => "Cliente",
            2 => "Empleado",
            3 => "Administrador",
            5 => "Super Administrador",
            _ => "Desconocido"
        };
        public event PropertyChangedEventHandler PropertyChanged;
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }

    public class Habitacion : INotifyPropertyChanged
    {
        [JsonProperty("_id")]
        public string Id { get; set; }

        [JsonProperty("tipo_capacidad")]
        public TipoCapacidad TipoCapacidad { get; set; }
        [JsonProperty("Extras")]
        public List<Extras> Extras { get; set; }

        [JsonProperty("precio_noche")]
        public decimal PrecioNoche { get; set; }
        public event PropertyChangedEventHandler PropertyChanged;
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }

    public class TipoCapacidad
    {
        [JsonProperty("tipo")]
        public string Tipo { get; set; }

        [JsonProperty("capacidad")]
        public int Capacidad { get; set; }
    }
    public class Extras : INotifyPropertyChanged
    {
        [JsonProperty("nombre")]
        public string Nombre { get; set; }

        [JsonProperty("precio")]
        public decimal Precio { get; set; }

        private bool _isSelected;
        public bool IsSelected
        {
            get => _isSelected;
            set
            {
                _isSelected = value;
                OnPropertyChanged();
            }
        }

        public event PropertyChangedEventHandler? PropertyChanged;
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
