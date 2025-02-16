using System;
using System.ComponentModel;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using Newtonsoft.Json;
using WPFIntermodular.model;

public class AddUserViewModel : INotifyPropertyChanged
{
    private string _nombre, _apellido, _email, _dni, _password, _username, _telefono, _rol;
    private readonly ConexionApi _apiService = new ConexionApi();

    public string Nombre { get => _nombre; set { _nombre = value; OnPropertyChanged(nameof(Nombre)); } }
    public string Apellido { get => _apellido; set { _apellido = value; OnPropertyChanged(nameof(Apellido)); } }
    public string Email { get => _email; set { _email = value; OnPropertyChanged(nameof(Email)); } }
    public string Dni { get => _dni; set { _dni = value; OnPropertyChanged(nameof(Dni)); } }
    public string Password { get => _password; set { _password = value; OnPropertyChanged(nameof(Password)); } }
    public string Username { get => _username; set { _username = value; OnPropertyChanged(nameof(Username)); } }
    public string Telefono { get => _telefono; set { _telefono = value; OnPropertyChanged(nameof(Telefono)); } }
    public string Rol { get => _rol; set { _rol = value; OnPropertyChanged(nameof(Rol)); } }

    public Action CloseWindowAction { get; set; }
    public ICommand EnviarCommand { get; }

    public event PropertyChangedEventHandler PropertyChanged;

    public AddUserViewModel()
    {
        EnviarCommand = new RelayCommand(async () => await EnviarAsync());
    }

    private async Task EnviarAsync()
    {
        MessageBox.Show("procesando");
        try
        {
            Usuario nuevoUsuario = new Usuario
            {
                nombre = Nombre,
                apellido = Apellido,
                email = Email,
                dni = Dni,
                password = Password,
                telefono = Telefono,
                rol = Rol
            };

            await _apiService.CrearUsuarioAsync(nuevoUsuario);
            MessageBox.Show("Usuario creado exitosamente.", "Éxito", MessageBoxButton.OK, MessageBoxImage.Information);
            CloseWindowAction?.Invoke();
        }
        catch (Exception ex)
        {
            MessageBox.Show($"Error al añadir usuario: {ex.Message}", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            CloseWindowAction?.Invoke();
        }
        CloseWindowAction?.Invoke();
    }

    private bool CanEnviar()
    {
        return !string.IsNullOrEmpty(Nombre) && !string.IsNullOrEmpty(Apellido) &&
               !string.IsNullOrEmpty(Email) && !string.IsNullOrEmpty(Dni) &&
               !string.IsNullOrEmpty(Password) && !string.IsNullOrEmpty(Username) &&
               !string.IsNullOrEmpty(Telefono) && !string.IsNullOrEmpty(Rol);
    }

    protected void OnPropertyChanged(string propertyName)
    {
        PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
    }
}

