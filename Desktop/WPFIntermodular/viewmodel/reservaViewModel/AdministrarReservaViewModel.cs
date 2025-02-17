using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using WPFIntermodular.network.reservaService;
using WPFIntermodular.view;
using WPFIntermodular.model.reserva;

namespace WPFIntermodular.viewmodel.reservaViewModel
{
    public class AdministrarReservaViewModel : ViewModelBase
    {
        private const string ConfirmacionEliminacion = "Confirmar eliminación";
        private const string Advertencia = "Advertencia";
        private const string ErrorFiltro = "Error en filtros";
        private readonly NavigationService _navigationService;
        public ObservableCollection<Reserva> Reservas { get; }
        private readonly ObservableCollection<Reserva> _reservasOriginal;

        public ObservableCollection<Usuario> Clientes { get; set; }
        public ObservableCollection<Usuario> Empleados { get; set; }

        private bool _isAllSelected;
        public bool IsAllSelected
        {
            get => _isAllSelected;
            set
            {
                if (Set(ref _isAllSelected, value))
                {
                    ActualizarSeleccionGlobal(value);
                }
            }
        }

        private Visibility _deleteButtonVisibility = Visibility.Collapsed;
        public Visibility DeleteButtonVisibility
        {
            get => _deleteButtonVisibility;
            set => Set(ref _deleteButtonVisibility, value);
        }

        // Propiedades para filtros
        public ComboBoxItem SelectedRole { get; set; }
        public Usuario SearchClientName { get; set; }
        public Usuario SearchEmployeeName { get; set; }
        public DateTime? FilterStartDate { get; set; }
        public DateTime? FilterEndDate { get; set; }

        // Comandos
        public RelayCommand EliminarSeleccionadosCommand { get; set; }
        public RelayCommand CrearReservaScreen { get; set; }
        public RelayCommand NavegarUsuario { get; set; }
        public RelayCommand NavegarHabitacion { get; set; }
        public RelayCommand FiltrarCommand { get; set; }
        public RelayCommand RestablecerFiltrosCommand { get; set; }
        public RelayCommand<Reserva> EditarCommand { get; set; }
        public RelayCommand<Reserva> VerDetallesCommand { get; set; }
        public RelayCommand<Reserva> EliminarIndividualCommand { get; set; }

        public AdministrarReservaViewModel()
        {
            _navigationService = new NavigationService();
            Reservas = new ObservableCollection<Reserva>();
            _reservasOriginal = new ObservableCollection<Reserva>();

            InicializarComandos();
            CargarDatosIniciales();
            ConfigurarManejoEventos();
        }

        private void InicializarComandos()
        {
            EliminarSeleccionadosCommand = new RelayCommand(EliminarReservasSeleccionadas);
            FiltrarCommand = new RelayCommand(AplicarFiltros);
            RestablecerFiltrosCommand = new RelayCommand(RestablecerFiltros);
            EditarCommand = new RelayCommand<Reserva>(EditarReserva);
            VerDetallesCommand = new RelayCommand<Reserva>(MostrarDetallesReserva);
            EliminarIndividualCommand = new RelayCommand<Reserva>(EliminarReservaIndividual);
            CrearReservaScreen = new RelayCommand(NavegarBuscaReserva);
            NavegarHabitacion = new RelayCommand(NavegarHabitacionScreen);
            NavegarUsuario = new RelayCommand(NavegarUsuarioScreen);
        }

        private void NavegarUsuarioScreen()
        {
            _navigationService.NavigateTo("userList");
        }

        private void NavegarHabitacionScreen()
        {
            _navigationService.NavigateTo("buscarHab");
        }

        private void NavegarBuscaReserva()
        {
            _navigationService.NavigateTo("buscarReserva");
        }

        private void ConfigurarManejoEventos()
        {
            Reservas.CollectionChanged += (s, e) =>
            {
                if (e.NewItems != null)
                {
                    foreach (Reserva item in e.NewItems)
                    {
                        item.PropertyChanged += Reserva_PropertyChanged;
                    }
                }
                if (e.OldItems != null)
                {
                    foreach (Reserva item in e.OldItems)
                    {
                        item.PropertyChanged -= Reserva_PropertyChanged;
                    }
                }
            };

            ActualizarVisibilidadBotonEliminar();
        }

        private async void CargarDatosIniciales()
        {
            try
            {
                var reservas = await ReservaApiService.ObtenerReservas();
                foreach (var reserva in reservas)
                {
                    Reservas.Add(reserva);
                    _reservasOriginal.Add(reserva);
                }
            }
            catch (Exception ex)
            {
                DialogService.ShowError($"Error cargando datos: {ex.Message}");
            }

            Clientes = new ObservableCollection<Usuario>(await ReservaApiService.ObtenerClientesAsync());
            Empleados = new ObservableCollection<Usuario>(await ReservaApiService.ObtenerEmpleadosAsync());
            RaisePropertyChanged(nameof(Clientes));  // 通知UI更新
            RaisePropertyChanged(nameof(Empleados));
        }

        private void ActualizarSeleccionGlobal(bool estaSeleccionado)
        {
            if (!Reservas.Any()) return;

            // 使用临时变量避免多次触发事件
            _isAllSelected = estaSeleccionado;

            foreach (var reserva in Reservas)
            {
                reserva.IsSelected = estaSeleccionado;
            }

            // 手动触发属性变更
            RaisePropertyChanged(nameof(IsAllSelected));
            ActualizarVisibilidadBotonEliminar();
        }
        private void Reserva_PropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            if (e.PropertyName == nameof(Reserva.IsSelected))
            {
                // 使用延迟更新解决界面渲染问题
                Application.Current.Dispatcher.BeginInvoke(new Action(() =>
                {
                    ActualizarVisibilidadBotonEliminar();
                    IsAllSelected = Reservas.All(r => r.IsSelected) && Reservas.Any();
                }), System.Windows.Threading.DispatcherPriority.Background);
            }
        }

        private void ActualizarVisibilidadBotonEliminar()
        {
            var cantidadSeleccionados = Reservas.Count(r => r.IsSelected);
            DeleteButtonVisibility = cantidadSeleccionados > 1 ? Visibility.Visible : Visibility.Collapsed;
        }

        private void AplicarFiltros()
        {
            try
            {
                var query = _reservasOriginal.AsQueryable();

                if (SelectedRole != null && SelectedRole.Tag != null)
                {
                    query = query.Where(r => r.Usuario.Tipo == Int32.Parse(SelectedRole.Tag.ToString()));
                    Debug.WriteLine(SelectedRole.Tag);
                }

                if (Clientes != null && SearchClientName != null && !string.IsNullOrEmpty(SearchClientName.Nombre))
                {
                    query = query.Where(r =>
                        r.Usuario.Nombre.Contains(SearchClientName.Nombre, StringComparison.OrdinalIgnoreCase));
                }

                if (SearchEmployeeName != null && !string.IsNullOrEmpty(SearchEmployeeName.Id))
                {
                    query = query.Where(r =>
                        r.AltUser.Contains(SearchEmployeeName.Id, StringComparison.OrdinalIgnoreCase));
                }

                if (FilterStartDate.HasValue)
                {
                    query = query.Where(r => r.Fecha_ini >= FilterStartDate.Value);
                }

                if (FilterEndDate.HasValue)
                {
                    query = query.Where(r => r.Fecha_fin <= FilterEndDate.Value);
                }

                var resultados = query.ToList();
                ActualizarListaReservas(resultados);
            }
            catch (Exception ex)
            {
                DialogService.ShowError($"{ErrorFiltro}: {ex.Message}");
            }
        }


        private void ActualizarListaReservas(IEnumerable<Reserva> reservas)
        {
            foreach (var item in Reservas)
            {
                item.PropertyChanged -= Reserva_PropertyChanged;
            }

            Reservas.Clear();

            foreach (var reserva in reservas)
            {
                reserva.IsSelected = false;
                reserva.PropertyChanged += Reserva_PropertyChanged; // 添加新项监听
                Reservas.Add(reserva);
            }

            // 强制更新状态
            IsAllSelected = false;
            ActualizarVisibilidadBotonEliminar();
            RaisePropertyChanged(nameof(IsAllSelected));
        }

        private async void RestablecerFiltros()
        {
            SelectedRole = null;
            SearchClientName = null;
            SearchEmployeeName = null;
            FilterStartDate = null;
            FilterEndDate = null;
            ActualizarListaReservas(_reservasOriginal);
            IsAllSelected = false;

            var reservas = await ReservaApiService.ObtenerReservas();

            Reservas.Clear();
            _reservasOriginal.Clear();
            foreach (var reserva in reservas)
            {
                Reservas.Add(reserva);
                _reservasOriginal.Add(reserva);
            }
        }

        private void EliminarReservasSeleccionadas()
        {
            var seleccionadas = Reservas.Where(r => r.IsSelected).ToList();
            if (seleccionadas.Count == 0) return;

            var confirmacion = DialogService.ShowConfirmation(
                $"¿Está seguro de eliminar {seleccionadas.Count} reservas?",
                ConfirmacionEliminacion);

            if (confirmacion != MessageBoxResult.Yes) return;

            var (validas, invalidas) = ClasificarReservas(seleccionadas);

            if (invalidas.Any())
            {
                MostrarAdvertenciaReservasInvalidas(invalidas.Count);
            }

            EliminarReservas(validas);
            IsAllSelected = false;
            ActualizarVisibilidadBotonEliminar();
        }

        private (List<Reserva> validas, List<Reserva> invalidas)
            ClasificarReservas(List<Reserva> reservas)
        {
            var invalidas = reservas.Where(r => r.Fecha_fin < DateTime.Today).ToList();
            var validas = reservas.Except(invalidas).ToList();
            return (validas, invalidas);
        }

        private void MostrarAdvertenciaReservasInvalidas(int cantidad)
        {
            DialogService.ShowWarning(
                $"No se pueden eliminar {cantidad} reservas completadas",
                Advertencia);
        }

        private async void EliminarReservas(IEnumerable<Reserva> reservas)
        {
            foreach (var reserva in reservas.ToList())
            {
                var success = await ReservaApiService.EliminarReservaAsync(reserva.Id);
                if (success)
                {
                    Reservas.Remove(reserva);
                    _reservasOriginal.Remove(reserva);
                }
            }
        }

        private async void EliminarReservaIndividual(Reserva reserva)
        {
            if (reserva == null) return;

            if (reserva.Fecha_fin < DateTime.Today)
            {
                DialogService.ShowWarning(
                    "No se pueden eliminar reservas pasadas",
                    Advertencia);
                return;
            }

            var confirmacion = DialogService.ShowConfirmation(
                "¿Confirmar eliminación de la reserva seleccionada?",
                ConfirmacionEliminacion);

            if (confirmacion == MessageBoxResult.Yes)
            {
                var success = await ReservaApiService.EliminarReservaAsync(reserva.Id);
                if (success)
                {
                    Reservas.Remove(reserva);
                    _reservasOriginal.Remove(reserva);
                }
                else
                {
                    return;
                }
            }
            ActualizarVisibilidadBotonEliminar();
        }

        private void EditarReserva(Reserva reserva)
        {
            _navigationService.NavigateTo("CrearReserva", reserva, CrearReserva.MODIFICAR);
            if (reserva?.Fecha_ini <= DateTime.Today)
            {
                DialogService.ShowWarning(
                    "No se pueden modificar reservas ya empezadas",
                    Advertencia);
                return;
            }
            new CrearReserva(reserva, CrearReserva.MODIFICAR).Show();

        }

        private void MostrarDetallesReserva(Reserva reserva)
        {
            new CrearReserva(reserva, CrearReserva.DETALLE).Show();
        }
    }
}