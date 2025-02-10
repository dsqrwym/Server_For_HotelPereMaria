using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using WPFIntermodular.model;
using WPFIntermodular.view;

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
        public  DateTime startDate { get; set; }
        public DateTime startEndDate { get; set; }

        public ObservableCollection<CalendarDateRange> BlackoutDates { get; set; }
        public ObservableCollection<Habitacion> Habitaciones { get; set; }
        private ObservableCollection<Habitacion> habitacionesOriginal;

        public RelayCommand BuscarCommand { get; set; }
        public RelayCommand ResetearCommand { get; set; }
        public RelayCommand<object> ReservarCommand { get; set; }
        public RelayCommand CerrarSesionCommand { get; set; }

        public List<TipoHab> TiposHab { get; set; }
        public TipoHab TipoSeleccionado { get; set; }
        public DateTime? FechaEntrada { get; set; }
        public DateTime? FechaSalida { get; set; }
        public bool IncluyeComidas { get; set; }
        public bool IncluyeParking { get; set; }
        public bool IncluyeGM { get; set; }

        public decimal PrecioDesde { get; set; }
        public decimal PrecioHasta { get; set; }

        public string nombreUsuario { get; set; }


        public BuscarReservaViewModel()
        {
            _navigationService = new NavigationService();

            // 初始化房间数据
            habitacionesOriginal = new ObservableCollection<Habitacion>
            {
                new Habitacion(1, "Individual", 1, 20m, new Dictionary<string, decimal>() { { "Wifi", 5m } }, new List<string>(){"/source/habitacion.png"}),
                new Habitacion(2, "Suite", 3, 66m, new Dictionary<string, decimal>() { { "GM", 10m }, { "Parking", 10m}, { "Wifi", 5m}}, new List<string>(){"/source/habitacion.png"}),
                new Habitacion(3, "Estándar", 2, 36m, new Dictionary<string, decimal>() { { "Parking", 10m } }, new List<string>(){"/source/habitacion.png"}),
            };

            Habitaciones = new ObservableCollection<Habitacion>(habitacionesOriginal);

            TiposHab = new List<TipoHab>
            {
                new TipoHab("Individual", 1),
                new TipoHab("Estándar", 2),
                new TipoHab("Lujo", 2),
                new TipoHab("Suite", 3),
                new TipoHab("Suite dúplex", 6)
            };

            // 设置默认价格范围
            PrecioDesde = 0;
            PrecioHasta = 1000;

            startDate = DateTime.UtcNow.AddDays(1);
            startEndDate = startDate.AddDays(1);

            BlackoutDates = new ObservableCollection<CalendarDateRange>
            {
                new CalendarDateRange(new DateTime(2025, 03, 01), new DateTime(2025, 03, 03)),
                new CalendarDateRange(new DateTime(2025, 04, 05), new DateTime(2025, 04, 10)),
                new CalendarDateRange(new DateTime(2025, 05, 15), new DateTime(2025, 05, 20))
            };
            // 初始化命令
            BuscarCommand = new RelayCommand(BuscarHabitaciones);
            ResetearCommand = new RelayCommand(ResetearFiltros);
            ReservarCommand = new RelayCommand<object>(Reservar);
            CerrarSesionCommand = new RelayCommand(() => _navigationService.NavigateTo("login"));
        }

        public List<CalendarDateRange> GetBlackoutDates()
        {
            return new List<CalendarDateRange>
            {
                new CalendarDateRange(new DateTime(2025, 03, 01), new DateTime(2025, 03, 03)),
                new CalendarDateRange(new DateTime(2025, 04, 05), new DateTime(2025, 04, 10)),
                new CalendarDateRange(new DateTime(2025, 05, 15), new DateTime(2025, 05, 20))
            };
        }

        private void BuscarHabitaciones()
        {
            Habitaciones.Clear();

            var resultados = habitacionesOriginal.Where(h =>
            {
                if (TipoSeleccionado != null && h.Tipo != TipoSeleccionado.Nombre)
                    return false;

                if (IncluyeComidas && !h.Extras.ContainsKey("Comidas"))
                    return false;

                if (IncluyeParking && !h.Extras.ContainsKey("Parking"))
                    return false;

                if (IncluyeGM && !h.Extras.ContainsKey("GM"))
                    return false;

                if (h.Precio < PrecioDesde || h.Precio > PrecioHasta)
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
                    new CrearReserva(null, habitacion, FechaEntrada, FechaSalida).Show();
                }
                else
                {
                    MessageBox.Show("Por favor, seleccione fechas de entrada y salida.", "Error", MessageBoxButton.OK, MessageBoxImage.Warning);
                }
            }
        }
    }
}