using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;

namespace WPFIntermodular.model
{
    public class Habitacion
    {
        [JsonProperty("Id")]
        public string Id { get; set; }

        [JsonProperty("Tipo")]
        public string Tipo { get; set; }

        [JsonProperty("Capacidad")]
        public int Capacidad { get; set; }

        [JsonProperty("Precio")]
        public double Precio { get; set; }  

        [JsonProperty("Extras")]
        public Dictionary<string,double> Extras { get; set; } = new Dictionary<string, double>();

        [JsonProperty("Images")]
        public string Images { get; set; }

        [JsonProperty("Nombre")]
        public string Nombre { get; set; }

        [JsonProperty("Descripccion")]
        public string Descripccion { get; set; }

        public Habitacion(string id, string tipo, int capacidad, double precio, Dictionary<string, double> extras, string images, string nombre, string descripccion)
        {
            Id = id;
            Tipo = tipo;
            Capacidad = capacidad;
            Precio = precio;
            Extras = extras;
            Images = images;
            Nombre = nombre;
            Descripccion = descripccion;
        }

        public override string ToString()
        {
            return Nombre;
        }
    }

  
}