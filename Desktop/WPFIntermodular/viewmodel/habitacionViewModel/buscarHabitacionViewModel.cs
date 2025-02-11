using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using WPFIntermodular.model;

namespace WPFIntermodular.viewmodel.habitacionViewModel
{
    internal class buscarHabitacionViewModel : INotifyPropertyChanged
    {
        private readonly ConexionController c = new ConexionController();

        public event PropertyChangedEventHandler? PropertyChanged;

        protected void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private ObservableCollection<Habitacion> _habitaciones;

        private static buscarHabitacionViewModel _instancia;


        public ObservableCollection<Habitacion> Habitaciones
        {
            get => _habitaciones;
            set
            {
                _habitaciones = value;
                OnPropertyChanged(nameof (Habitaciones));
            }
        }
        public buscarHabitacionViewModel()
        {
            
            c = new ConexionController();
            Habitaciones = new ObservableCollection<Habitacion>();
            _ =CargarDatos();
        }

        public static buscarHabitacionViewModel instance
        {
            get {
                if (_instancia == null)
                {
                    _instancia = new buscarHabitacionViewModel();
                }
                return _instancia;
            }
          
        }


        public async Task CargarDatos()
        {
            MessageBox.Show("CargarDatos 1");
            var habitaciones = await c.Index();
            if (habitaciones == null )
            {
                MessageBox.Show("No hay nada aqui");
            }
           
            if (habitaciones != null)
            {
                
                foreach(var hab in habitaciones)
                {
                    MessageBox.Show(hab.Id +
                        hab.Tipo+
                        hab.Capacidad+
                        hab.Precio+
                        hab.Extras+ // Extras vacíos
                        new List<string>()+ // Lista de imágenes vacía
                        hab.Nombre+
                        hab.Descripccion);
                    Habitaciones.Add(new Habitacion(
                        hab.Id,
                        hab.Tipo,
                        hab.Capacidad,
                        hab.Precio,
                        hab.Extras, // Extras vacíos
                        new List<string>(), // Lista de imágenes vacía
                        hab.Nombre,
                        hab.Descripccion
                    ));
                }
                   

                
            }
            OnPropertyChanged(nameof(Habitaciones));
        }


    }
}
