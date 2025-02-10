using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Data;

namespace WPFIntermodular.model
{
    public class Habitacion
    {
        public int Id {  get; set; }
        public string Tipo { get; set; }
        public int Capacidad {  get; set; }
        public decimal Precio {  get; set; }
        public Dictionary<string, decimal> Extras { get; set; } = new Dictionary<string, decimal>();
        public List<string> Images { get; set; }
        public string Nombre { get; set; }
        public string Descripccion { get;set; }

        public Habitacion(int id, string tipo, int capacidad, decimal precio, Dictionary<string, decimal> extras, List<string> images,string nombre,string des) 
        {
            Id = id;
            Tipo = tipo;
            Capacidad = capacidad;
            Precio = precio;
            Extras = extras;
            Images = images;
            Nombre = nombre;
            Descripccion = des;

        }

        public string PrimerImagen => Images?.FirstOrDefault() ?? "/source/habitacion.png";
    }
}
