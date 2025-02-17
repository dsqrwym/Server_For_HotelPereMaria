using Newtonsoft.Json;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Runtime.CompilerServices;
using System.Windows.Media.Imaging;
using System.Windows.Media;

namespace WPFIntermodular.model.reserva
{

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
        public Usuario Usuario
        {
            get => usuario;
            set
            {
                usuario = value;
                OnPropertyChanged();
            }
        }

        [JsonProperty("id_habitacion")]
        private Habitacion habitacion;
        public Habitacion Habitacion
        {
            get => habitacion;
            set
            {
                habitacion = value;
                OnPropertyChanged();
            }
        }

        [JsonProperty("huespedes")]
        private int cantidadHuesped;
        public int CantidadHuesped
        {
            get => cantidadHuesped;
            set
            {
                cantidadHuesped = value;
                OnPropertyChanged();
            }
        }

        [JsonProperty("fecha_entrada")]
        private DateTime fecha_ini;
        public DateTime Fecha_ini
        {
            get => fecha_ini;
            set
            {
                fecha_ini = value;
                OnPropertyChanged();
            }
        }

        [JsonProperty("fecha_salida")]
        private DateTime fecha_fin;
        public DateTime Fecha_fin
        {
            get => fecha_fin;
            set
            {
                fecha_fin = value;
                OnPropertyChanged();
            }
        }

        [JsonProperty("precio_total")]
        private decimal precio_total;
        public decimal Precio_total
        {
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
            set
            {
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
        public string _id { get; set; }
        [JsonProperty("Id")]
        public string Id { get; set; }

        [JsonProperty("Tipo")]
        public string Tipo { get; set; }
        [JsonProperty("Capacidad")]
        public int Capacidad { get; set; }
        [JsonProperty("Images")]
        public string ImagesUrl { get; set; }
        public ImageSource ImageSource
        {
            get
            {
                return ConvertBase64ToImage(ImagesUrl);
            }
        }

        [JsonProperty("Extras")]
        public Dictionary<string, decimal> extrasDict { get; set; }

        public List<Extras> ExtrasList
        {
            get
            {
                return extrasDict?.Select(kv => new Extras { Nombre = kv.Key, Precio = kv.Value }).ToList();
            }
            set
            {
                if (value != null)
                {
                    extrasDict = value.ToDictionary(e => e.Nombre, e => e.Precio);
                    OnPropertyChanged();
                }
            }
        }

        [JsonProperty("Precio")]
        public decimal PrecioNoche { get; set; }
        public event PropertyChangedEventHandler PropertyChanged;
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    
        private ImageSource ConvertBase64ToImage(string base64String)
        {
            if (string.IsNullOrEmpty(base64String))
                return null;

            try
            {
                byte[] imageBytes = Convert.FromBase64String(base64String);
                using (var ms = new MemoryStream(imageBytes))
                {
                    BitmapImage bitmap = new BitmapImage();
                    bitmap.BeginInit();
                    bitmap.CacheOption = BitmapCacheOption.OnLoad;
                    bitmap.StreamSource = ms;
                    bitmap.EndInit();
                    return bitmap;
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error converting Base64 to Image: {ex.Message}");
                return null;
            }
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
        private string _nombre;
        public string Nombre {
            get => _nombre;
            set
            {
                _nombre = value;
                OnPropertyChanged(nameof(Nombre));
            }
        }

        [JsonProperty("precio")]
        private decimal _precio;
        public decimal Precio {
            get => _precio;
            set
            {
                _precio = value;
                OnPropertyChanged(nameof(Precio));
            }
        }

        private bool _isSelected;
        public bool IsSelected
        {
            get => _isSelected;
            set
            {
                _isSelected = value;
                OnPropertyChanged(nameof(IsSelected));
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}