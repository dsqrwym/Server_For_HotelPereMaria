using GalaSoft.MvvmLight.Command;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;
using System.Windows.Input;
using WPFIntermodular.model;


namespace WPFIntermodular.viewmodel.habitacionViewModel
{
    internal class buscarHabitacionViewModel : INotifyPropertyChanged
    {
        private readonly ConexionController c = new ConexionController();
        private readonly string[] _tiposHabitacion = { "Individual", "Estandar", "Lujo", "Suite", "Duplex" };
        public string[] TiposHabitacion => _tiposHabitacion;

        public event PropertyChangedEventHandler? PropertyChanged;

        protected void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private ObservableCollection<Habitacion> _habitaciones;

        private static buscarHabitacionViewModel _instancia;

      

        public RelayCommand BuscarEditCommand { get; private set; }
        public bool Editar { get; set; }
        private Habitacion _habitacionSeleccionada;
        public Habitacion HabitacionSeleccionada
        {
            get => _habitacionSeleccionada;
            set
            {
                _habitacionSeleccionada = value;
                OnPropertyChanged(nameof(HabitacionSeleccionada));
            }
        }

        private NavigationService _navigationService;
        

        private RelayCommand NavigateToSecondWindowCommand { get; set; }
        public ObservableCollection<Habitacion> Habitaciones
        {
            get => _habitaciones;
            set
            {
                _habitaciones = value;
                OnPropertyChanged(nameof (Habitaciones));

                Filtro = CollectionViewSource.GetDefaultView(_habitaciones);
            }
        }
        public int IndiceSeleccionado => Habitaciones.IndexOf(HabitacionSeleccionada);

        public ICommand CerrarVentanaCommand { get;}

        private ICollectionView _filtro;
        public ICollectionView Filtro
        {
            get => _filtro;
            set
            {
                _filtro = value;
                OnPropertyChanged(nameof(Filtro));
            }
        }
        private string filtroNombre;
        public string FiltroNombre
        {
            get => filtroNombre;
            set
            {
                filtroNombre = value;
                OnPropertyChanged(nameof(FiltroNombre));
                Filtro.Refresh();
            }
        }

        private string filtroID;
        public string FiltroID
        {
            get => filtroID;
            set
            {
                filtroID = value;
                OnPropertyChanged(nameof(FiltroID));
                Filtro.Refresh();
            }
        }


        private int _selectedTipoIndex;
        public int SelectedTipoIndex
        {
            get => _selectedTipoIndex;
            set
            {
                if (_selectedTipoIndex != value)
                {
                    _selectedTipoIndex = value;
                    OnPropertyChanged(nameof(SelectedTipoIndex));
                    if (_selectedTipoIndex >= 0 && _selectedTipoIndex < _tiposHabitacion.Length)
                    {
                        Tipo = _tiposHabitacion[_selectedTipoIndex];
                    }
                }
            }
        }

        private string _tipo;
        public string Tipo
        {
            get => _tipo;
            set
            {
                if (_tipo != value)
                {
                    _tipo = value;
                    OnPropertyChanged(nameof(Tipo));
                    Filtro.Refresh();  

                    int newIndex = Array.IndexOf(_tiposHabitacion, _tipo);
                    if (newIndex != -1 && newIndex != _selectedTipoIndex)
                    {
                        SelectedTipoIndex = newIndex;
                    }
                }
            }
        }


        public buscarHabitacionViewModel()
        {
            c = new ConexionController();
            Habitaciones = new ObservableCollection<Habitacion>();
            _navigationService = new NavigationService();
            NavigateToSecondWindowCommand = new RelayCommand(nav);
            CerrarVentanaCommand = new RelayCommand(CerrarVentana);
            Filtro = CollectionViewSource.GetDefaultView(Habitaciones);
            Filtro.Filter = FiltrarHabitacion;
            CargarDatos();
        }

        private bool FiltrarHabitacion(object obj)
        {
            if (obj is Habitacion habitacion)
            {
                
                bool cumpleNombre = string.IsNullOrEmpty(FiltroNombre) ||habitacion.Nombre.IndexOf(filtroNombre, StringComparison.OrdinalIgnoreCase) >= 0;

                bool cumpleID = string.IsNullOrEmpty(filtroID) ||habitacion.Id.IndexOf(filtroID, StringComparison.OrdinalIgnoreCase) >= 0;

                bool cumpleTipo = string.IsNullOrEmpty(Tipo) ||habitacion.Tipo.IndexOf(Tipo, StringComparison.OrdinalIgnoreCase) >= 0;

                return cumpleNombre && cumpleID && cumpleTipo;
            }
            return false;
        }

        public static buscarHabitacionViewModel instance
        {
            get {
                if (_instancia == null)
                {
                    _instancia = new buscarHabitacionViewModel();
                }
                return _instancia;
            }
          
        }

        


        public void AgregarHabitacion(Habitacion nuevaHabitacion)
        {
            if (nuevaHabitacion != null)
            {
                Habitaciones.Add(nuevaHabitacion);
                OnPropertyChanged(nameof(Habitaciones));
            }
        }

        public void BorrarHabitacion(Habitacion habitacion)
        {
            if (habitacion != null)
            {
                Habitaciones.Remove(habitacion);
                OnPropertyChanged(nameof(Habitaciones));
            }
        }



        public async Task CargarDatos()
        {
            var habitaciones = await c.Index();
            if (habitaciones == null)
            {
                MessageBox.Show("No se han cargado datos, revise su conexion al servidor");
            }
            if (habitaciones != null)
            {
                
                foreach(var hab in habitaciones)
                {
                   
                    Habitaciones.Add(new Habitacion(
                        hab.Id,
                        hab.Tipo,
                        hab.Capacidad,
                        hab.Precio,
                        hab.Extras, 
                        hab.Images, 
                        hab.Nombre,
                        hab.Descripccion
                    ));
                }
                   

                
            }
            OnPropertyChanged(nameof(Habitaciones));
        }   

        public void BuscarEditar()
        {
            if (HabitacionSeleccionada != null)
            {
                Editar = true;
                nav();
            }
            else
            {
                Editar = false;
                nav();
                
            }
        }

        public void CerrarVentana()
        {
            _navigationService.NavigateTo("buscarReserva");
        }
        public void  nav()
        {
            if (Editar)
            {
                Habitacion h = Habitaciones[IndiceSeleccionado];
                _navigationService.NavigateTo("addHab", h, true);
            }
            else
            {
                try
                {
                    Habitacion h = Habitaciones[0];
                    _navigationService.NavigateTo("addHab", h, false);

                }
                catch (Exception ex)
                {
                    MessageBox.Show("No hay conexion a la API, porfavor revise su conexion");

                }
                
               

                

            }
            

        }
    }
}
