using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using GalaSoft.MvvmLight.Command;
using Microsoft.Win32;
using WPFIntermodular.model;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace WPFIntermodular.viewmodel.habitacionViewModel
{
    public class addHabsViewModel : INotifyPropertyChanged
    {
        // Lista de tipos de habitación
        private readonly string[] _tiposHabitacion = { "Individual", "Estandar", "Lujo", "Suite", "Duplex" };
        public string[] TiposHabitacion => _tiposHabitacion;

        // Campos privados
        private bool _editar;
        private Habitacion _habitacion;
        private string _nombre;
        private string _tipo;
        private string _descripcion;
        private int _selectedTipoIndex;
        private BitmapImage _imagenPreview;
        private byte[] _imageBytes;
        private string imageb64;

        // Propiedad para el precio (enlazada al TextBox de Precio)
        public double Precio
        {
            get => _habitacion?.Precio ?? 0.0;
            set
            {
                if (_habitacion != null && _habitacion.Precio != value)
                {
                    _habitacion.Precio = value;
                    OnPropertyChanged(nameof(Precio));
                }
            }
        }

        // Propiedad para determinar si se está editando o creando
       

        // Objeto Habitacion (contiene ID, Capacidad, Extras, etc.)
        public Habitacion Habitacion
        {
            get => _habitacion;
            set => SetProperty(ref _habitacion, value);
        }

        // Propiedad para el nombre (enlazada al TextBox de Nombre)
        public string Nombre
        {
            get => _nombre;
            set => SetProperty(ref _nombre, value);
        }

        // Propiedad para el tipo (enlazada al ComboBox)
        public string Tipo
        {
            get => _tipo;
            set
            {
                if (_tipo != value)
                {
                    _tipo = value;
                    OnPropertyChanged(nameof(Tipo));

                    // Actualizamos el índice seleccionado en el ComboBox
                    int newIndex = Array.IndexOf(_tiposHabitacion, _tipo);
                    if (newIndex != -1 && newIndex != _selectedTipoIndex)
                    {
                        SelectedTipoIndex = newIndex;
                    }
                }
            }
        }

        public string Descripcion
        {
            get => _descripcion;
            set => SetProperty(ref _descripcion, value);
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
                        Tipo = _tiposHabitacion[_selectedTipoIndex];
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

        private bool Editar;
        public addHabsViewModel(Habitacion habitacion, bool editar)
        {
            Habitacion = habitacion;
            Editar = editar;

            _navigationService = new NavigationService();

            SeleccionarImagenCommand = new RelayCommand(SeleccionarImagen);
            GuardarHabitacionCommand = new RelayCommand(async () => await CrearHabitacionAsync());
            CerrarCommand = new RelayCommand(CerrarVentana);

            if (Editar)
            {
                Nombre = habitacion.Nombre;
                Tipo = habitacion.Tipo;
                Descripcion = habitacion.Descripccion; 
                SelectedTipoIndex = Array.IndexOf(_tiposHabitacion, habitacion.Tipo);
            }
            else
            {
                Nombre = string.Empty;
                Tipo = _tiposHabitacion[0];
                SelectedTipoIndex = 0;
                Descripcion = string.Empty;
            }
        }

        // Método para seleccionar imagen (abre diálogo y carga imagen en Base64 y vista previa)
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
                imageb64 = ConvertirA64(_imageBytes);
            }
        }

        // Convierte un arreglo de bytes a cadena en Base64
        private string ConvertirA64(byte[] imagenBytes)
        {
            string base64 = Convert.ToBase64String(imagenBytes);
            // Puedes quitar este MessageBox una vez que hayas depurado la conversión
            MessageBox.Show(base64);
            return base64;
        }

        // Convierte un arreglo de bytes a BitmapImage para la vista previa
        private BitmapImage ConvertirBytesAImagen(byte[] imagenBytes)
        {
            if (imagenBytes == null || imagenBytes.Length == 0)
                return null;

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

        // Método asíncrono para crear/guardar la habitación
        public async Task<Habitacion> CrearHabitacionAsync()
        {

            if (Editar)
            {
                bool result = comps();
                if (result)
                {
                    ConexionController c = new ConexionController();
                    Habitacion nuevaHabitacion = new Habitacion(
                        Habitacion.Id,
                        Tipo,
                        Habitacion.Capacidad,
                        Precio, // Utilizamos la propiedad Precio
                        new Dictionary<string, double>(Habitacion.Extras), // Conversión de extras si es necesario
                        imageb64, // Imagen en Base64
                        Nombre,
                        Descripcion);
                    bool resultado = await c.editHab(nuevaHabitacion);
                    if (resultado)
                    {
                        //buscarHabitacionViewModel.instance.AgregarHabitacion(nuevaHabitacion);

                    }
                    else
                    {
                        MessageBox.Show("Ha habido un problema al editar la habitación.");
                    }
                    return resultado ? nuevaHabitacion : null;
                }
                return null;

            }
            else
            {

                try
                {
                    bool result = comps();
                    if (result)
                    {
                        Habitacion nuevaHabitacion = new Habitacion(
                                            Habitacion.Id,
                                            Tipo,
                                            Habitacion.Capacidad,
                                            Precio, // Utilizamos la propiedad Precio
                                            new Dictionary<string, double>(Habitacion.Extras), // Conversión de extras si es necesario
                                            imageb64, // Imagen en Base64
                                            Nombre,
                                            Descripcion
                                        );

                        MessageBox.Show("Procesando creación de habitación...");

                        // Se asume que tienes un controlador de conexión para guardar la habitación
                        ConexionController c = new ConexionController();
                        bool resultado = await c.GuardarHab(nuevaHabitacion);

                        if (resultado)
                        {
                            buscarHabitacionViewModel.instance.AgregarHabitacion(nuevaHabitacion);

                        }
                        else
                        {
                            MessageBox.Show("Ha habido un problema al guardar la habitación.");
                        }

                        return resultado ? nuevaHabitacion : null;
                    }

                    return null;


            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al crear la habitación: {ex.Message}");
                return null;
            }
            }
        }

        // Método para cerrar la ventana (o navegar a otra vista)

        private bool comps()
        {
        
            // Validar ID: debe comenzar con 'H' seguido de al menos 3 dígitos
            if (string.IsNullOrWhiteSpace(Habitacion.Id) || !Regex.IsMatch(Habitacion.Id, @"^H\d{3,}$"))
            {
                MessageBox.Show("Error: ID inválido. Debe comenzar con 'H' seguido de números.");
                return false;
            }

            // Validar Tipo
            string[] tiposValidos = _tiposHabitacion;
            if (string.IsNullOrEmpty(Tipo) || !tiposValidos.Contains(Tipo, StringComparer.OrdinalIgnoreCase))
            {
                MessageBox.Show("Error: Tipo de habitación no válido.");
                return false;
            }

            // Validar capacidad
            if (Habitacion.Capacidad <= 0)
            {
                MessageBox.Show("Error: La capacidad debe ser mayor a 0.");
                return false;
            }

            // Validar precio
            if (Precio <= 0)
            {
                MessageBox.Show("Error: El precio debe ser un número positivo.");
                return false;
            }

            // Validar extras (si existen)
            if (Habitacion.Extras != null && Habitacion.Extras.Any(e => e.Value <= 0))
            {
                MessageBox.Show("Error: Todos los extras deben tener un precio mayor a 0.");
                return false;
            }

            // Validar imagen
            if (string.IsNullOrEmpty(imageb64))
            {
                MessageBox.Show("Error: Debe agregar al menos una imagen válida.");
                return false;
            }

            // Validar nombre
            if (string.IsNullOrWhiteSpace(Nombre))
            {
                MessageBox.Show("Error: El nombre de la habitación no puede estar vacío.");
                return false;
            }

            // Validar descripción
            if (string.IsNullOrWhiteSpace(Descripcion) || Descripcion.Length < 10)
            {
                MessageBox.Show("Error: La descripción debe tener al menos 10 caracteres.");
                return false;
            }

            return true;
        }
        public void CerrarVentana()
        {
            _navigationService.NavigateTo("buscarHab");
        }

        // Implementación de INotifyPropertyChanged
        public event PropertyChangedEventHandler PropertyChanged;
        protected void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        // Método auxiliar para actualizar propiedades y notificar cambios
        private void SetProperty<T>(ref T field, T value, [System.Runtime.CompilerServices.CallerMemberName] string propertyName = null)
        {
            if (!Equals(field, value))
            {
                field = value;
                OnPropertyChanged(propertyName);
            }
        }
    }
}