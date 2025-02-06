using System;
using System.Windows;

using WPFIntermodular;
using WPFIntermodular.vistaUsuario;

namespace WPFIntermodular
{
    public class NavigationService
    {
        // Método para navegar a una nueva ventana
        public void NavigateTo(string windowName)
        {
            // Resolver el tipo de ventana a partir del nombre
            Type windowType = GetWindowTypeByName(windowName);

            if (windowType != null)
            {
                // Crear una instancia de la ventana
                Window newWindow = (Window)Activator.CreateInstance(windowType);
                newWindow.Show();  // Mostrar la ventana
            }
            else
            {
                MessageBox.Show("Ventana no encontrada: " + windowName);
            }
        }

        // Método para obtener el tipo de ventana a partir de su nombre
        private Type GetWindowTypeByName(string windowName)
        {
            switch (windowName)
            {
                case "login":
                    return typeof(loginScreen);  // Tipo de la ventana principal
                case "buscarReserva":
                    return typeof(buscadorHabitaciones);  // Tipo de la segunda ventana
                // Agregar más ventanas según sea necesario
                default:
                    return null;
            }
        }
    }
}