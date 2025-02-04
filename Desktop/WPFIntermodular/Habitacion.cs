using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WPFIntermodular
{
    public class Habitacion
    {

        private int id { get; set; }
        private string nombre { get; set; }
        private string tipo { get; set; }
        private int capacidad { get; set; }

        private bool ocupada { get; set; }
        private string descripccion { get; set; }
        public Habitacion(int id, string nombre, string tipo, int capacidad, string descripccion, bool ocupada)
        {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
            this.capacidad = capacidad;
            this.descripccion = descripccion;
            this.ocupada = ocupada;
        }

        public bool Ocupada
        {
            get { return ocupada; }
            set { ocupada = value; }
        }
        public int Id
        {
            get { return id; }
            set { id = value; }
        }

        public string Nombre
        {
            get { return nombre; }
            set { nombre = value; }
        }

        public string Tipo
        {
            get { return tipo; }
            set { tipo = value; }
        }

        public int Capacidad
        {
            get { return capacidad; }
            set { capacidad = value; }
        }

        public string Descripccion
        {
            get { return descripccion; }
            set { descripccion = value; }
        }
    }
}