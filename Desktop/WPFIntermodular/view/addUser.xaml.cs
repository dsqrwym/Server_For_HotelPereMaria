using System.Windows;
using System.Windows.Controls;
using WPFIntermodular.model;

namespace WPFIntermodular.view
{
    public partial class addUser : Window
    {
        public addUser()
        {
            InitializeComponent();
            var viewModel = new AddUserViewModel();
            viewModel.CloseWindowAction = () => this.Dispatcher.Invoke(this.Close);
            DataContext = viewModel;
        }
    }
}
