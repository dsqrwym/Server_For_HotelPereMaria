using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using WPFIntermodular.model.reserva;
using WPFIntermodular.network.reservaService;

namespace WPFIntermodular.viewmodel.reservaViewModel
{
    public class Room
    {
        public string ImageSource { get; set; }
        public string RoomName { get; set; }
        public double Price { get; set; }
    }
    public class BuscarReservaViewModel : ViewModelBase
    {
        private readonly NavigationService _navigationService;

        private RelayCommand NavigateToSecondWindowCommand { get; set; }

        private bool atras;
        private bool listado;
        private bool listaReser;

        public ObservableCollection<CalendarDateRange> BlackoutDates { get; set; }
        public ObservableCollection<Habitacion> Habitaciones { get; set; }
        private ObservableCollection<Habitacion> habitacionesOriginal;

        public RelayCommand BuscarCommand { get; set; }
        public RelayCommand ResetearCommand { get; set; }
        public RelayCommand<object> ReservarCommand { get; set; }
        public RelayCommand CerrarSesionCommand { get; set; }
        public RelayCommand NavegarAUsuarioCommand { get; set; }
        public RelayCommand NavegarAHabitacionCommand { get; set; }
        public RelayCommand NavegarAListaReservasCommand { get; set; }


        public  DateTime startDate { get; set; }
        public DateTime startEndDate { get; set; }
        public List<TipoCapacidad> TiposHab { get; set; }
        public TipoCapacidad TipoSeleccionado { get; set; }
        public DateTime? FechaEntrada { get; set; }
        public DateTime? FechaSalida { get; set; }
        public bool IncluyeComidas { get; set; }
        public bool IncluyeParking { get; set; }
        public bool IncluyeGM { get; set; }

        public decimal PrecioDesde { get; set; }
        public decimal PrecioHasta { get; set; }

        public string nombreUsuario { get; set; }
        private Habitacion selectedHabitacion { get; set; }


        public BuscarReservaViewModel()
        {
            _navigationService = new NavigationService();

            // 初始化房间数据


            InicializarDatosAsync();

            // 设置默认价格范围
            PrecioDesde = 0;
            PrecioHasta = 1000;

            startDate = DateTime.UtcNow.AddDays(1);
            startEndDate = startDate.AddDays(1);

            // 初始化命令
            BuscarCommand = new RelayCommand(BuscarHabitaciones);
            ResetearCommand = new RelayCommand(ResetearFiltros);
            ReservarCommand = new RelayCommand<object>(Reservar);
            CerrarSesionCommand = new RelayCommand(() => _navigationService.NavigateTo("login"));
            NavegarAUsuarioCommand = new RelayCommand(NavegarAUsuario);
            NavegarAHabitacionCommand = new RelayCommand(NavegarAHabitacion);
            NavegarAListaReservasCommand = new RelayCommand(NavegarAListaReservas);
            NavigateToSecondWindowCommand = new RelayCommand(nav);
        }
        private async Task InicializarDatosAsync()
        {
            habitacionesOriginal = new ObservableCollection<Habitacion>();
            var habitaciones = await obtenerHabitacion(DateTime.UtcNow.AddDays(1), DateTime.UtcNow.AddDays(2));

            if (habitaciones.Count == 0)
            {
                DialogService.ShowWarning("No hay habitaciones disponibles en las fechas seleccionadas.", "No se encontraron resultados");
            }
            foreach (var item in habitaciones)
            {
                habitacionesOriginal.Add(item);
            }

            Habitaciones = new ObservableCollection<Habitacion>(habitacionesOriginal);
            TiposHab = await getHabitacionTipos();
            RaisePropertyChanged(nameof(TiposHab));  // 通知 UI 数据已更新
            RaisePropertyChanged(nameof(habitacionesOriginal));
            RaisePropertyChanged(nameof(Habitaciones));
        }
        private async Task<List<TipoCapacidad>> getHabitacionTipos()
        {
            try
            {
                return await ReservaApiService.ObtenerTiposHabitacionAsync();
            }
            catch (Exception ex)
            {
                DialogService.ShowError($"Hubo un error al obtener los tipos de habitación: {ex.Message}", "Error");
                return new List<TipoCapacidad>();
            }
        }

        private async Task<List<Habitacion>> obtenerHabitacion(DateTime fechaEntrada, DateTime fechaSalida)
        {
            return await ReservaApiService.ObtenerHabitacionesDisponiblesAsync(fechaEntrada, fechaSalida);
        }
        private async void BuscarHabitaciones()
        {

            if (FechaEntrada != null && FechaSalida != null && FechaSalida > FechaEntrada)
            {
                DateTime fechaEntrada = (DateTime)FechaEntrada;
                DateTime fechaSalida = (DateTime)FechaSalida;

                var habitaciones = await obtenerHabitacion(fechaEntrada, fechaSalida);

                if (habitaciones.Count == 0)
                {
                    DialogService.ShowWarning("No hay resultado", "Datos vacios");
                }

                habitacionesOriginal.Clear();
                foreach (var item in habitaciones)
                {
                    habitacionesOriginal.Add(item);
                }
            }
            else
            {
                DialogService.ShowWarning("Por favor, selecciona una fecha de entrada y salida antes de buscar.", "Falta de datos");
                return;
            }

            Habitaciones.Clear();

            var resultados = habitacionesOriginal.Where(h =>
            {
                if (TipoSeleccionado != null && h.Tipo != TipoSeleccionado.Tipo)
                    return false;

                if (IncluyeComidas && !h.extrasDict.ContainsKey("Comidas"))
                    return false;

                if (IncluyeParking && !h.extrasDict.ContainsKey("Parking"))
                    return false;

                if (IncluyeGM && !h.extrasDict.ContainsKey("GM"))
                    return false;

                if (h.PrecioNoche < PrecioDesde || h.PrecioNoche > PrecioHasta)
                    return false;

                return true;
            });

            foreach (var habitacion in resultados)
            {
                Habitaciones.Add(habitacion);
            }
        }

        private void ResetearFiltros()
        {
            InicializarDatosAsync();
            TipoSeleccionado = null;
            FechaEntrada = null;
            FechaSalida = null;
            IncluyeComidas = false;
            IncluyeParking = false;
            IncluyeGM = false;
            PrecioDesde = 0;
            PrecioHasta = 1000;
            Habitaciones.Clear();
            foreach (var habitacion in habitacionesOriginal)
            {
                Habitaciones.Add(habitacion);
            }
            RaisePropertyChanged(nameof(TipoSeleccionado));
            RaisePropertyChanged(nameof(FechaEntrada));
            RaisePropertyChanged(nameof(FechaSalida));
            RaisePropertyChanged(nameof(IncluyeComidas));
            RaisePropertyChanged(nameof(IncluyeParking));
            RaisePropertyChanged(nameof(IncluyeGM));
            RaisePropertyChanged(nameof(PrecioDesde));
            RaisePropertyChanged(nameof(PrecioHasta));
        }

        private void Reservar(object obj)
        {
            if (obj is Habitacion habitacion)
            {
                if (FechaEntrada != null && FechaSalida != null)
                {
                    _navigationService.NavigateTo("CrearReserva", habitacion, FechaEntrada, FechaSalida);
                }
                else
                {
                    DialogService.ShowError("Debe seleccionar una fecha de entrada y una fecha de salida para continuar con la reserva.");
                }
            }
            
        }
        private void NavegarAHabitacion()
        {
            atras = false;
            nav();
        }

        private void NavegarAUsuario()
        {
            listado = true;
            nav();
        }

        private void NavegarAListaReservas()
        {
            listaReser = true;
            nav();
        }

        private void nav()
        {
            string navDestination = listado ? "userList" :
                                    atras ? "login" :
                                    listaReser ? "adminReserva" :
                                    "buscarHab";

            switch (navDestination)
            {
                case "userList":
                    _navigationService.NavigateTo("userList");
                    break;
                case "login":
                    _navigationService.NavigateTo("login");
                    break;
                case "adminReserva":
                    _navigationService.NavigateTo("adminReserva");
                    break;
                case "buscarHab":
                    _navigationService.NavigateTo("buscarHab");
                    break;
            }
        }
    }
}