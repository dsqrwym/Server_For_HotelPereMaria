using System.Windows;
using WPFIntermodular.viewmodel;
using WPFIntermodular.model;
using WPFIntermodular.viewModel;

namespace WPFIntermodular.view
{
    public partial class updateUser : Window
    {
        public updateUser(Usuario usuario, ConexionApi conexionApi)
        {
            InitializeComponent();
            var viewModel = new UpdateUserViewModel(usuario,conexionApi);
            viewModel.CloseWindowAction = () => this.Dispatcher.Invoke(this.Close);
            DataContext = viewModel;
        }
    }
}
