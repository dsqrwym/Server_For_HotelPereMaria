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
        public int CantidadHuesped {  get; set; }
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

        public Reserva(int id, int idUsuario, int idHab, int idAltUser, DateTime fechaIni, DateTime fechaFin, int cantidadHuesped, decimal precioTotal, string habitacionImg) {
            Id = id;
            Id_usuario = idUsuario;
            Id_hab = idHab;
            Id_AltUser = idAltUser;
            Fecha_ini = fechaIni;
            Fecha_fin = fechaFin;
            CantidadHuesped = cantidadHuesped;
            Precio_total = precioTotal;
            HabitacionImage = habitacionImg;
        }
        public event PropertyChangedEventHandler PropertyChanged;
        protected void OnPropertyChanged(string propertyName) =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
    }

    public class Extras
    {
        public string Nombre { get; set; }
        public decimal Precio { get; set; }

        public Extras(string nombre, decimal precio)
        {
            Nombre = nombre;
            Precio = precio;
        }
    }

    public class Cliente
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public string Email { get; set; }

        public Cliente(int id, string nombre, string email) 
        {
            Id = id;
            Nombre = nombre;
            Email = email;
        }
    }

    public class TipoHab { 
        public string Nombre {  get; set; }
        public int Capacidad {  get; set; }

        public TipoHab(string nombre, int capacidad)
        {
            Nombre = nombre;
            Capacidad = capacidad;
        }
    }
}