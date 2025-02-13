using Microsoft.Win32;
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
using System.IO;
using System.Diagnostics;
using WPFIntermodular.model;
using GalaSoft.MvvmLight.Command;
using WPFIntermodular.viewmodel.habitacionViewModel;


namespace WPFIntermodular.view
{
    /// <summary>
    /// Lógica de interacción para addHabitaciones.xaml
    /// </summary>
    public partial class addHabitaciones : Window
    {

        private Habitacion H;
        private bool EDITAR;
        public addHabitaciones(Habitacion h, bool editar) 
        {

          
            
            InitializeComponent();

            H= h;
            EDITAR= editar;
            if (editar)
            {
                btnFinal.Content = "Editar";
                titulo.Content = "Editar";
            }
            else
            {
                btnFinal.Content ="Crear";
                titulo.Content = "Crear";

            }
            this.DataContext = new addHabsViewModel(h, editar);

           
           
        }

        

        private void btnFinal_Click(object sender, RoutedEventArgs e)
        {
            if (this.DataContext is addHabsViewModel vm)
            {
                this.Hide();
                vm.CrearHabitacionAsync();

            }
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            if (this.DataContext is addHabsViewModel vm)
            {
                this.Hide();
                vm.CrearHabitacionAsync();

            }
        }
    }

}