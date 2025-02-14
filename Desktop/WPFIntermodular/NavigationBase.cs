using System;
using System.Windows;

using WPFIntermodular.view;

namespace WPFIntermodular
{
    public class NavigationService
    {
        // Método para navegar a una nueva ventana
        public void NavigateTo(string windowName, params object[] parameters)
        {
            if (Application.Current == null || Application.Current.Windows == null)
            {
                MessageBox.Show("Error: No se pudo acceder a la lista de ventanas.");
                return;
            }

            // Resolver el tipo de ventana a partir del nombre
            Type windowType = GetWindowTypeByName(windowName);

            if (windowType == null)
            {
                MessageBox.Show("Ventana no encontrada: " + windowName);
                return;
            }

            // si ya existe dicha ventana
            Window existingWindow = Application.Current.Windows
                .OfType<Window>()
                .FirstOrDefault(w => w.GetType() == windowType);

            if (existingWindow != null)
            {
                existingWindow.Show();
                return;
            }


            Window currentWindow = Application.Current.Windows
           .OfType<Window>()
           .FirstOrDefault(w => w.IsActive);
            currentWindow?.Hide();

            Window newWindow;
            if (parameters.Length == 0)
            {
                newWindow = (Window)Activator.CreateInstance(windowType);
            }
            else
            {

                // Obtener el constructor adecuado
                var constructor = windowType.GetConstructor(
                    parameters.Where(p => p != null).Select(p => p.GetType()).ToArray());

                if (constructor != null)
                {
                    // Crear y mostrar la nueva ventana
                    newWindow = (Window)constructor.Invoke(parameters);
                }
                else
                {
                    MessageBox.Show("No se encontró un constructor adecuado para los parámetros.");
                    return;
                }
            }

            newWindow.Show();
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