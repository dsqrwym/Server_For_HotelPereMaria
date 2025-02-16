using GalaSoft.MvvmLight.Command;
using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows.Input;
using WPFIntermodular.model;

namespace WPFIntermodular.viewModel
{
    public class UpdateUserViewModel : INotifyPropertyChanged
    {
        private Usuario _usuario;
        private ConexionApi _conexionApi;
        private string _rolSeleccionado;

        public Usuario Usuario
        {
            get => _usuario;
            set
            {
                _usuario = value;
                OnPropertyChanged();
            }
        }

        public string Nombre
        {
            get => _usuario.nombre;
            set
            {
                if (_usuario.nombre != value)
                {
                    _usuario.nombre = value;
                    OnPropertyChanged();
                }
            }
        }

        public string Apellido
        {
            get => _usuario.apellido;
            set
            {
                if (_usuario.apellido != value)
                {
                    _usuario.apellido = value;
                    OnPropertyChanged();
                }
            }
        }

        public string DNI
        {
            get => _usuario.dni;
            set
            {
                if (_usuario.dni != value)
                {
                    _usuario.dni = value;
                    OnPropertyChanged();
                }
            }
        }

        public string Email
        {
            get => _usuario.email;
            set
            {
                if (_usuario.email != value)
                {
                    _usuario.email = value;
                    OnPropertyChanged();
                }
            }
        }

        public string RolSeleccionado
        {
            get => _rolSeleccionado;
            set
            {
                if (_rolSeleccionado != value)
                {
                    _rolSeleccionado = value;
                    _usuario.rol = ConvertirRol(_rolSeleccionado);
                    OnPropertyChanged();
                }
            }
        }

        public ICommand GuardarCambiosCommand { get; }

        public event PropertyChangedEventHandler PropertyChanged;

        public UpdateUserViewModel(Usuario usuario, ConexionApi conexionApi)
        {
            _usuario = usuario ?? throw new ArgumentNullException(nameof(usuario));
            _conexionApi = conexionApi ?? throw new ArgumentNullException(nameof(conexionApi));

            RolSeleccionado = ConvertirRolInverso(_usuario.rol);
            GuardarCambiosCommand = new RelayCommand(async () => await GuardarCambiosAsync());
        }

        protected void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        public Action CloseWindowAction { get; set; }

        private async Task GuardarCambiosAsync()
        {
            bool resultado = await _conexionApi.ReemplazarUsuarioAsync(_usuario.id, _usuario);

            if (resultado)
            {
                Console.WriteLine("Usuario actualizado con éxito");
                CloseWindowAction?.Invoke();
            }
            else
            {
                Console.WriteLine("Error al actualizar usuario");
            }
        }

        private void CerrarVentana()
        {
            foreach (var window in System.Windows.Application.Current.Windows)
            {
                if (window is System.Windows.Window win && win.DataContext == this)
                {
                    win.Close();
                    break;
                }
            }
        }

        private string ConvertirRol(string rolTexto)
        {
            return rolTexto switch
            {
                "Administrador" => "1",
                "Usuario" => "3",
                "Invitado" => "5",
                _ => "1"
            };
        }

        private string ConvertirRolInverso(string rolNumero)
        {
            return rolNumero switch
            {
                "1" => "Administrador",
                "3" => "Usuario",
                "5" => "Invitado",
                _ => "Administrador"
            };
        }
    }
}
