using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using System.IO;
using System.Diagnostics;


namespace WPFIntermodular
{
    /// <summary>
    /// Lógica de interacción para addHabitaciones.xaml
    /// </summary>
    public partial class addHabitaciones : Window
    {
        private byte[] imageBytes;
        public addHabitaciones(Habitacion h, Boolean editar)
        {
            InitializeComponent();
            if (editar)
            {

                fillFields(h);
            }
        }

        private void fillFields(Habitacion h)
        {

            int index = h.Capacidad-1;
            nombreText.Text = h.Nombre;
            huespedesCombo.SelectedIndex = index;
            //imagen buscar como cargarla
            descripccionText.Text = h.Descripccion;
            //precio por noche se calcula a parte
            botonFinal.Content = "Editar";





        }

        private void imagen_Click(object sender, RoutedEventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog
            {
                Title = "Seleccionar archivo multimedia",
                Filter = "Archivos multimedia|*.mp4;*.mp3;*.wav;*.avi;*.mkv;*.jpg;*.png;*.gif|Todos los archivos|*.*",
                Multiselect = false
            };

            if (openFileDialog.ShowDialog() == true)
            {
                byte[] imagen = File.ReadAllBytes(openFileDialog.FileName);  
                String base64 = Convert.ToBase64String(imagen);  
                imageBytes = imagen;  
                convertImage(base64);
            }
        }
        private Byte[] convertImage(String foto)
        {
            if (String.IsNullOrEmpty(foto)){
                MessageBox.Show("No se recibio la imagen correctamente, porfavor seleccionela de nuevo");
                return null;
            }
            else
            {
                try
                {
                    byte[] imagenBytes = Convert.FromBase64String(foto);  // Convertimos de nuevo de Base64 a bytes
                    imageBytes = imagenBytes;  // Asignamos los bytes a la variable global

                    using (var stream = new MemoryStream(imageBytes))
                    {
                        // Creamos una imagen desde el flujo de memoria
                        var bitmap = new BitmapImage();
                        bitmap.BeginInit();
                        bitmap.StreamSource = stream;
                        bitmap.CacheOption = BitmapCacheOption.OnLoad;
                        bitmap.EndInit();

                        // Mostramos la imagen en el control Image
                        imageDisplay.Source = bitmap;
                    }
                }
                catch (Exception e) { }
               
                

            }

            return null;
        }
        private void ConvertirBinarioAImagen(object sender, RoutedEventArgs e)
        {
            try
            {
                if (imageBytes != null && imageBytes.Length > 0)
                {
                    // Convertimos los bytes de vuelta a un flujo de memoria
                    using (var stream = new MemoryStream(imageBytes))
                    {
                        // Creamos una imagen desde el flujo de memoria
                        var bitmap = new BitmapImage();
                        bitmap.BeginInit();
                        bitmap.StreamSource = stream;
                        bitmap.CacheOption = BitmapCacheOption.OnLoad;
                        bitmap.EndInit();

                        // Mostramos la imagen en el control Image de la UI
                        imageDisplay.Source = bitmap;
                    }

                }
                else
                {
                    MessageBox.Show("No hay datos binarios para convertir.");
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al convertir el binario a imagen: {ex.Message}");
            }
        }

        private void botonFinal_Click(object sender, RoutedEventArgs e)
        {

        }
    }


}