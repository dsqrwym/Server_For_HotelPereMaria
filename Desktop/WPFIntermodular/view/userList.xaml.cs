using GalaSoft.MvvmLight.Command;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using WPFIntermodular.model;

namespace WPFIntermodular.view
{
    /// <summary>
    /// Interaction logic for userList.xaml
    /// </summary>
    public partial class userList : Window
    {
        private ObservableCollection<Usuario> usuarios;
        private ObservableCollection<Usuario> UsuariosFiltrados { get; set; }
        private ICollectionView vistaUsuarios;

        List<Usuario> listaUsuarios = new List<Usuario>();

        private NavigationService _navigationService;

        private bool editar = false;
        private Usuario u;
        private ConexionApi apiConexion;


        private RelayCommand NavigateToSecondWindowCommand { get; set; }
        public userList()
        {
            InitializeComponent();
            _navigationService = new NavigationService();
            NavigateToSecondWindowCommand = new RelayCommand(nav);
            CargarUsuarios();

            vistaUsuarios = CollectionViewSource.GetDefaultView(usuarios);
            UserListView.ItemsSource = vistaUsuarios;
        }
        private void FiltroRolComboBox_SelectionChanged(object sender, System.Windows.Controls.SelectionChangedEventArgs e)
        {
            
                int rol = FiltroRolComboBox.SelectedIndex;
                FiltrarPorRol(rol);
          

        }
        private void FiltrarPorRol(int rolSeleccionado)
        {
            MessageBox.Show("Filtrando "+rolSeleccionado);
            if (rolSeleccionado == 0)
            {
                CargarUsuarios();
                vistaUsuarios = CollectionViewSource.GetDefaultView(usuarios);
                UserListView.ItemsSource = vistaUsuarios;
            }
            else
            {

                List<Usuario> listaUsuariosF = new List<Usuario>();

                foreach (Usuario u in listaUsuarios)
                {
                    if (!string.IsNullOrEmpty(u.rol) && int.TryParse(u.rol, out int rol) && rol == rolSeleccionado)
                    {
                        listaUsuariosF.Add(u);
                    }
                }

                UsuariosFiltrados = new ObservableCollection<Usuario>(listaUsuariosF);

                vistaUsuarios = CollectionViewSource.GetDefaultView(UsuariosFiltrados);
                UserListView.ItemsSource = vistaUsuarios;
            }
        }

        private async void CargarUsuarios()
        {

             apiConexion = new ConexionApi();
            listaUsuarios = await apiConexion.AllUsuarios();
            
            if (listaUsuarios != null)
            {
                UserListView.ItemsSource = listaUsuarios;
            }
        }

        private void filterByID_TextChanged(object sender, System.Windows.Controls.TextChangedEventArgs e)
        {
            List<Usuario> listaUsuariosF = new List<Usuario>();

            foreach (Usuario u in listaUsuarios)
            {
                bool cumpleFiltroRol = (!string.IsNullOrEmpty(u.rol) && int.TryParse(u.rol, out int rol) && rol == FiltroRolComboBox.SelectedIndex) || FiltroRolComboBox.SelectedIndex == 0;
                bool cumpleFiltroNombre = string.IsNullOrEmpty(filterByID.Text) ||
                          (!string.IsNullOrEmpty(u.nombre) &&
                           u.nombre.IndexOf(filterByID.Text, StringComparison.OrdinalIgnoreCase) >= 0);


                if (cumpleFiltroRol && cumpleFiltroNombre)
                {
                    listaUsuariosF.Add(u);
                }
            }

            UsuariosFiltrados = new ObservableCollection<Usuario>(listaUsuariosF);
            vistaUsuarios = CollectionViewSource.GetDefaultView(UsuariosFiltrados);
            UserListView.ItemsSource = vistaUsuarios;
        }


        private void anyadirBtn_Click(object sender, RoutedEventArgs e)
        {
            addUser newUser = new addUser();
            newUser.ShowDialog();
        }

        private void nav()
        {

            if (editar)
            {
                this.Hide();
                _navigationService.NavigateTo("addUser");
            }
            else
            {
                this.Hide();
                _navigationService.NavigateTo("updateUser",u);

            }

        }

        private void updateBtn_Click(object sender, RoutedEventArgs e)
        {
            ConexionApi conexion = new ConexionApi();
            Usuario? u = UserListView.SelectedItem as Usuario;
            updateUser us = new updateUser(u,conexion);
            us.ShowDialog();

            CargarUsuarios();
        }

        private void detallesBtn_Click(object sender, RoutedEventArgs e)
        {
            if (UserListView.SelectedItem is Usuario usuario) {
                string mensaje = $"ID: {usuario.id}\n" +
                         $"Nombre: {usuario.nombre}\n" +
                         $"Apellido: {usuario.apellido}\n" +
                         $"DNI: {usuario.dni}\n" +
                         $"Rol: {usuario.rol}\n" +
                         $"Email: {usuario.email}\n" +
                    $"Telefono:{usuario.telefono}\n";

                MessageBox.Show(mensaje, "Detalles del Usuario", MessageBoxButton.OK, MessageBoxImage.Information);
            }
        }

        private void filtrarBtn_Click(object sender, RoutedEventArgs e)
        {

        }
    }
}