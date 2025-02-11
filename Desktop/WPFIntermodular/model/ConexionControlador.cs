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
        MessageBox.Show("INDEX");
        var reservas = await _conectarService.AllHabitaciones();
        return reservas;
    }

    
}