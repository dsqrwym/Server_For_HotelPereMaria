using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
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
using WPFIntermodular.model.reserva;
using WPFIntermodular.network.reservaService;

namespace WPFIntermodular.view
{
    /// <summary>
    /// CrearReserva.xaml 的交互逻辑
    /// </summary>
    public partial class CrearReserva : Window
    {
        private readonly NavigationService _navigationService = new NavigationService();
        private decimal precio;
        private ObservableCollection<Usuario> listaClientes;
        private ObservableCollection<Usuario> listClientesOriginal;
        private ObservableCollection<Extras> extras;

        private string currentReservaId;
        public readonly static byte DETALLE = 0;

        public readonly static byte MODIFICAR = 1;
        private Habitacion habitacion;
        private async void CargarFechasReservadas(Reserva reserva)
        {
            try
            {
                var fechasReservadas = await ReservaApiService.ObtenerFechasReservadasAsync(reserva.Habitacion._id, reserva.Id);

                foreach (var rango in fechasReservadas)
                {
                    fechaEntradaInput.BlackoutDates.Add(rango);
                    fechaSalidaInput.BlackoutDates.Add(rango);
                    Debug.WriteLine(rango.ToString());
                }
            }
            catch (Exception ex)
            {
                DialogService.ShowError(ex.Message, "Error al cargar fechas reservadas");
            }
        }
        private async void CargarDatosBasicos(Reserva reserva)
        {
            currentReservaId = reserva.Id;
            imageHabitacion.Source = reserva.Habitacion.ImageSource;
            var clientes = await ReservaApiService.ObtenerClientesAsync();

            listaClientes = new ObservableCollection<Usuario>(clientes);
            listClientesOriginal = new ObservableCollection<Usuario>(clientes);
            clienteComboBox.ItemsSource = listaClientes;
            // 设置选中的客户
            var clienteSeleccionado = listaClientes.FirstOrDefault(c => c.Id == reserva.Usuario.Id);
            if (clienteSeleccionado != null)
            {
                clienteComboBox.SelectedItem = clienteSeleccionado;
            }
            else
            {
                listaClientes.Add(reserva.Usuario);
                clienteComboBox.SelectedItem = reserva.Usuario;
            }
        }
        private async void CargarDatosBasicos()
        {
            var clientes = await ReservaApiService.ObtenerClientesAsync();

            listaClientes = new ObservableCollection<Usuario>(clientes);
            listClientesOriginal = new ObservableCollection<Usuario>(clientes);
            clienteComboBox.ItemsSource = listaClientes;
        }


        public CrearReserva(Reserva reserva, byte state)
        {
            InitializeComponent();

            imageHabitacion.Source = reserva.Habitacion.ImageSource;
            /* Falta obtener todos usuarios*/
            CargarDatosBasicos(reserva);
            extras = new ObservableCollection<Extras>(reserva.Habitacion.ExtrasList.Select(extra => {
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
            if(extras.Count == 0)
            {
                extrasList.Visibility = Visibility.Collapsed;
            }
            extrasList.ItemsSource = extras;

            

            if (state == DETALLE)
            {
                titulo.Text = reserva.Habitacion.Id;
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
                titulo.Text = $"Modificar Reserva";
                fechaEntradaInput.IsEnabled = true;
                fechaSalidaInput.IsEnabled = true;
                crearNuevaReserva.Content = "Modificar";
                CargarFechasReservadas(reserva);
            }

            //fecha
            fechaEntradaInput.SelectedDate = reserva.Fecha_ini;
            fechaSalidaInput.SelectedDate = reserva.Fecha_fin;
            fechaEntradaInput.DisplayDateStart = DateTime.Now.AddDays(1);
            fechaSalidaInput.DisplayDateStart = DateTime.Now.AddDays(2);

            //cantidad huesped
            cantidadHuespedes.Value = reserva.CantidadHuesped;

            //Capacidad maxima
            cantidadHuespedes.Maximum = reserva.Habitacion.Capacidad;

            //obtener precio por noche luego sumando extras
            precio = reserva.Habitacion.PrecioNoche;

            //Extras
            updatePrecio();

            // 设置 ComboBox 的显示项
            clienteComboBox.DisplayMemberPath = "Nombre";

        }

        public CrearReserva(Habitacion habitacion, DateTime? fechaEntrada, DateTime? fechaSalida)
        {
            InitializeComponent();

            /* Falta obtener todos usuarios*/
            CargarDatosBasicos();
            this.habitacion = habitacion;

            if (habitacion.ExtrasList.Count == 0)
            {
                contenedorListaExtras.Visibility = Visibility.Collapsed;
            }
            else
            {
                extras = new ObservableCollection<Extras>(
                    habitacion.ExtrasList.Select(e => new Extras
                    {
                        Nombre = e.Nombre,
                        Precio = e.Precio,
                        IsSelected = false
                    }).ToList()
                );
                extrasList.ItemsSource = extras;
            }

            titulo.Text = habitacion.Tipo;

            //Capacidad maxima
            cantidadHuespedes.Maximum = habitacion.Capacidad;

            fechaEntradaInput.SelectedDate = fechaEntrada;
            fechaSalidaInput.SelectedDate = fechaSalida;
            fechaEntradaInput.DisplayDateStart = DateTime.Now.AddDays(1);
            fechaSalidaInput.DisplayDateStart = DateTime.Now.AddDays(2);

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
            decimal precioExtras = 0;
            if (extras != null)
            {
                precioExtras += extras.Where(e => e.IsSelected).Sum(e => e.Precio);// 选中Extras的总价
            }

            decimal precioFinal = precioBase + precioExtras;

            precioTotal.Text = $"{precioFinal:0.00}€";
        }

        private void clienteComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Usuario cliente = clienteComboBox.SelectedItem as Usuario;
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

        private async void crearNuevaReserva_ClickAsync(object sender, RoutedEventArgs e)
        {

            if (clienteComboBox.SelectedItem == null)
            {
                MessageBox.Show("Por favor, seleccione un cliente.", "Error", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            if (!fechaEntradaInput.SelectedDate.HasValue || !fechaSalidaInput.SelectedDate.HasValue)
            {
                MessageBox.Show("Por favor, seleccione fechas de entrada y salida.", "Error", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            DateTime fechaEntrada = fechaEntradaInput.SelectedDate.Value;
            DateTime fechaSalida = fechaSalidaInput.SelectedDate.Value;

            if (fechaSalida <= fechaEntrada)
            {
                MessageBox.Show("La fecha de salida debe ser posterior a la fecha de entrada.", "Error", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            if (cantidadHuespedes.Value == null || cantidadHuespedes.Value <= 0)
            {
                MessageBox.Show("Ingrese una cantidad válida de huéspedes.", "Error", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            Usuario clienteSeleccionado = (Usuario)clienteComboBox.SelectedItem;

            // **2️⃣ 组装 Reserva 对象**
            Reserva nuevaReserva = new Reserva
            {
                Id = currentReservaId,
                Usuario = clienteSeleccionado,
                Habitacion = habitacion,
                Fecha_ini = fechaEntrada,
                Fecha_fin = fechaSalida,
                CantidadHuesped = (int)cantidadHuespedes.Value,
                ExtrasSeleccionadas = extras.Where(e => e.IsSelected).ToList()
            };

            try
            {
                if (crearNuevaReserva.Content.Equals("Modificar"))
                {
                    bool exito = await ReservaApiService.ActualizarReservaAsync(nuevaReserva);
                    if (exito)
                    {
                        MessageBox.Show("Reserva actualizada con éxito.", "Éxito", MessageBoxButton.OK, MessageBoxImage.Information);
                        _navigationService.NavigateTo("adminReserva");
                        this.Close();
                    }
                    else
                    {
                        MessageBox.Show("No se pudo actualizar la reserva.", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                else
                {
                    bool exito = await ReservaApiService.CrearReservaAsync(nuevaReserva);
                    if (exito)
                    {
                        MessageBox.Show("Reserva creada con éxito.", "Éxito", MessageBoxButton.OK, MessageBoxImage.Information);
                        _navigationService.NavigateTo("buscarReserva");
                        this.Close();
                    }
                    else
                    {
                        MessageBox.Show("No se pudo crear la reserva.", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error: {ex.Message}", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void crearNuevaReserva_Click(object sender, RoutedEventArgs e)
        {
            crearNuevaReserva_ClickAsync(sender, e);
        }
    }
}

