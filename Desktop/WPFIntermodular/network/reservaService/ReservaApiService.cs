using Newtonsoft.Json;
using System.Diagnostics;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Windows.Controls;
using WPFIntermodular.model.reserva;

namespace WPFIntermodular.network.reservaService
{
    public static class ReservaApiService
    {
        private static readonly HttpClient _httpClient = new HttpClient();
        private static string BASE = "http://localhost:3000/api/";
        private static string baseUrl = BASE + "reservas/";
        private static string _authToken;


        public static void SetAuthToken(string token)
        {
            _authToken = token;
        }

        public static HttpClient ReservaAPIService => _httpClient;

        public async static Task<List<Reserva>> ObtenerReservas()
        {

            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                string _urlEndpoint = baseUrl + "?fields=id_usuario,id_habitacion,huespedes,fecha_entrada,fecha_salida,precio_total,extrasSeleccionados,altUser&usuarioFields=nombre,tipo,email&habitacionFields=Tipo,Capacidad,Precio,Extras,Images"; // Cambia esto por tu URL real

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

            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
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
        public static async Task<List<CalendarDateRange>> ObtenerFechasReservadasAsync(string id_habitacion, string id_reserva)
        {
            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                var requestUrl = $"{baseUrl}date/?id_habitacion={id_habitacion}&id={id_reserva}";
                Debug.WriteLine(requestUrl);
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
        public static async Task<bool> CrearReservaAsync(Reserva nuevaReserva)
        {
            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                _httpClient.DefaultRequestHeaders.Authorization =
                    new AuthenticationHeaderValue("Bearer", _authToken);

                // 处理Extras转换
                var requestData = new
                {
                    id_usuario = nuevaReserva.Usuario?.Id,
                    id_habitacion = nuevaReserva.Habitacion?._id,
                    huespedes = nuevaReserva.CantidadHuesped,
                    fecha_entrada = nuevaReserva.Fecha_ini.ToString("yyyy-MM-dd"),
                    fecha_salida = nuevaReserva.Fecha_fin.ToString("yyyy-MM-dd"),
                    extrasSeleccionados = nuevaReserva.ExtrasSeleccionadas? .Where(e => e.IsSelected).Select(e => new { nombre = e.Nombre, precio = e.Precio })

                };

                var json = JsonConvert.SerializeObject(requestData);
                var content = new StringContent(json, Encoding.UTF8, "application/json");
                Debug.WriteLine(json);

                var response = await _httpClient.PostAsync(baseUrl, content);

                if (response.IsSuccessStatusCode)
                {
                    return true;
                }

                HandleErrorResponse(response);
                return false;
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error creando reserva");
                return false;
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }

        public static async Task<bool> ActualizarReservaAsync(Reserva reservaActualizada)
        {
            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                _httpClient.DefaultRequestHeaders.Authorization =
                    new AuthenticationHeaderValue("Bearer", _authToken);

                // 构造更新数据，排除不允许修改的字段
                var updateData = new
                {
                    huespedes = reservaActualizada.CantidadHuesped,
                    fecha_entrada = reservaActualizada.Fecha_ini.ToString("yyyy-MM-dd"),
                    fecha_salida = reservaActualizada.Fecha_fin.ToString("yyyy-MM-dd"),
                    extrasSeleccionados = reservaActualizada.ExtrasSeleccionadas?.Where(e => e.IsSelected).Select(e => new { nombre = e.Nombre, precio = e.Precio })
                };

                var json = JsonConvert.SerializeObject(updateData);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PutAsync($"{baseUrl}{reservaActualizada.Id}", content);

                if (response.IsSuccessStatusCode) return true;

                HandleErrorResponse(response);
                return false;
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error actualizando reserva");
                return false;
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }

        private static void HandleErrorResponse(HttpResponseMessage response)
        {
            switch (response.StatusCode)
            {
                case HttpStatusCode.BadRequest:
                    DialogService.ShowError("Datos de entrada inválidos");
                    break;
                case HttpStatusCode.Conflict:
                    DialogService.ShowError("Conflicto de versión, la reserva fue modificada por otro usuario");
                    break;
                case HttpStatusCode.NotFound:
                    DialogService.ShowError("Reserva no encontrada");
                    break;
                default:
                    DialogService.ShowError($"Error HTTP: {(int)response.StatusCode}");
                    break;
            }
        }

        public static async Task<List<Extras>> ObtenerExtrasAsync()
        {
            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                var requestUrl = BASE + "habitacion/extras";
                _httpClient.DefaultRequestHeaders.Authorization =
                    new AuthenticationHeaderValue("Bearer", _authToken);

                var response = await _httpClient.GetAsync(requestUrl);
                response.EnsureSuccessStatusCode();

                var json = await response.Content.ReadAsStringAsync();
                var apiResponse = JsonConvert.DeserializeObject<ApiResponse<List<Extras>>>(json);

                return apiResponse?.Data ?? new List<Extras>();
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error obteniendo extras");
                return new List<Extras>();
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }
        public static async Task<List<TipoCapacidad>> ObtenerTiposHabitacionAsync()
        {
            string url = BASE + "habitacion/types";
            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _authToken);

                var response = await _httpClient.GetAsync(url);
                response.EnsureSuccessStatusCode();

                var json = await response.Content.ReadAsStringAsync();
                var apiResponse = JsonConvert.DeserializeObject<ApiResponse<List<TipoCapacidad>>>(json);

                return apiResponse?.Data as List<TipoCapacidad> ?? new List<TipoCapacidad>();
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error obteniendo tipos de habitación");
                return new List<TipoCapacidad>();
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }
        public static async Task<List<Habitacion>> ObtenerHabitacionesDisponiblesAsync(DateTime fechaEntrada, DateTime fechaSalida)
        {
            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                string url = $"{baseUrl}habitacion_disponible/?fecha_entrada={fechaEntrada:yyyy-MM-dd}&fecha_salida={fechaSalida:yyyy-MM-dd}";

                _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _authToken);

                // Send GET request to retrieve available rooms
                var response = await _httpClient.GetAsync(url);
                response.EnsureSuccessStatusCode();

                // Parse the response
                var json = await response.Content.ReadAsStringAsync();
                var apiResponse = JsonConvert.DeserializeObject<ApiResponse<List<Habitacion>>>(json);

                return apiResponse?.Data ?? new List<Habitacion>(); // Return available rooms or an empty list if no data
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error obteniendo habitaciones disponibles");
                return new List<Habitacion>(); // Return empty list on error
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }

        public static async Task<List<Usuario>> ObtenerClientesAsync()
        {
            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                string url = BASE + "user/client";

                _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _authToken);

                var response = await _httpClient.GetAsync(url);
                response.EnsureSuccessStatusCode();

                var json = await response.Content.ReadAsStringAsync();
                var apiResponse = JsonConvert.DeserializeObject<ApiResponse<List<Usuario>>>(json);

                return apiResponse?.Data ?? new List<Usuario>();
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error obteniendo clientes");
                return new List<Usuario>();
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }

        public static async Task<List<Usuario>> ObtenerEmpleadosAsync()
        {
            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                string url = BASE + "user/employee";

                _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _authToken);

                var response = await _httpClient.GetAsync(url);
                response.EnsureSuccessStatusCode();

                var json = await response.Content.ReadAsStringAsync();
                var apiResponse = JsonConvert.DeserializeObject<ApiResponse<List<Usuario>>>(json);

                return apiResponse?.Data ?? new List<Usuario>();
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error obteniendo clientes");
                return new List<Usuario>();
            }
            finally
            {
                _httpClient.DefaultRequestHeaders.Authorization = null;
            }
        }
    }

}
