using GalaSoft.MvvmLight.Command;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.IO;
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
using System.Xml.Linq;
using WPFIntermodular.model;
using WPFIntermodular.viewmodel.habitacionViewModel;
using static System.Net.Mime.MediaTypeNames;


namespace WPFIntermodular.view
{
    /// <summary>
    /// Lógica de interacción para buscadorHabitaciones.xaml
    /// </summary>
    public partial class buscadorHabitaciones : Window
    {
        private bool editar = false;
        private bool atras = false;
        private ObservableCollection<Habitacion> Habitaciones = new ObservableCollection<Habitacion>();
        Habitacion h;
   
        public buscadorHabitaciones()
        {


            InitializeComponent();
            this.DataContext = buscarHabitacionViewModel.instance;


          

            buscarEditBtn.Content = "Crear";





        }
       
        

       

        private void listaHab_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (listaHab.SelectedIndex != -1)
            {
                buscarEditBtn.Content = "Editar";
            }
            else
            {
                buscarEditBtn.Content = "Crear";

            }
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
           
            
           

        }



        

        

        private void buscarEditBtn_Click(object sender, RoutedEventArgs e)
        {
            this.Close();
            buscarHabitacionViewModel.instance.BuscarEditar();
        }

        private void borrarClick(object sender, RoutedEventArgs e)
        {
            MessageBoxResult result = MessageBox.Show(
                "¿Estás seguro de que deseas continuar?",  
                "Confirmación",                           
                MessageBoxButton.YesNo,                    
                MessageBoxImage.Question                  
            );

            if (listaHab.SelectedIndex != null)
            {
                if (result == MessageBoxResult.Yes)
                {
                    Habitacion h = listaHab.SelectedItem as Habitacion;
                    buscarHabitacionViewModel.instance.BorrarHabitacion(h);
                    ConexionController controller = new ConexionController();
                    controller.deleteHab(h);
                    
                }
                else
                {
                    MessageBox.Show("Operacion cancelada");
                }
            }
            else
            {
                MessageBox.Show("Selecciona una habitacion");
            }

            
        }

        private void Button_Click_1(object sender, RoutedEventArgs e)
        {
            this.Close();
        }

        private void Button_Click_2(object sender, RoutedEventArgs e)
        {
                idText.Text = "";
                nomText.Text = "";
                cajaTipo.SelectedIndex = -1;
                 buscarHabitacionViewModel.instance.Filtro.Refresh();
           
            

        }
    }
}