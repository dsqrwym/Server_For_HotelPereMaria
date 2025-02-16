using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WPFIntermodular.model
{
    public class Usuario
    {
        [JsonProperty("_id")]
        public string? id { get; set; }
        public string? nombre { get; set; }
        public string? apellido { get; set; }
        public string? telefono { get; set; }
        public string? email { get; set; }

        [JsonProperty("NIF")] // Mapea la propiedad NIF de la API a DNI en el código
        public string? dni { get; set; }
        public string? password { get; set; }

        [JsonProperty("tipo")]
        public string? rol { get; set; }
        public string? avatar { get; set; }
    }
}
