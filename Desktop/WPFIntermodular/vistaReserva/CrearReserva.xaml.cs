using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using WPFIntermodular.model;

namespace WPFIntermodular.vistaReserva
{
    /// <summary>
    /// CrearReserva.xaml 的交互逻辑
    /// </summary>
    public partial class CrearReserva : Window
    {
        private Window parent;

        private decimal precio;
        private ObservableCollection<Cliente> listaClientes;
        private ObservableCollection<Cliente> listClientesOriginal;

        public readonly static byte DETALLE = 0;

        public readonly static byte MODIFICAR = 1;
        private void inicialPosi(Window parent)
        {
            this.parent = parent;
            this.WindowStyle = parent.WindowStyle;
            this.WindowState = parent.WindowState;
            this.Width = parent.Width;
            this.Height = parent.Height;
            this.Left = parent.Left;
            this.Top = parent.Top;
            this.Closing += CrearReserva_Closing;
        }

        public CrearReserva(Window parent, Reserva reserva, byte state)
        {
            InitializeComponent();

            inicialPosi(parent);

            if (state == DETALLE) {
                titulo.Text = $"Reserva {reserva.Id}";
                crearNuevaReserva.Visibility = Visibility.Collapsed;
                cantidadHuespedes.IsEnabled = false;
                clienteComboBox.IsEnabled = false;
                extrasList.IsEnabled = false;
            }
            else
            {
                //titulo.Text = $"Modificar Reserva: {reserva.Id}";
                titulo.Text = $"Modificar {reserva.Id}";
                fechaEntradaInput.IsEnabled = true;
                fechaSalidaInput.IsEnabled = true;
                crearNuevaReserva.Content = "Modificar";
            }

            //fecha
            fechaEntradaInput.SelectedDate = reserva.Fecha_ini;
            fechaSalidaInput.SelectedDate = reserva.Fecha_fin;

            extrasList.ItemsSource = new List<Extras> {
                new Extras ("Wifi", 5.0m ),
                new Extras ("Parking", 10.0m ),
                new Extras ("Comidas", 20.0m ),
                new Extras ("Piscina",15.0m ),
                new Extras ("Wifi", 5.0m ),
                new Extras ("Wifi", 5.0m ),
                new Extras ("Parking", 10.0m ),
                new Extras ("Comidas", 20.0m ),
                new Extras ("Piscina",15.0m ),
                new Extras ("Wifi", 5.0m ),
                new Extras ("Parking", 10.0m ),
                new Extras ("Comidas", 20.0m ),
                new Extras ("Piscina",15.0m ),
                new Extras ("Spa", 25.0m )
            };

            //cantidad huesped
            cantidadHuespedes.Value = reserva.CantidadHuesped;

            //Capacidad maxima
            cantidadHuespedes.Maximum = 10;

            //obtener precio por noche luego sumando extras
            precio = reserva.Precio_total;

            //Extras
            updatePrecio();

            //Seleccione Cliente
            var clientes = new ObservableCollection<Cliente>{
            new Cliente(1, "Alice", "alice@example.com" ),
            new Cliente(2, "Bob", "bob@example.com" ),
            new Cliente(3, "Charlie", "charlie@example.com" )
            };

            listaClientes = new ObservableCollection<Cliente>(clientes);
            listClientesOriginal = new ObservableCollection<Cliente>(clientes);

            // 将数据绑定到 ComboBox
            clienteComboBox.ItemsSource = listaClientes;


            // 设置 ComboBox 的显示项
            clienteComboBox.DisplayMemberPath = "Nombre";

        }

        public CrearReserva(Window parent, Habitacion habitacion, DateTime? fechaEntrada, DateTime? fechaSalida)
        {
            InitializeComponent();

            inicialPosi(parent);

            if (habitacion.Extras.Count == 0)
            {
                contenedorListaExtras.Visibility = Visibility.Collapsed;
            }
            else
            {
                List<Extras> serviciosAdicionales = new List<Extras>();

                foreach (var extras in habitacion.Extras)
                {
                    serviciosAdicionales.Add(new Extras(extras.Key, extras.Value));
                }

                extrasList.ItemsSource = serviciosAdicionales;
            }

            titulo.Text = habitacion.Tipo;

            //Capacidad maxima
            cantidadHuespedes.Maximum = habitacion.Capacidad;

            fechaEntradaInput.SelectedDate = fechaEntrada;
            fechaSalidaInput.SelectedDate = fechaSalida;
            fechaEntradaInput.DisplayDateStart = DateTime.Now.AddDays(1);
            fechaSalidaInput.DisplayDateStart = DateTime.Now.AddDays(1);
            //Aniade fechas no selecionables
            var dateRanges = new List<CalendarDateRange>{
                new CalendarDateRange(new DateTime(2025, 03, 01), new DateTime(2025, 03, 03)),
                new CalendarDateRange(new DateTime(2025, 04, 05), new DateTime(2025, 04, 10)),
                new CalendarDateRange(new DateTime(2025, 05, 15), new DateTime(2025, 05, 20))
            };

            foreach (var dateRange in dateRanges)
            {
                fechaEntradaInput.BlackoutDates.Add(dateRange);
                fechaSalidaInput.BlackoutDates.Add(dateRange);
            }

            //obtener precio por noche luego sumando extras
            precio = habitacion.Precio;
            updatePrecio();


            //Seleccione Cliente
            var clientes = new ObservableCollection<Cliente>{
            new Cliente(1, "Alice", "alice@example.com" ),
            new Cliente(2, "Bob", "bob@example.com" ),
            new Cliente(3, "Charlie", "charlie@example.com" )
            };

            listaClientes = new ObservableCollection<Cliente>(clientes);
            listClientesOriginal = new ObservableCollection<Cliente>(clientes);

            // 将数据绑定到 ComboBox
            clienteComboBox.ItemsSource = listaClientes;
            

            // 设置 ComboBox 的显示项
            clienteComboBox.DisplayMemberPath = "Nombre";

        }

        private void CrearReserva_Closing(object? sender, System.ComponentModel.CancelEventArgs e)
        {
            configuraParentView();
        }

        private void Button_CloseClick(object sender, RoutedEventArgs e)
        {
            configuraParentView();
            this.Visibility = Visibility.Collapsed;
            this.Close();
        }

        private void configuraParentView()
        {
            parent.WindowStyle = this.WindowStyle;
            parent.WindowState = this.WindowState;
            parent.Width = this.Width;
            parent.Height = this.Height;
            parent.Left = this.Left;
            parent.Top = this.Top;
            parent.Visibility = Visibility.Visible;
        }

        private void fechaEntradaInput_SelectedDateChanged(object sender, SelectionChangedEventArgs e)
        {
            if (fechaEntradaInput.SelectedDate.HasValue)
            {
                DateTime fechaEntrada = fechaEntradaInput.SelectedDate.Value;

                foreach (var fecha in fechaEntradaInput.BlackoutDates)
                {
                    if (fechaSalidaInput.SelectedDate.HasValue && fechaEntrada < fecha.Start && fechaSalidaInput.SelectedDate.Value > fecha.End)
                    {
                        MessageBox.Show("La fecha de entrada seleccionada está dentro de un rango no válido.",
                                        "Fecha inválida", MessageBoxButton.OK, MessageBoxImage.Warning);
                        fechaEntradaInput.SelectedDate = null;
                        return;
                    }
                }

                // No creo es necesario
                if (fechaSalidaInput.SelectedDate.HasValue && fechaSalidaInput.SelectedDate.Value < fechaEntrada)
                {
                    MessageBox.Show("La fecha de salida no puede ser anterior a la fecha de entrada.",
                                    "Fecha inválida", MessageBoxButton.OK, MessageBoxImage.Warning);
                    fechaSalidaInput.SelectedDate = null;
                }

                fechaSalidaInput.DisplayDateStart = fechaEntrada;
            }
        }

        private void fechaSalidaInput_SelectedDateChanged(object sender, SelectionChangedEventArgs e)
        {
            if (fechaSalidaInput.SelectedDate.HasValue)
            {
                DateTime fechaSalida = fechaSalidaInput.SelectedDate.Value;

                foreach (var fecha in fechaSalidaInput.BlackoutDates)
                {
                    if (fechaEntradaInput.SelectedDate.HasValue && fechaSalida > fecha.End && fechaEntradaInput.SelectedDate.Value < fecha.Start)
                    {
                        MessageBox.Show("La fecha de salida seleccionada está dentro de un rango no válido.",
                                        "Fecha inválida", MessageBoxButton.OK, MessageBoxImage.Warning);
                        fechaSalidaInput.SelectedDate = null;
                        return;
                    }
                }
                // Por si acaso
                if (fechaEntradaInput.SelectedDate.HasValue && fechaEntradaInput.SelectedDate.Value > fechaSalida)
                {
                    MessageBox.Show("La fecha de entrada no puede ser posterior a la fecha de salida.",
                                    "Fecha inválida", MessageBoxButton.OK, MessageBoxImage.Warning);
                    fechaEntradaInput.SelectedDate = null;
                }

                fechaEntradaInput.DisplayDateEnd = fechaSalida;
            }
        }

        private void CheckBox_UpdatePrice(object sender, RoutedEventArgs e)
        {
            var checkbox = sender as CheckBox;
            if (checkbox != null) {
                if (checkbox.IsChecked == true) {
                    precio += (decimal)checkbox.Tag;
                }
                else {  
                    precio -= (decimal)checkbox.Tag;
                }
                updatePrecio();
            }
        }
        private void updatePrecio()
        {
            precioTotal.Text = $"{precio.ToString("0.00")}€";
        }

        private void clienteComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Cliente cliente =  clienteComboBox.SelectedItem as Cliente;
            if (cliente != null) {
                clienteNombre.Text = cliente.Nombre;
                clienteEmail.Text = cliente.Email;
            }
        }

        private void clienteComboBox_KeyUp(object sender, KeyEventArgs e)
        {
            var clientesFiltradas = listClientesOriginal.Where(c =>
                string.IsNullOrEmpty(clienteComboBox.Text) || c.Nombre.ToLower().Contains(clienteComboBox.Text.ToLower())
            ).ToList();

            // 更新绑定到过滤后的结果
            listaClientes.Clear();
            foreach (var cliente in clientesFiltradas)
            {
                listaClientes.Add(cliente);
            }

            if (e.Key != Key.Enter)
            {
                clienteComboBox.IsDropDownOpen = true;
            }
        }

        private void cantidadHuespedes_KeyDown(object sender, KeyEventArgs e)
        {
            // 检查是否是数字键输入
            if (e.Key >= Key.D0 && e.Key <= Key.D9 || e.Key >= Key.NumPad0 && e.Key <= Key.NumPad9)
            {
                // 获取数字值
                int input = e.Key >= Key.D0 && e.Key <= Key.D9
                    ? e.Key - Key.D0
                    : e.Key - Key.NumPad0;

                if (cantidadHuespedes.Maximum > 9) // 当最大值大于 9 时支持多位数输入
                {
                    // 当前值拼接，支持多位数输入
                    if (int.TryParse(cantidadHuespedes.Tag?.ToString() + input, out int result))
                    {
                        // 将值限制在 Slider 的范围内
                        if (result > cantidadHuespedes.Maximum) result = (int)cantidadHuespedes.Maximum;
                        if (result < cantidadHuespedes.Minimum) result = (int)cantidadHuespedes.Minimum;

                        // 更新 Slider 值
                        cantidadHuespedes.Value = result;

                        // 暂存值到 Tag，用于多位数输入
                        cantidadHuespedes.Tag = result.ToString();
                    }
                }
                else // 当最大值小于等于 9 时，仅支持单个数字输入
                {
                    // 直接设置输入值为 Slider 值
                    cantidadHuespedes.Value = input;

                    // 如果值超出范围，调整为合法范围
                    if (cantidadHuespedes.Value > cantidadHuespedes.Maximum) cantidadHuespedes.Value = cantidadHuespedes.Maximum;
                    if (cantidadHuespedes.Value < cantidadHuespedes.Minimum) cantidadHuespedes.Value = cantidadHuespedes.Minimum;

                    // 清空 Tag，因为不需要多位数支持
                    cantidadHuespedes.Tag = null;
                }
            }
            else
            {
                cantidadHuespedes.Tag = null; // 非数字键重置暂存值
            }
        }

        private void crearNuevaReserva_Click(object sender, RoutedEventArgs e)
        {
            if (crearNuevaReserva.Content.Equals("Modificar"))
            {

            }
            else
            {

            }
        }
    }
}

