using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using WPFIntermodular.vistaUsuario;

namespace WPFIntermodular
{

    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    /// 
    public partial class MainWindow : Window
    {
        private List<Window> ventanas = new List<Window>();

        public MainWindow()
        {
            InitializeComponent();
            
        }
        public Window buscarVentana(string nombre)
        {
            for (int i = 0; i < ventanas.Count; i++){
                Window w = ventanas[i];
                String titulo = w.Title.ToLower();
                if(w != null &&titulo == nombre.ToLower())
                {
                    w.Show();
                    return w;
                }

            }
            return null;
        }
        public void addVentana(Window w ) {

            ventanas.Add(w);
        }
    }
}