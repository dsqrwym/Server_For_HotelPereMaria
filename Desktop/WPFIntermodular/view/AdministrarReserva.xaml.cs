using System.Collections.ObjectModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using WPFIntermodular.model;

namespace WPFIntermodular.view
{
    public partial class AdministrarReserva : Window
    {
        public AdministrarReserva()
        {
            InitializeComponent();
            DataContext = new viewmodel.reservaViewModel.AdministrarReservaViewModel();
        }
        // 编辑按钮点击事件
        private void EditButton_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            var reserva = button?.DataContext as model.Reserva;
            if (reserva != null) 
            {
                if (reserva.Fecha_fin < DateTime.Today)
                {
                    MessageBox.Show($"No se puede modifiacr la reserva con ID {reserva.Id} porque la fecha de finalización es anterior a hoy.",
                       "Eliminar reserva", MessageBoxButton.OK,
                       MessageBoxImage.Warning);
                    return;
                }
                new CrearReserva(reserva, CrearReserva.MODIFICAR).Show();
                cerrarView();
            }
        }

        // 查看按钮点击事件
        private void ViewDetailsButton_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            var reserva = button?.DataContext as model.Reserva;
            if (reserva != null)
            {
                CrearReserva detalle = new CrearReserva(reserva, CrearReserva.DETALLE);
                detalle.Show();
                cerrarView();
            }
        }

        private void ButtonCrear_Click(object sender, RoutedEventArgs e)
        {
            //new BuscarReserva(this).Show();
            cerrarView();
        }

        private void cerrarView()
        {
            Visibility = Visibility.Collapsed;
        }

        private void botonBuscador_Click(object sender, RoutedEventArgs e)
        {
            //new BuscarReserva(this).Show();
            cerrarView();
        }


        private void comboBox_KeyUp(object sender, System.Windows.Input.KeyEventArgs e)
        {
            ComboBox comboBox = sender as ComboBox;
            if (comboBox != null) {
                if (e.Key != Key.Enter && e.Key != Key.Escape)
                {
                    comboBox.IsDropDownOpen = true;
                }
            }
        }
    }
}
