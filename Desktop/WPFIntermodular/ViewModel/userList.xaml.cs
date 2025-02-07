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

namespace WPFIntermodular.ViewModel
{
    /// <summary>
    /// Interaction logic for userList.xaml
    /// </summary>
    public partial class userList : Window
    {
        private ObservableCollection<Usuario> usuarios;
        private ICollectionView vistaUsuarios;
        public userList()
        {
            InitializeComponent();
            usuarios = new ObservableCollection<Usuario>
            {
                new Usuario { id = "0", nombre = "Iván Nieto", rol = "Administrador", email="ivanivienen@gmail.com", fechaNac="0/0/0", dni="84321059K" },
                new Usuario { id = "1", nombre= "Kliman", rol= "Cliente", email="k@k.kom", fechaNac="0/0/0", dni="28394573K" },
                new Usuario { id = "2", nombre= "Carlos", rol= "Empleado", email="carlos@mail.com", fechaNac="5/5/1990", dni="12345678Z" }
            };
            vistaUsuarios = CollectionViewSource.GetDefaultView(usuarios);
            UserListView.ItemsSource = vistaUsuarios;
        }
        private void FiltroRolComboBox_SelectionChanged(object sender, System.Windows.Controls.SelectionChangedEventArgs e)
        {
            if (vistaUsuarios != null)
            {
                string filtro = (FiltroRolComboBox.SelectedItem as ComboBoxItem)?.Content.ToString();

                vistaUsuarios.Filter = usuario =>
                {
                    var u = usuario as Usuario;
                    return filtro == "Todos" || u.rol == filtro;
                };

                vistaUsuarios.Refresh(); // Actualizar la lista
            }
        }

        private void filterByID_TextChanged(object sender, System.Windows.Controls.TextChangedEventArgs e)
        {
            if (vistaUsuarios != null)
            {
                string filtro = filterByID.Text.Trim();

                vistaUsuarios.Filter = usuario =>
                {
                    var u = usuario as Usuario;
                    return string.IsNullOrEmpty(filtro) || u.id.StartsWith(filtro);
                };
                vistaUsuarios.Refresh();
            }
        }

        private void detallesBtn_Click(object sender, RoutedEventArgs e)
        {
            if (UserListView.SelectedItem is Usuario usuarioSeleccionado)
            {
                string mensaje = $"ID: {usuarioSeleccionado.id}\n" +
                                 $"Nombre: {usuarioSeleccionado.nombre}\n" +
                                 $"Rol: {usuarioSeleccionado.rol}\n" +
                                 $"DNI: {usuarioSeleccionado.dni}\n" +
                                 $"Email: {usuarioSeleccionado.email}\n" +
                                 $"Fecha Nacimiento: {usuarioSeleccionado.fechaNac}";

                MessageBox.Show(mensaje, "Detalles del Usuario", MessageBoxButton.OK, MessageBoxImage.Information);
            }
            else
            {
                MessageBox.Show("Por favor, selecciona un usuario de la lista.", "Error", MessageBoxButton.OK, MessageBoxImage.Warning);
            }
        }

        private void updateBtn_Click(object sender, RoutedEventArgs e)
        {
            if (UserListView.SelectedItem is Usuario usuarioSeleccionado)
            {
                updateUser ventanaActualizar = new updateUser(usuarioSeleccionado);
                if (ventanaActualizar.ShowDialog() == true)
                {
                    vistaUsuarios.Refresh();
                }
            }
            else
            {
                MessageBox.Show("Por favor, selecciona un usuario para actualizar.", "Error", MessageBoxButton.OK, MessageBoxImage.Warning);
            }
        }

        private void anyadirBtn_Click(object sender, RoutedEventArgs e)
        {
            int nuevoId = usuarios.Any() ? usuarios.Max(u => int.Parse(u.id)) + 1 : 0;
            addUser newUser = new addUser(nuevoId);
            newUser.ShowDialog();
            usuarios.Add(newUser.nuevoUsuario);
        }
    }
}