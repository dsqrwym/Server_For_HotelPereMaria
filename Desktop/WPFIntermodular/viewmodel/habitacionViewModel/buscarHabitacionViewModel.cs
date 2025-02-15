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
            }
        }
        public int IndiceSeleccionado => Habitaciones.IndexOf(HabitacionSeleccionada);

        public buscarHabitacionViewModel()
        {
            c = new ConexionController();
            Habitaciones = new ObservableCollection<Habitacion>();
            _navigationService = new NavigationService();
            NavigateToSecondWindowCommand = new RelayCommand(nav);
            CargarDatos();
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
                        hab.Extras, // Extras vacíos
                        hab.Images, // Lista de imágenes vacía
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
