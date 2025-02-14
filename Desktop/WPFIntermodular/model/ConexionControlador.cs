using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;
using System.Windows;
using WPFIntermodular.model;

public class ConexionController : Controller
{
    private readonly ConexionApi _conectarService;

  
    public ConexionController()
    {
        _conectarService = new ConexionApi();
    }

    public async Task<List<Habitacion>> Index()
    {
        var reservas = await _conectarService.AllHabitaciones();
        return reservas;
    }

    public async Task<bool> GuardarHab(Habitacion h )
    {
        var respuesta = await _conectarService.addHab(h);
        return respuesta;
    }

    public async Task<bool> deleteHab(Habitacion h)
    {
        var respuesta = await _conectarService.deleteHab(h);
        return respuesta;
    }
    public async Task<bool> editHab(Habitacion h)
    {
        var respuesta = await _conectarService.editHab(h);
        return respuesta;
    }

    
}