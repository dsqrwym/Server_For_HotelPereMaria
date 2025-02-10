using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;

public class ConexionController : Controller
{
    private readonly ConexionApi _conectarService;

    public ConexionController()
    {
        _conectarService = new ConexionApi();
    }

    public async Task<IActionResult> Index()
    {
        var reservas = await _conectarService.AllHabitaciones();
        return View(reservas);
    }

    
}