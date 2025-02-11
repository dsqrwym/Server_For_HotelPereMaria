using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
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


        public ObservableCollection<Habitacion> Habitaciones
        {
            get => _habitaciones;
            set
            {
                _habitaciones = value;
                OnPropertyChanged();
            }
        }
        public buscarHabitacionViewModel()
        {
            c = new ConexionController();
            Habitaciones = new ObservableCollection<Habitacion>();
            _ =CargarDatos();
        }


        private async Task CargarDatos()
        {
            var habitaciones = await c.Index();
            if (habitaciones != null)
            {
                Habitaciones.Clear();
                foreach (var hab in habitaciones)
                {
                    Habitaciones.Add(hab);
                }
            }
        }


    }
}
