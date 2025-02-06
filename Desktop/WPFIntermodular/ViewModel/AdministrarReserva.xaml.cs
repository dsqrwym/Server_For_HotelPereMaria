using System.Collections.ObjectModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using WPFIntermodular.model;

namespace WPFIntermodular.vistaReserva
{
    public partial class AdministrarReserva : Window
    {
        // 存储Reserva对象的集合
        private ObservableCollection<model.Reserva> reservas;
        private ObservableCollection<model.Reserva> reservasOriginal;

        public AdministrarReserva()
        {
            InitializeComponent();

            InicialData();

            DataContext = this;
        }

        private void InicialData()
        {
            // 初始化数据
            var reservasIniciales = new ObservableCollection<model.Reserva> {
                new model.Reserva (1, 101, 201, 1, DateTime.Now, DateTime.Now.AddDays(1), 2, 100.5m, "/source/habitacion.png" ),
                new model.Reserva (2, 102, 202, 3, DateTime.Now, DateTime.Now.AddDays(2), 3, 200.75m, "/source/habitacion.png" ),
                new model.Reserva (3, 103, 203, 2, DateTime.Now, DateTime.Now.AddDays(3), 1, 300.00m, "/source/habitacion.png" )
            };

            reservasOriginal = new ObservableCollection<model.Reserva>(reservasIniciales);
            reservas = new ObservableCollection<model.Reserva>(reservasIniciales);

            listReservas.ItemsSource = reservas;
        }


        // 更新删除按钮的可见性
        private void UpdateDeleteButtonVisibility()
        {
            var selectedCount = reservas.Count(r => r.IsSelected);
            if (selectedCount > 1)
            {
                deleteAllSelectedButton.Visibility = Visibility.Visible;
            }
            else
            {
                deleteAllSelectedButton.Visibility = Visibility.Collapsed;
            }
        }

        private void CheckBox_Checked(object sender, RoutedEventArgs e)
        {
            foreach (var reserva in reservas)
            {
                reserva.IsSelected = true;

            }
            UpdateDeleteButtonVisibility();
        }

        private void CheckBox_UpdateChecked(object sender, RoutedEventArgs e)
        {
            UpdateDeleteButtonVisibility();
        }

        // 监听全选框的取消勾选
        private void CheckBox_Unchecked(object sender, RoutedEventArgs e)
        {
            foreach (var reserva in reservas)
            {
                reserva.IsSelected = false;
            }
            UpdateDeleteButtonVisibility();
        }

        // 删除选中的项
        private void DeleteSelected_Click(object sender, RoutedEventArgs e)
        {
            var selectedItems = reservas.Where(r => r.IsSelected).ToList();

            if (MessageBox.Show($"¿Estás seguro de que deseas eliminar {selectedItems.Count} reserva(s) seleccionada(s)?", "Confirmar eliminación", MessageBoxButton.YesNo, MessageBoxImage.Question) == MessageBoxResult.Yes)
            {

                int invalidCount = 0;

                foreach (var item in selectedItems)
                {
                    if (item.Fecha_fin < DateTime.Today)
                    {
                        invalidCount++;
                        continue;
                    }

                    var itemInOriginal = reservasOriginal.FirstOrDefault(r => r.Id == item.Id);
                    if (itemInOriginal != null)
                    {
                        reservasOriginal.Remove(itemInOriginal);
                    }
                    reservas.Remove(item);

                }

                if (invalidCount > 0)
                {
                    MessageBox.Show($"No se pueden eliminar {invalidCount} reservas porque la fecha de finalización es anterior a hoy.", "Eliminar reserva", MessageBoxButton.OK, MessageBoxImage.Warning);
                }
            }
            else {
                allCheckd.IsChecked = false;
                foreach (var item in selectedItems)
                {
                    item.IsSelected = false;
                }
            }
            UpdateDeleteButtonVisibility();
        }

        // 删除某行
        private void DeleteRow_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            var reserva = button?.DataContext as Reserva;
            if (reserva != null)
            {
                if (reserva.Fecha_fin < DateTime.Today) 
                {
                    MessageBox.Show($"No se puede eliminar la reserva con ID {reserva.Id} porque la fecha de finalización es anterior a hoy.",
                        "Eliminar reserva", MessageBoxButton.OK, 
                        MessageBoxImage.Warning);
                    return;
                }

                if (MessageBox.Show("¿Estás seguro de que deseas eliminar la reserva que selecionado ?", "Confirmar eliminación", MessageBoxButton.YesNo, MessageBoxImage.Question) == MessageBoxResult.Yes) 
                {
                    var itemInOriginal = reservasOriginal.FirstOrDefault(r => r.Id == reserva.Id);
                    if (itemInOriginal != null)
                    {
                        reservasOriginal.Remove(itemInOriginal);
                    }
                    reservas.Remove(reserva);

                    UpdateDeleteButtonVisibility();
                }
            }
        }

        // 编辑按钮点击事件
        private void EditButton_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            var reserva = button?.DataContext as model.Reserva;
            if (reserva != null) 
            {
                if (reserva.Fecha_fin < DateTime.Today)
                {
                    MessageBox.Show($"No se puede modifiacr la reserva con ID {reserva.Id} porque la fecha de finalización es anterior a hoy.",
                       "Eliminar reserva", MessageBoxButton.OK,
                       MessageBoxImage.Warning);
                    return;
                }
                new CrearReserva(this, reserva, CrearReserva.MODIFICAR).Show();
                cerrarView();
            }
        }

        // 查看按钮点击事件
        private void ViewDetailsButton_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            var reserva = button?.DataContext as model.Reserva;
            if (reserva != null)
            {
                CrearReserva detalle = new CrearReserva(this, reserva, CrearReserva.DETALLE);
                detalle.Show();
                cerrarView();
            }
        }

        private void FiltrarReservas()
        {
            // 获取用户输入的过滤条件
            var rolSeleccionado = (ComboBoxItem)rol.SelectedItem;
            string rolFiltro = rolSeleccionado?.Content.ToString() ?? "";
            string nombreClienteFiltro = nombreClienteSelect.Text?.Trim() ?? "";
            string nombreEmpleadoFiltro = nombreEmpleadoSelect.Text?.Trim() ?? "";
            DateTime? fechaInicioFiltro = fechaInicioInput.SelectedDate;
            DateTime? fechaFinalFiltro = fechaFinalInput.SelectedDate;

            if (fechaInicioFiltro.HasValue && fechaFinalFiltro.HasValue && fechaInicioFiltro > fechaFinalFiltro)
            {
                MessageBox.Show($"La fecha de entrada -->{fechaInicioFiltro.Value.ToString("dd-MM-yyyy")} \n no puede ser posterior a la fecha de salida --> {fechaFinalFiltro.Value.ToString("dd-MM-yyyy")}.", "Error en las fechas", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            // 筛选条件
            var reservasFiltradas = reservasOriginal.Where(r =>
                (string.IsNullOrEmpty(rolFiltro) || (rolFiltro == "Cliente" && r.Id_AltUser == 1) ||
                 (rolFiltro == "Empleado" && r.Id_AltUser == 2) ||
                 (rolFiltro == "Administrador" && r.Id_AltUser == 3)) &&
                (string.IsNullOrEmpty(nombreClienteFiltro) || r.Id_usuario.ToString().Contains(nombreClienteFiltro)) &&
                (string.IsNullOrEmpty(nombreEmpleadoFiltro) || r.Id_hab.ToString().Contains(nombreEmpleadoFiltro)) &&
                (!fechaInicioFiltro.HasValue || r.Fecha_ini >= fechaInicioFiltro) &&
                (!fechaFinalFiltro.HasValue || r.Fecha_fin <= fechaFinalFiltro)
            ).ToList();

            // 更新绑定到过滤后的结果
            reservas.Clear();
            foreach (var reserva in reservasFiltradas)
            {
                reservas.Add(reserva);
            }
        }

        private void botonBuscar_Click(object sender, RoutedEventArgs e)
        {
            FiltrarReservas();
        }

        private void ButtonCrear_Click(object sender, RoutedEventArgs e)
        {
            //new BuscarReserva(this).Show();
            cerrarView();
        }

        private void cerrarView()
        {
            Visibility = Visibility.Collapsed;
        }

        private void botonRestablecer_Click(object sender, RoutedEventArgs e)
        {
            rol.SelectedIndex = -1;
            nombreClienteSelect.SelectedIndex = -1;
            nombreEmpleadoSelect.SelectedIndex = -1;
            fechaInicioInput.SelectedDate = null;
            fechaFinalInput.SelectedDate = null;

            // 恢复原始数据
            reservas.Clear();
            foreach (var reserva in reservasOriginal)
            {
                reservas.Add(reserva);
            }
        }

        private void botonBuscador_Click(object sender, RoutedEventArgs e)
        {
            //new BuscarReserva(this).Show();
            cerrarView();
        }


        private void comboBox_KeyUp(object sender, System.Windows.Input.KeyEventArgs e)
        {
            ComboBox comboBox = sender as ComboBox;
            if (comboBox != null) {
                if (e.Key != Key.Enter && e.Key != Key.Escape)
                {
                    comboBox.IsDropDownOpen = true;
                }
            }
        }
    }
}
