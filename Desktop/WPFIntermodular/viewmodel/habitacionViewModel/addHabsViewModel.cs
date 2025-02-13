using System;
using System.ComponentModel;
using System.IO;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using Microsoft.Win32;
using GalaSoft.MvvmLight.Command;
using WPFIntermodular.model;
using System.Text.RegularExpressions;

namespace WPFIntermodular.viewmodel.habitacionViewModel
{
    public class addHabsViewModel : INotifyPropertyChanged
    {
        private readonly string[] _tiposHabitacion = { "Individual", "Estandar", "Lujo", "Suite", "Duplex" };

        public string[] TiposHabitacion => _tiposHabitacion;

        private bool _editar;
        private Habitacion _habitacion;
        private string _nombre;
        private string _tipo;
        private string _descripcion;
        private int _selectedTipoIndex;
        private BitmapImage _imagenPreview;
        private byte[] _imageBytes;

        public double Precio
        {
            get => Habitacion?.Precio ?? 0.0;
            set
            {
                if (Habitacion != null && Habitacion.Precio != value)
                {
                    Habitacion.Precio = value;
                    OnPropertyChanged(nameof(Precio));
                }
            }
        }

        public event PropertyChangedEventHandler? PropertyChanged;

        private string imageb64;
        public bool Editar
        {
            get => _editar;
            set => SetProperty(ref _editar, value);
        }

        public Habitacion Habitacion
        {
            get => _habitacion;
            set => SetProperty(ref _habitacion, value);
        }

        public string Nombre
        {
            get => _nombre;
            set => SetProperty(ref _nombre, value);
        }

        public string Tipo
        {
            get => _tipo;
            set
            {
                if (_tipo != value)
                {
                    _tipo = value;
                    OnPropertyChanged(nameof(Tipo));

                    int newIndex = Array.IndexOf(_tiposHabitacion, _tipo);
                    if (newIndex != -1 && newIndex != _selectedTipoIndex)
                    {
                        _selectedTipoIndex = newIndex;
                        OnPropertyChanged(nameof(SelectedTipoIndex));
                    }
                }
            }
        }

        public string Descripcion
        {
            get => _descripcion;
            set => SetProperty(ref _descripcion, value);
        }

        protected void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        public int SelectedTipoIndex
        {
            get => _selectedTipoIndex;
            set
            {
                if (_selectedTipoIndex != value)
                {
                    _selectedTipoIndex = value;
                    OnPropertyChanged(nameof(SelectedTipoIndex));

                    if (_selectedTipoIndex >= 0 && _selectedTipoIndex < _tiposHabitacion.Length)
                    {
                        _tipo = _tiposHabitacion[_selectedTipoIndex];
                        OnPropertyChanged(nameof(Tipo)); // Notificar cambio en Tipo
                    }
                }
            }
        }

        public BitmapImage ImagenPreview
        {
            get => _imagenPreview;
            set => SetProperty(ref _imagenPreview, value);
        }

        public ICommand SeleccionarImagenCommand { get; }
        public ICommand GuardarHabitacionCommand { get; }
        public ICommand CerrarCommand { get; }


        private NavigationService _navigationService;


        private RelayCommand NavigateToSecondWindowCommand { get; set; }

        public addHabsViewModel(Habitacion habitacion, bool editar)
        {
            Habitacion = habitacion;
            Editar = editar;

            _navigationService = new NavigationService();
            NavigateToSecondWindowCommand = new RelayCommand(CerrarVentana);

            if (Editar)
            {
                Nombre = habitacion.Nombre;
                Tipo = habitacion.Tipo;
                Descripcion = habitacion.Descripccion;
                SelectedTipoIndex = Array.IndexOf(_tiposHabitacion, habitacion.Tipo);
            }

            

            SeleccionarImagenCommand = new RelayCommand(SeleccionarImagen);
            CerrarCommand = new RelayCommand(CerrarVentana);
            GuardarHabitacionCommand = new RelayCommand(async () => await CrearHabitacionAsync());
        }

        private void SeleccionarImagen()
        {
            OpenFileDialog openFileDialog = new OpenFileDialog
            {
                Title = "Seleccionar archivo de imagen",
                Filter = "Imágenes|*.jpg;*.png;*.gif",
                Multiselect = false
            };

            if (openFileDialog.ShowDialog() == true)
            {
                _imageBytes = File.ReadAllBytes(openFileDialog.FileName);
                ImagenPreview = ConvertirBytesAImagen(_imageBytes);

                imageb64 = convetir64(_imageBytes);
            }
        }

        private string convetir64(byte[] imagenBytes)
        {
            MessageBox.Show(Convert.ToBase64String(imagenBytes));
            return Convert.ToBase64String(imagenBytes);
        }

        private BitmapImage ConvertirBytesAImagen(byte[] imagenBytes)
        {
            if (imagenBytes == null || imagenBytes.Length == 0) return null;

            using (var stream = new MemoryStream(imagenBytes))
            {
                var bitmap = new BitmapImage();
                bitmap.BeginInit();
                bitmap.StreamSource = stream;
                bitmap.CacheOption = BitmapCacheOption.OnLoad;
                bitmap.EndInit();
                return bitmap;
            }
        }

        public async Task<Habitacion> CrearHabitacionAsync()
        {
            try
            {

                if (string.IsNullOrWhiteSpace(Habitacion.Id) || !Regex.IsMatch(Habitacion.Id, @"^H\d{3,}$"))
                {
                    MessageBox.Show("Error: ID inválido. Debe comenzar con 'H' seguido de números.");
                    return null;
                }

                string[] tiposValidos = { "Individual", "Estandar", "Lujo", "Suite", "Duplex" };
                if (string.IsNullOrEmpty(Tipo) || !tiposValidos.Contains(Tipo, StringComparer.OrdinalIgnoreCase))
                {
                    MessageBox.Show("Error: Tipo de habitación no válido.");
                    return null;
                }

                if (Habitacion.Capacidad <= 0)
                {
                    MessageBox.Show("Error: La capacidad debe ser mayor a 0.");
                    return null;
                }

                if (Precio <= 0)
                {
                    MessageBox.Show("Error: El precio debe ser un número positivo.");
                    return null;
                }

                if (Habitacion.Extras != null && Habitacion.Extras.Any(e => e.Value <= 0))
                {
                    MessageBox.Show("Error: Todos los extras deben tener un precio mayor a 0.");
                    return null;
                }

                if (string.IsNullOrEmpty(imageb64))
                {
                    MessageBox.Show("Error: Debe agregar al menos una imagen válida.");
                    return null;
                }

                if (string.IsNullOrWhiteSpace(Nombre))
                {
                    MessageBox.Show("Error: El nombre de la habitación no puede estar vacío.");
                    return null;
                }

                if (string.IsNullOrWhiteSpace(Descripcion) || Descripcion.Length < 10)
                {
                    MessageBox.Show("Error: La descripción debe tener al menos 10 caracteres.");
                    return null;
                }

                Habitacion nuevaHabitacion = new Habitacion(
                           Habitacion.Id,
                           Tipo,
                           Habitacion.Capacidad,
                           200, // Directamente asignamos el precio como double
                           new Dictionary<string, double>(Habitacion.Extras), // Cambiamos de MongoDecimal a double
                           imageb64, // Imagen convertida a Base64
                           Nombre,
                           Descripcion
                       );

                MessageBox.Show($"Pedro Sancheeeez");

                ConexionController c = new ConexionController();
                bool resultado = await c.GuardarHab(nuevaHabitacion);

                if (resultado)
                {
                    MessageBox.Show($"Habitación '{nuevaHabitacion.Nombre}' creada con éxito.");
                }
                else
                {
                    MessageBox.Show("Ha habido un problema");
                }

                return resultado ? nuevaHabitacion : null;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al crear la habitación: {ex.Message}");
                return null;
            }
        }

        private void CerrarVentana()
        {
            MessageBox.Show("Cerrando ventana...");
            _navigationService.NavigateTo("buscarHab");
        }

        private void SetProperty<T>(ref T field, T value, [System.Runtime.CompilerServices.CallerMemberName] string propertyName = null)
        {
            if (!Equals(field, value))
            {
                field = value;
                PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }
    }
}