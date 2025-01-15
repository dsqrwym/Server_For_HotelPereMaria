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

namespace WPFIntermodular
{
    /// <summary>
    /// Lógica de interacción para addHabitaciones.xaml
    /// </summary>
    public partial class addHabitaciones : Window
    {
        public addHabitaciones(string nombre, int index, string descripccion, Boolean cama, Boolean cuna,Boolean editar)
        {
            InitializeComponent();
            fillFields(nombre, index, descripccion, cama, cuna, editar);

        }

        private void fillFields(string nombre,int index, string descripccion,Boolean cama,Boolean cuna,Boolean editar)
        {
            if (editar)
            {
                index = index-1;
                nombreText.Text = nombre;
                huespedesCombo.SelectedIndex = index;
                //imagen buscar como cargarla
                descripccionText.Text = descripccion;
                camaCheck.IsChecked = cama;
                cunaCheck.IsChecked = cuna;
                //precio por noche se calcula a parte
                botonFinal.Content = "Editar";
            }
            else
            {
                botonFinal.Content = "Crear";
            }



        }

    }
    

}
