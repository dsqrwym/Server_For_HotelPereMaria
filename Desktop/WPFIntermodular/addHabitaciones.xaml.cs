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
        public addHabitaciones(Habitacion h, Boolean editar)
        {
            InitializeComponent();
            if (editar)
            {

                fillFields(h);
            }
        }

        private void fillFields(Habitacion h)
        {

            int index = h.Capacidad-1;
            nombreText.Text = h.Nombre;
            huespedesCombo.SelectedIndex = index;
            //imagen buscar como cargarla
            descripccionText.Text = h.Descripccion;
            //precio por noche se calcula a parte
            botonFinal.Content = "Editar";





        }

    }


}