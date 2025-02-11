using GalaSoft.MvvmLight.Command;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
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

namespace WPFIntermodular.view

{
    /// <summary>
    /// Interaction logic for loginScreen.xaml
    /// </summary>
    public partial class loginScreen : Window
    {
        private NavigationService _navigationService;


        public RelayCommand NavigateToSecondWindowCommand { get; private set; }

        public loginScreen()
        {
            InitializeComponent();
            _navigationService = new NavigationService();  // Crear una instancia del servicio
            NavigateToSecondWindowCommand = new RelayCommand(nav);
        }

       

        private void loginBtn_Click(object sender, RoutedEventArgs e)
        {
            nav();
        }
        private void nav()
        {
            this.Hide();
            _navigationService.NavigateTo("buscarReserva");
           
        }
    }
}
