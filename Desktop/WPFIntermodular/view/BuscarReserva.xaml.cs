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
using WPFIntermodular.viewmodel.reservaViewModel;

namespace WPFIntermodular.view
{

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
        }

        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            var viewmodel = new BuscarReservaViewModel();
            // 获取 ViewModel 中的 BlackoutDates 并应用到 DatePicker
            foreach (var dateRange in viewmodel.GetBlackoutDates())
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
