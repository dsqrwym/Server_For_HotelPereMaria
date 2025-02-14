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


        private void ButtonCrear_Click(object sender, RoutedEventArgs e)
        {
            //new BuscarReserva(this).Show();
            cerrarView();
        }

        private void cerrarView()
        {
            Visibility = Visibility.Collapsed;
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

        private void botonRestablecer_Click(object sender, RoutedEventArgs e)
        {
            fechaFinalInput.SelectedDate = null;
            fechaInicioInput.SelectedDate = null;
        }
    }
}
