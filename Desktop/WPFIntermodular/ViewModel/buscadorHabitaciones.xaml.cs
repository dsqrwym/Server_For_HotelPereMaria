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
using static System.Net.Mime.MediaTypeNames;


namespace WPFIntermodular
{
    /// <summary>
    /// Lógica de interacción para buscadorHabitaciones.xaml
    /// </summary>
    public partial class buscadorHabitaciones : Window
    {

        private ObservableCollection<Habitacion> habitaciones = new ObservableCollection<Habitacion>();
        Habitacion h = new Habitacion(1, "Suite", 3, 25, [],null);
       
        
        public buscadorHabitaciones()
        {


            InitializeComponent();
            

            buscarEditBtn.Content = "Crear";




            habitaciones.Add(h);
            

            listaHab.ItemsSource = habitaciones;





        }
       
        public void addHab(Habitacion h)
        {
            habitaciones.Add(h);
            listaHab.ItemsSource = habitaciones;
        }
        public void actLista()
        {
            listaHab.ItemsSource = habitaciones;

        }

        private void buscarEditBtn_Click(object sender, RoutedEventArgs e)
        {
            if (listaHab.SelectedIndex != -1)
            {
                Habitacion h = habitaciones[listaHab.SelectedIndex];
                addHabitaciones a = new addHabitaciones(h, true);
                this.Hide();
                a.Show();
            }
            else
            {
                buscarEditBtn.Content = "Crear";
                addHabitaciones a = new addHabitaciones(null, false);
                this.Hide();
                a.Show();

            }
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
            bool valido = true;
            int id = -1;
            bool? ocupadao = null;

            // Validar el ID
            if (!string.IsNullOrEmpty(idText.Text))
            {
                valido = int.TryParse(idText.Text, out id);
            }

            if (ocupadaSi.IsChecked == true)
            {
                ocupadao = true;  
            }
            else if (ocupadaNo.IsChecked == true)
            {
                ocupadao = false;  
            }

            filterHabitaciones(tiposCombo.Text, id, valido, ocupadao);
        }

        public void filterHabitaciones(string tipo, int id, bool valido, bool? ocupado)
        {
            if (habitaciones == null) return;

            ICollectionView habitacionesView = CollectionViewSource.GetDefaultView(habitaciones);

            habitacionesView.Filter = new Predicate<object>(obj =>
            {
                if (obj is Habitacion habitacion)
                {

                    bool tipoCoincide = string.IsNullOrEmpty(tipo) || habitacion.Tipo == tipo;

                    bool idCoincide = (valido && id != -1) ? habitacion.Id == id : true;

                    //bool ocupadaCoincide = !ocupado.HasValue || habitacion.Ocupada == ocupado;

                    return tipoCoincide && idCoincide;
                }
                return false;
            });

            habitacionesView.Refresh();
        }

        private void Button_Click_1(object sender, RoutedEventArgs e)
        {
            MainWindow m = new MainWindow();
            Window w = m.buscarVentana("BuscarReserva");
            this.Hide();
        }
    }
}