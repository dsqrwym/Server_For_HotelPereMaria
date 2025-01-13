using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WPFIntermodular.model
{
    public class Reserva: INotifyPropertyChanged
    {
        public int Id { get; set; }
        public int Id_usuario { get; set; }
        public int Id_hab { get; set; }
        public int Id_AltUser { get; set; }
        public DateTime Fecha_ini { get; set; }
        public DateTime Fecha_fin { get; set; }
        public decimal Precio_total { get; set; }
        public string HabitacionImage { get; set; }

        private bool _isSelected;

        public bool IsSelected
        {
            get => _isSelected;
            set
            {
                _isSelected = value;
                OnPropertyChanged(nameof(IsSelected));
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        protected void OnPropertyChanged(string propertyName) =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
    }
}