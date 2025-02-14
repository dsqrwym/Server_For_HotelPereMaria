using System.Windows;
using WPFIntermodular.network.reservaService;


public static class DialogService
{
    public static void ShowWarning(string message, string title)
    {
        MessageBox.Show(message, title, MessageBoxButton.OK, MessageBoxImage.Warning);
    }
    public static void ShowError(string message, string title = "Error")
    {
        MessageBox.Show(message, title, MessageBoxButton.OK, MessageBoxImage.Error);
    }

    public static MessageBoxResult ShowConfirmation(string message, string title)
    {
        return MessageBox.Show(message, title, MessageBoxButton.YesNo, MessageBoxImage.Question);
    }
}