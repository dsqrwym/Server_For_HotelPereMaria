using GalaSoft.MvvmLight.Command;
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

namespace WPFIntermodular.ViewModel
{
    public class Room
    {
        public string ImageSource { get; set; }
        public string RoomName { get; set; }
        public double Price { get; set; }
    }


    /// <summary>
    /// BuscarReserva.xaml 的交互逻辑
    /// </summary>
    public partial class BuscarReserva : Window
    {
        private Window parent;

        private ObservableCollection<model.Habitacion> habitaciones;
        private ObservableCollection<model.Habitacion> habitacionesOriginal;
        private bool atras = false;

        private NavigationService _navigationService;


        private RelayCommand NavigateToSecondWindowCommand { get;  set; }

        public BuscarReserva(Window parent)
        {
            InitializeComponent();
            this.parent = parent;
            inicialPosi(parent);
            _navigationService = new NavigationService();  // Crear una instancia del servicio
            NavigateToSecondWindowCommand = new RelayCommand(nav);

            //Seleccionar tipo de habitacion

            habitaciones = new ObservableCollection<model.Habitacion>
            {
                new model.Habitacion(1, "Individual", 1, 20m, new Dictionary<string, decimal>() { { "Wifi", 5m } }, new List<string>(){"/source/habitacion.png"}),
                new model.Habitacion(2, "Suite", 3, 66m, new Dictionary<string, decimal>() { { "GM", 10m }, { "Parking", 10m}, { "Wifi", 5m}}, new List<string>(){"/source/habitacion.png"}),
                new model.Habitacion(3, "Estándar", 2, 36m, new Dictionary<string, decimal>() { { "Parking", 10m } }, new List<string>(){"/source/habitacion.png"}),
            };
            resultadoHabitacion.ItemsSource = habitaciones;

            habitacionesOriginal = new ObservableCollection<model.Habitacion>(habitaciones);

            List<TipoHab> tiposHab = new List<TipoHab>{
                new TipoHab("Individual", 1),
                new TipoHab("Estándar", 2),
                new TipoHab("Lujo", 2),
                new TipoHab("Suite", 3),
                new TipoHab("Suite dúplex", 6)
            };

            // 将数据绑定到 ComboBox
            tipoHab.ItemsSource = tiposHab;
            tipoHab.DisplayMemberPath = "Nombre";

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
        }

        private void inicialPosi(Window parent)
        {
            this.parent = parent;
            this.WindowStyle = parent.WindowStyle;
            this.WindowState    = parent.WindowState;
            this.Width = parent.Width;
            this.Height = parent.Height;
            this.Left = parent.Left;
            this.Top = parent.Top;
            this.Closing += CrearReserva_Closing;
        }

        private void configuraAdministrarReservaView()
        {
            parent.WindowStyle = this.WindowStyle;
            parent.WindowState = this.WindowState;
            parent.Width = this.Width;
            parent.Height = this.Height;
            parent.Left = this.Left;
            parent.Top = this.Top;
            parent.Visibility = Visibility.Visible;
        }
        private void CrearReserva_Closing(object? sender, System.ComponentModel.CancelEventArgs e)
        {
            configuraAdministrarReservaView();
        }

        private void tipoHab_KeyUp(object sender, KeyEventArgs e)
        {
            if (e.Key != Key.Enter) {
                tipoHab.IsDropDownOpen = true;
            }
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

        private void TextBox_TextChanged_Valid(object sender, TextChangedEventArgs e)
        {
            TextBox textBox = sender as TextBox;

            if (textBox != null)
            {
                // 记录位置
                int caretIndex = textBox.CaretIndex;

                double min = precio.Minimum;  // Slider 的最小值
                double max = precio.Maximum;  // Slider 的最大值

                // 只保留数字，移除其他
                string validText = System.Text.RegularExpressions.Regex.Replace(textBox.Text, "[^0-9]", "");

                // 如果清理后的文本与当前文本不同，则更新文本框内容
                if (textBox.Text != validText)
                {
                    textBox.Text = validText;
                    textBox.CaretIndex = caretIndex > validText.Length ? validText.Length : caretIndex;
                }

                // 范围校验
                if (double.TryParse(validText, out double value))
                {
                    if (value > max)
                    {
                        textBox.Text = max.ToString();
                        textBox.CaretIndex = textBox.Text.Length;
                    }
                }
            }
        }

        private void TextBox_LostFocus(object sender, RoutedEventArgs e)
        {
            TextBox textBox = sender as TextBox;

            if (textBox != null)
            {
                // 如果内容为空或不合法，则设置为 0
                if (string.IsNullOrWhiteSpace(textBox.Text) || !double.TryParse(textBox.Text, out _))
                {
                    textBox.Text = "0";
                }
            }

        }
         private void ButtonBuscar_Click(object sender, RoutedEventArgs e)
        {
            // 重置房间结果
            habitaciones.Clear();

            // 获取筛选条件
            string tipoSeleccionado = (tipoHab.SelectedItem as TipoHab)?.Nombre;
            DateTime? fechaEntrada = fechaEntradaInput.SelectedDate;
            DateTime? fechaSalida = fechaSalidaInput.SelectedDate;
            bool incluyeComidas = checkBox_Comidas.IsChecked == true;
            bool incluyeParking = checkBox_Parking.IsChecked == true;
            bool incluyeGM = checkBox_GM.IsChecked == true;

            // 获取价格范围
            decimal precioDesde = (decimal)precio.LowerValue;
            decimal precioHasta = (decimal)precio.HigherValue;

            // 根据条件筛选
            var resultados = habitacionesOriginal.Where(h =>
            {
                // 1. 检查房间类型
                if (!string.IsNullOrEmpty(tipoSeleccionado) && h.Tipo != tipoSeleccionado)
                {
                    return false;
                }

                // 2. 检查日期范围 需要API
                if (fechaEntrada.HasValue && fechaSalida.HasValue)
                {
                    
                }

                // 3. 检查额外服务
                if (incluyeComidas && !h.Extras.ContainsKey("Comidas"))
                {
                    return false;
                }
                if (incluyeParking && !h.Extras.ContainsKey("Parking"))
                {
                    return false;
                }
                if (incluyeGM && !h.Extras.ContainsKey("GM"))
                {
                    return false;
                }

                // 4. 检查价格范围
                if (h.Precio < precioDesde || h.Precio > precioHasta)
                {
                    return false;
                }

                return true;
            });

            foreach (var habitacion in resultados)
            {
                habitaciones.Add(habitacion);
            }
        }


        private void ButtonReserva_Click(object sender, RoutedEventArgs e)
        {
            Button buttonReserva = sender as Button;
            model.Habitacion habitacion = buttonReserva.DataContext as model.Habitacion;
            DateTime? fechaEntrada = fechaEntradaInput.SelectedDate;
            DateTime? fechaSalida = fechaSalidaInput.SelectedDate;

            if (fechaEntrada != null && fechaSalida != null)
            {
                new CrearReserva(this, habitacion, fechaEntrada, fechaSalida).Show();
            }
            else {
                MessageBox.Show("Por favor, seleccione fechas de entrada y salida.", "Error", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }
        }

        private void ButtonRestaura_Click(object sender, RoutedEventArgs e)
        {
            tipoHab.SelectedItem = null; // 清空房间类型选择
            fechaEntradaInput.SelectedDate = null; // 清空选择的入住日期
            fechaSalidaInput.SelectedDate = null; // 清空选择的退房日期
            checkBox_Comidas.IsChecked = false; // 取消选中“包括餐饮”
            checkBox_Parking.IsChecked = false; // 取消选中“包括停车”
            checkBox_GM.IsChecked = false; // 取消选中“包括健身房”

            // 重置价格范围到默认值
            precio.LowerValue = precio.Minimum;
            precio.HigherValue = precio.Maximum;

            habitaciones.Clear();
            foreach (var habitacion in habitacionesOriginal)
            {
                habitaciones.Add(habitacion);
            }
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            atras = true;
            nav();
        }

        private void Button_Click_1(object sender, RoutedEventArgs e)
        {
            atras = false;
            nav();
        }

        private void nav()
        {
            if (atras)
            {
                this.Hide();
                _navigationService.NavigateTo("login");
            }
            else
            {
                this.Hide();
                _navigationService.NavigateTo("buscarHab");
            }
            
        }
    }
}
