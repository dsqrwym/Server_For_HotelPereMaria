using System;
using System.Collections.Generic;
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

namespace WPFIntermodular.vistaUsuario
{
    /// <summary>
    /// Interaction logic for updateUser.xaml
    /// </summary>
    public partial class updateUser : Window
    {
        public Usuario UsuarioSeleccionado { get; private set; }
        public updateUser(Usuario usuario)
        {
            InitializeComponent();
            UsuarioSeleccionado = usuario;
            IDTextBox.Text = usuario.id;
            NombreTextBox.Text = usuario.nombre;
            DNITextBox.Text = usuario.dni;
            EmailTextBox.Text = usuario.email;
            FechaNacTextBox.Text = usuario.fechaNac;

            RolComboBox.SelectedItem = RolComboBox.Items
                .Cast<ComboBoxItem>()
                .FirstOrDefault(item => item.Content.ToString() == usuario.rol);
        }
        private void GuardarCambios_Click(object sender, RoutedEventArgs e)
        {
            UsuarioSeleccionado.nombre = NombreTextBox.Text;
            UsuarioSeleccionado.dni = DNITextBox.Text;
            UsuarioSeleccionado.email = EmailTextBox.Text;
            UsuarioSeleccionado.fechaNac = FechaNacTextBox.Text;

            if (RolComboBox.SelectedItem is ComboBoxItem selectedItem)
            {
                UsuarioSeleccionado.rol = selectedItem.Content.ToString();
            }

            DialogResult = true;
            Close();
        }
    }
}
