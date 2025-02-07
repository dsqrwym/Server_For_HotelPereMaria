using System;
using System.Windows;

using WPFIntermodular;

namespace WPFIntermodular
{
    public class NavigationService
    {
        // Método para navegar a una nueva ventana
        public void NavigateTo(string windowName, params object[] parameters)
        {
            
            // Resolver el tipo de ventana a partir del nombre
            Type windowType = GetWindowTypeByName(windowName);

            if (windowType != null)
            {
                // Crear una instancia de la ventana
                Window newWindow = (Window)Activator.CreateInstance(windowType, parameters);
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
                    return typeof(loginScreen);  
                case "buscarReserva":
                    return typeof(BuscarReserva);
                case "addHab":
                    return typeof(addHabitaciones);
                case "addUser":
                    return typeof(addUser);
                case "adminReserva":
                    return typeof(AdministrarReserva);
                case "buscarHab":
                    return typeof(buscadorHabitaciones);
                case "crearReserva":
                    return typeof(CrearReserva);
                case "updateUser":
                    return typeof(updateUser);
                case "userList":
                    return typeof(userList);
               

                // Agregar más ventanas según sea necesario
                default:
                    return null;
            }
        }
    }
}