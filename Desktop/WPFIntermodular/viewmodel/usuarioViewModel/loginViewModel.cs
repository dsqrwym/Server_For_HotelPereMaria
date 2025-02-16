using GalaSoft.MvvmLight.Command;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Navigation;
using WPFIntermodular.view;


public class LoginViewModel : INotifyPropertyChanged
{
    private string _email;
    private string _password;
    private bool _isLoading;
    private readonly ConexionApi _apiService;
    private NavigationService _navigationService;
    private readonly loginScreen loginS;

    public event PropertyChangedEventHandler PropertyChanged;

    public string Email
    {
        get => _email;
        set { _email = value; OnPropertyChanged(); }
    }

    public string Password
    {
        get => _password;
        set { _password = value; OnPropertyChanged(); }
    }

    public bool IsLoading
    {
        get => _isLoading;
        set { _isLoading = value; OnPropertyChanged(); }
    }

    public RelayCommand LoginCommand { get; }

    public LoginViewModel()
    {
        _apiService = new ConexionApi();
        LoginCommand = new RelayCommand(async () => await LoginAsync(), CanExecuteLogin);
    }

    private async Task LoginAsync()
    {
        IsLoading = true;

        bool success = await _apiService.Login(Email, Password);

        if (success)
        {
            MessageBox.Show("Pulsa seguir");
        }
        else
        {
            MessageBox.Show("Error.");
        }

        IsLoading = false;
    }

    private bool CanExecuteLogin()
    {
        return !string.IsNullOrWhiteSpace(Email) && !string.IsNullOrWhiteSpace(Password) && !IsLoading;
    }

    private void OnPropertyChanged([CallerMemberName] string propertyName = null)
    {
        PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        LoginCommand.RaiseCanExecuteChanged(); // Habilitar/Deshabilitar el botón
    }
}
