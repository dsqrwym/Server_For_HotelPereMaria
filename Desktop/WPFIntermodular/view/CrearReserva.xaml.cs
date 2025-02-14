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
using WPFIntermodular.network.reservaService;
using Extras = WPFIntermodular.network.reservaService.Extras;
using Usuario = WPFIntermodular.network.reservaService.Usuario;

namespace WPFIntermodular.view
{
    /// <summary>
    /// CrearReserva.xaml 的交互逻辑
    /// </summary>
    public partial class CrearReserva : Window
    {
        private decimal precio;
        private ObservableCollection<Usuario> listaClientes;
        private ObservableCollection<Usuario> listClientesOriginal;
        private ObservableCollection<Extras> extras;

        public readonly static byte DETALLE = 0;

        public readonly static byte MODIFICAR = 1;

        public CrearReserva(network.reservaService.Reserva reserva, byte state)
        {
            InitializeComponent();
            
            /* Falta obtener todos usuarios*/
            listaClientes = new ObservableCollection<Usuario>();
            listClientesOriginal = new ObservableCollection<Usuario>();
            clienteComboBox.ItemsSource = listaClientes;
            extras = new ObservableCollection<Extras>(reserva.Habitacion.Extras.Select(extra => {
                var matchingExtra = reserva.ExtrasSeleccionadas.FirstOrDefault(b => b.Nombre == extra.Nombre);
                if (matchingExtra != null)
                {
                    return new Extras
                    {
                        Nombre = extra.Nombre,
                        Precio = matchingExtra.Precio,  // 用 B 的价格替换
                        IsSelected = true
                    };
                }
                return extra;  // 保持原样
            }).ToList());
            extrasList.ItemsSource = extras;

            // 设置选中的客户
            var clienteSeleccionado = listaClientes.FirstOrDefault(c => c.Id == reserva.Usuario.Id);
            if (clienteSeleccionado != null)
            {
                clienteComboBox.SelectedItem = clienteSeleccionado;
            }

            if (state == DETALLE)
            {
                titulo.Text = $"Reserva {reserva.Id}";
                crearNuevaReserva.Visibility = Visibility.Collapsed;
                cantidadHuespedes.IsEnabled = false;
                clienteComboBox.IsEnabled = false;
                extrasList.IsEnabled = false;
                clienteNombre.Text = reserva.Usuario.Nombre;
                clienteEmail.Text = reserva.Usuario.Email;
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

            //cantidad huesped
            cantidadHuespedes.Value = reserva.CantidadHuesped;

            //Capacidad maxima
            cantidadHuespedes.Maximum = reserva.Habitacion.TipoCapacidad.Capacidad;

            //obtener precio por noche luego sumando extras
            precio = reserva.Habitacion.PrecioNoche;

            //Extras
            updatePrecio();

            // 设置 ComboBox 的显示项
            clienteComboBox.DisplayMemberPath = "Nombre";

        }

        public CrearReserva(network.reservaService.Habitacion habitacion, DateTime? fechaEntrada, DateTime? fechaSalida)
        {
            InitializeComponent();

            /* Falta obtener todos usuarios*/
            listaClientes = new ObservableCollection<Usuario>();
            listClientesOriginal = new ObservableCollection<Usuario>();
            clienteComboBox.ItemsSource = listaClientes;

            if (habitacion.Extras.Count == 0)
            {
                contenedorListaExtras.Visibility = Visibility.Collapsed;
            }
            else
            {
                extrasList.ItemsSource = habitacion.Extras;
            }

            titulo.Text = habitacion.TipoCapacidad.Tipo;

            //Capacidad maxima
            cantidadHuespedes.Maximum = habitacion.TipoCapacidad.Capacidad;

            fechaEntradaInput.SelectedDate = fechaEntrada;
            fechaSalidaInput.SelectedDate = fechaSalida;
            fechaEntradaInput.DisplayDateStart = DateTime.Now.AddDays(1);
            fechaSalidaInput.DisplayDateStart = DateTime.Now.AddDays(2);
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
            precio = habitacion.PrecioNoche;
            updatePrecio();

            // 将数据绑定到 ComboBox
            clienteComboBox.ItemsSource = listaClientes;


            // 设置 ComboBox 的显示项
            clienteComboBox.DisplayMemberPath = "Nombre";

        }

        private void Button_CloseClick(object sender, RoutedEventArgs e)
        {
            this.Close();
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
                if (fechaSalidaInput.SelectedDate.HasValue && fechaSalidaInput.SelectedDate.Value <= fechaEntrada)
                {
                    MessageBox.Show("La fecha de salida no puede ser anterior o igual a la fecha de entrada.",
                                    "Fecha inválida", MessageBoxButton.OK, MessageBoxImage.Warning);
                    fechaEntradaInput.SelectedDate = null;
                }

                fechaSalidaInput.DisplayDateStart = fechaEntrada;
            }
            updatePrecio();
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
                if (fechaEntradaInput.SelectedDate.HasValue && fechaEntradaInput.SelectedDate.Value >= fechaSalida)
                {
                    MessageBox.Show("La fecha de entrada no puede ser posterior o igual a la fecha de salida.",
                                    "Fecha inválida", MessageBoxButton.OK, MessageBoxImage.Warning);
                    fechaSalidaInput.SelectedDate = null;
                }

                fechaEntradaInput.DisplayDateEnd = fechaSalida;
            }
            updatePrecio();
        }

        private void CheckBox_UpdatePrice(object sender, RoutedEventArgs e)
        {
           updatePrecio(); 
        }
        private void updatePrecio()
        {
            int noches = 1; // 默认至少1晚
            if (fechaEntradaInput.SelectedDate.HasValue && fechaSalidaInput.SelectedDate.HasValue)
            {
                noches = (fechaSalidaInput.SelectedDate.Value - fechaEntradaInput.SelectedDate.Value).Days;
                if (noches < 1) noches = 1; // 确保至少1晚
            }

            decimal precioBase = noches * precio; // 房间基础价格（每晚价格 * 天数）

            decimal precioExtras = extras.Where(e => e.IsSelected).Sum(e => e.Precio); // 选中Extras的总价

            decimal precioFinal = precioBase + precioExtras;

            precioTotal.Text = $"{precioFinal:0.00}€";
        }

        private void clienteComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Cliente cliente = clienteComboBox.SelectedItem as Cliente;
            if (cliente != null)
            {
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

