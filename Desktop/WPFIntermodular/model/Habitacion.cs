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

        [JsonProperty("precio")]
        public MongoDecimal Precio { get; set; } 

        [JsonProperty("Extras")]
        public Dictionary<string, MongoDecimal> Extras { get; set; } = new Dictionary<string, MongoDecimal>();

        [JsonProperty("Images")]
        public List<string> Images { get; set; }

        [JsonProperty("Nombre")]
        public string Nombre { get; set; }

        [JsonProperty("Descripccion")]
        public string Descripccion { get; set; }

        public Habitacion(string id, string tipo, int capacidad, MongoDecimal precio, Dictionary<string, MongoDecimal> extras, List<string> images, string nombre, string descripccion)
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

        public string PrimerImagen => Images?.FirstOrDefault() ?? "/source/habitacion.png";

        public override string ToString()
        {
            return Nombre;
        }
    }
    public class MongoDecimal
    {
        [JsonProperty("$numberDecimal")]
        public decimal Value { get; set; }
    }
}