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
using WPFIntermodular.vistaReserva;

namespace WPFIntermodular.vistaUsuario
{
    /// <summary>
    /// Interaction logic for loginScreen.xaml
    /// </summary>
    public partial class loginScreen : Window
    {
        public loginScreen()
        {
            InitializeComponent();
            MainWindow m = new MainWindow();
            m.addVentana(this);
        }

        private void loginBtn_Click(object sender, RoutedEventArgs e)
        {
            BuscarReserva b = new BuscarReserva(this);
            MainWindow m = new MainWindow();
            this.Hide();
            b.Show();
        }
    }
}
