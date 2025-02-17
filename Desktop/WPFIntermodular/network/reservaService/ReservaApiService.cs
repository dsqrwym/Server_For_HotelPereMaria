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
        private const string BaseApiUrl = "http://localhost:3000/api/";
        private static string _authToken;

        public static void SetAuthToken(string token) => _authToken = token;

        private static async Task<T> SendRequestAsync<T>(HttpMethod method, string endpoint, object content = null)
        {
            try
            {
                SetAuthToken(new ConexionApi().CargarToken());
                var request = new HttpRequestMessage(method, $"{BaseApiUrl}{endpoint}");

                if (!string.IsNullOrEmpty(_authToken))
                {
                    request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _authToken);
                }

                if (content != null)
                {
                    request.Content = new StringContent(
                        JsonConvert.SerializeObject(content),
                        Encoding.UTF8,
                        "application/json");
                }

                using var response = await _httpClient.SendAsync(request);
                var responseContent = await response.Content.ReadAsStringAsync();

                if (!response.IsSuccessStatusCode)
                {
                    HandleErrorResponse(response, responseContent);
                    return default;
                }

                return JsonConvert.DeserializeObject<T>(responseContent);
            }
            catch (HttpRequestException httpEx)
            {
                DialogService.ShowError(httpEx.Message, "Connection error");
                return default;
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Unexpected error");
                return default;
            }
        }

        private static void HandleErrorResponse(HttpResponseMessage response, string content)
        {
            var statusCode = response.StatusCode;
            var message = content;

            switch (statusCode)
            {
                case HttpStatusCode.BadRequest:
                    message = "Invalid request data";
                    break;
                case HttpStatusCode.Conflict:
                    message = "Version conflict";
                    break;
                case HttpStatusCode.NotFound:
                    message = "Resource not found";
                    break;
                case HttpStatusCode.Unauthorized:
                    message = "Authentication required";
                    break;
            }

            DialogService.ShowError(message, $"HTTP Error {(int)statusCode}");
        }

        public static async Task<List<Reserva>> ObtenerReservas()
        {
            var response = await SendRequestAsync<ApiResponse<ReservaData>>(
                HttpMethod.Get,
                "reservas/?fields=id_usuario,id_habitacion,huespedes,fecha_entrada,fecha_salida,precio_total,extrasSeleccionados,altUser&usuarioFields=nombre,tipo,email&habitacionFields=Tipo,Capacidad,Precio,Extras,Images");

            return response?.Data?.Reservas ?? new List<Reserva>();
        }

        public static async Task<bool> EliminarReservaAsync(string reservaId)
        {
            var response = await SendRequestAsync<ApiResponse<object>>(
                HttpMethod.Delete,
                $"reservas/{reservaId}");

            return response != null;
        }

        public static async Task<List<CalendarDateRange>> ObtenerFechasReservadasAsync(string idHabitacion, string idReserva)
        {
            var response = await SendRequestAsync<ApiResponse<ReservedDatesData>>(
                HttpMethod.Get,
                $"reservas/date/?id_habitacion={idHabitacion}&id={idReserva}");

            return response?.Data?.ReservedDates?
                .Select(d => new CalendarDateRange(d.FechaEntrada, d.FechaSalida))
                .ToList() ?? new List<CalendarDateRange>();
        }

        public static async Task<bool> CrearReservaAsync(Reserva nuevaReserva)
        {
            var requestData = new
            {
                id_usuario = nuevaReserva.Usuario?.Id,
                id_habitacion = nuevaReserva.Habitacion?._id,
                huespedes = nuevaReserva.CantidadHuesped,
                fecha_entrada = nuevaReserva.Fecha_ini.ToString("yyyy-MM-dd"),
                fecha_salida = nuevaReserva.Fecha_fin.ToString("yyyy-MM-dd"),
                extrasSeleccionados = nuevaReserva.ExtrasSeleccionadas?
                    .Where(e => e.IsSelected)
                    .Select(e => new { nombre = e.Nombre, precio = e.Precio })
            };

            var response = await SendRequestAsync<ApiResponse<object>>(
                HttpMethod.Post,
                "reservas",
                requestData);

            return response != null;
        }

        public static async Task<bool> ActualizarReservaAsync(Reserva reservaActualizada)
        {
            var updateData = new
            {
                huespedes = reservaActualizada.CantidadHuesped,
                fecha_entrada = reservaActualizada.Fecha_ini.ToString("yyyy-MM-dd"),
                fecha_salida = reservaActualizada.Fecha_fin.ToString("yyyy-MM-dd"),
                extrasSeleccionados = reservaActualizada.ExtrasSeleccionadas?
                    .Where(e => e.IsSelected)
                    .Select(e => new { nombre = e.Nombre, precio = e.Precio })
            };

            var response = await SendRequestAsync<ApiResponse<object>>(
                HttpMethod.Put,
                $"reservas/{reservaActualizada.Id}",
                updateData);

            return response != null;
        }

        public static async Task<List<Extras>> ObtenerExtrasAsync()
        {
            var response = await SendRequestAsync<ApiResponse<List<Extras>>>(
                HttpMethod.Get,
                "habitacion/extras");

            return response?.Data ?? new List<Extras>();
        }

        public static async Task<List<TipoCapacidad>> ObtenerTiposHabitacionAsync()
        {
            var response = await SendRequestAsync<ApiResponse<List<TipoCapacidad>>>(
                HttpMethod.Get,
                "habitacion/types");

            return response?.Data ?? new List<TipoCapacidad>();
        }

        public static async Task<List<Habitacion>> ObtenerHabitacionesDisponiblesAsync(DateTime fechaEntrada, DateTime fechaSalida)
        {
            var response = await SendRequestAsync<ApiResponse<List<Habitacion>>>(
                HttpMethod.Get,
                $"reservas/habitacion_disponible/?fecha_entrada={fechaEntrada:yyyy-MM-dd}&fecha_salida={fechaSalida:yyyy-MM-dd}");

            return response?.Data ?? new List<Habitacion>();
        }

        public static async Task<List<Usuario>> ObtenerClientesAsync()
        {
            var response = await SendRequestAsync<ApiResponse<List<Usuario>>>(
                HttpMethod.Get,
                "user/client");

            return response?.Data ?? new List<Usuario>();
        }

        public static async Task<List<Usuario>> ObtenerEmpleadosAsync()
        {
            var response = await SendRequestAsync<ApiResponse<List<Usuario>>>(
                HttpMethod.Get,
                "user/employee");

            return response?.Data ?? new List<Usuario>();
        }
    }

}
