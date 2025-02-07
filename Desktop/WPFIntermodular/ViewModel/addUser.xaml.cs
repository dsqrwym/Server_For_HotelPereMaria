using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
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
    /// Interaction logic for addUser.xaml
    /// </summary>
    public partial class addUser : Window
    {
        public Usuario nuevoUsuario { get; private set; }
        public int idfinal;
        public addUser(int nuevoId)
        {
            InitializeComponent();
            idfinal = nuevoId;
        }
        private void Button_Click(object sender, RoutedEventArgs e)
        {
            string name;
            string surname;
            string fullName = null;
            int role = 0;
            string email;
            string dni;
            string birthDate;
            bool isValid = true;
            string errorMessages = "";
            string tel;

            if (roleBox.SelectedIndex != -1)
            {
                role = roleBox.SelectedIndex;
            }
            else
            {
                isValid = false;
                errorMessages += "Rol vacío\n";
            }


            name = checkName();
            if (name == null)
            {
                isValid = false;
                errorMessages += "El nombre no puede estar vacio.\n";
            }

            surname = checkSurname();
            if (surname == null)
            {
                isValid = false;
                errorMessages += "El apellido no puede estar vacio.\n";
            }

            if (name != null && surname != null)
            {
                fullName = name + " " + surname;
            }

            email = checkEmail();
            if (email == null)
            {
                isValid = false;
                errorMessages += "El email no puede estar vacio.\n";
            }

            dni = checkDni();
            if (dni == null)
            {
                isValid = false;
                errorMessages += "El dni no puede estar vacio.\n";
            }

            birthDate = birthField.Text;
            if (birthDate == null)
            {
                isValid = false;
                errorMessages += "La fecha de nacimiento no puede estar vacia.\n";
            }

            tel = checkTlf();
            if (tel == null)
            {
                isValid = false;
                errorMessages += "El telefono no puede estar vacio.\n";
            }

            if (!isValid)
            {
                MessageBox.Show("Errores detectados:\n" + errorMessages, "Validacion", MessageBoxButton.OK, MessageBoxImage.Warning);
            }
            else
            {
                MessageBox.Show($"Todos los datos son correctos, display:\nNombre completo: {fullName}\nRol: {role}\nEmail: {email}\nDni: {dni}\nFecha de nacimiento: {birthDate}",
                                "Datos validos", MessageBoxButton.OK, MessageBoxImage.Information);

                string roleStr = null;

                if (role == 0) { roleStr = "Administrador"; }
                if (role == 1) { roleStr = "Empleado"; }
                if (role == 2) { roleStr = "Cliente"; }

                nuevoUsuario = new Usuario
                {
                    id = idfinal.ToString(),
                    nombre = fullName,
                    rol = roleStr,
                    email = email,
                    dni = dni,
                    fechaNac = birthDate
                };

                DialogResult = true;
                Close();
            }
        }

        private string checkName()
        {

            string name = nameField.Text;

            if (name == null)
            {
                return null;
            }

            return name;
        }

        private string checkSurname()
        {

            string surname = surnameField.Text;

            if (surname == null)
            {
                return null;
            }

            return surname;
        }

        private string checkEmail()
        {

            string email = emailField.Text;

            if (email == null || !email.Contains("@") || !email.Contains("."))
            {
                return null;
            }

            return email;
        }

        private string checkDni()
        {

            string dni = dniField.Text;
            string pattern = @"^\d{8}[A-Z]$";

            if (Regex.IsMatch(dni, pattern))
            {
                return dni;
            }
            return null;
        }

        private string checkTlf()
        {

            string tlf = phoneField.Text;
            bool res = tlf.All(Char.IsDigit);

            if (tlf == null || res == false)
            {
                return null;
            }

            return tlf;
        }
    }
}
