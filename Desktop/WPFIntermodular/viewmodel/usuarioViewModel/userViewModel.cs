using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;
using System.Windows;
using WPFIntermodular.model;

public class UserViewModel : INotifyPropertyChanged
{
    private ObservableCollection<Usuario> _usuarios;
    private ObservableCollection<Usuario> _usuariosFiltrados;
    private string _filtroID;
    private string _filtroRol;

    public ObservableCollection<Usuario> Usuarios
    {
        get => _usuariosFiltrados;
        set
        {
            _usuariosFiltrados = value;
            OnPropertyChanged(nameof(Usuarios));
        }
    }

    public string FiltroID
    {
        get => _filtroID;
        set
        {
            _filtroID = value;
            FiltrarUsuarios();
            OnPropertyChanged(nameof(FiltroID));
        }
    }

    public string FiltroRol
    {
        get => _filtroRol;
        set
        {
            _filtroRol = value;
            FiltrarUsuarios();
            OnPropertyChanged(nameof(FiltroRol));
        }
    }

    public UserViewModel()
    {
        _usuarios = new ObservableCollection<Usuario>();
        _usuariosFiltrados = new ObservableCollection<Usuario>();
        _filtroID = "";
        _filtroRol = "Todos";
    }

    private void FiltrarUsuarios()
    {
        var filtrados = _usuarios.Where(u =>
            (string.IsNullOrEmpty(FiltroID) || u.id.Contains(FiltroID)) &&
            (FiltroRol == "Todos" || u.rol == FiltroRol)
        ).ToList();

        Usuarios.Clear();
        foreach (var usuario in filtrados)
        {
            Usuarios.Add(usuario);
        }
    }

    public event PropertyChangedEventHandler PropertyChanged;
    protected void OnPropertyChanged(string name) => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));
}
