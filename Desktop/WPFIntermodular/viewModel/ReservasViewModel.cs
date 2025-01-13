using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using System.Windows;
using System.Globalization;
using System.Windows.Data;

namespace WPFIntermodular.viewModel
{
    public class ReservaViewModel : INotifyPropertyChanged
    {
        public ObservableCollection<model.Reserva> Reservas { get; set; }

        private bool _showDeleteButton;
        public bool ShowDeleteButton
        {
            get => _showDeleteButton;
            set
            {
                _showDeleteButton = value;
                OnPropertyChanged(nameof(ShowDeleteButton));
            }
        }

        private bool _isAllSelected;
        public bool IsAllSelected
        {
            get => _isAllSelected;
            set
            {
                _isAllSelected = value;
                OnPropertyChanged(nameof(IsAllSelected));
                SelectAllItems(value);
            }
        }

        public ICommand DeleteSelectedCommand { get; set; }
        public ICommand EditCommand { get; set; }
        public ICommand DetailsCommand { get; set; }
        public ICommand DeleteCommand { get; set; }

        public ReservaViewModel()
        {
            Reservas = new ObservableCollection<model.Reserva>
            {
                new model.Reserva { Id = 1, Id_usuario = 101, Id_hab = 201, Id_AltUser = 1, Fecha_ini = DateTime.Now, Fecha_fin = DateTime.Now.AddDays(1), Precio_total = 100.5m, HabitacionImage = "/source/usuario.png" },
                new model.Reserva { Id = 2, Id_usuario = 102, Id_hab = 202, Id_AltUser = 1, Fecha_ini = DateTime.Now, Fecha_fin = DateTime.Now.AddDays(2), Precio_total = 200.75m, HabitacionImage = "/source/usuario.png" },
                new model.Reserva { Id = 3, Id_usuario = 103, Id_hab = 203, Id_AltUser = 2, Fecha_ini = DateTime.Now, Fecha_fin = DateTime.Now.AddDays(3), Precio_total = 300.00m, HabitacionImage = "/source/usuario.png" }
            };

            DeleteSelectedCommand = new RelayCommand(_ => DeleteSelected());
            EditCommand = new RelayCommand<model.Reserva>(EditReserva);
            DetailsCommand = new RelayCommand<model.Reserva>(ViewDetails);
            DeleteCommand = new RelayCommand<model.Reserva>(DeleteReserva);
        }

        private void SelectAllItems(bool isSelected)
        {
            foreach (var reserva in Reservas)
            {
                reserva.IsSelected = isSelected;
            }
            UpdateDeleteButtonVisibility();
        }

        private void UpdateDeleteButtonVisibility()
        {
            ShowDeleteButton = Reservas.Any(r => r.IsSelected);
        }

        private void DeleteSelected()
        {
            var selectedItems = Reservas.Where(r => r.IsSelected).ToList();
            foreach (var item in selectedItems)
            {
                Reservas.Remove(item);
            }

            IsAllSelected = false;
        }

        private void EditReserva(model.Reserva reserva)
        {
            if (reserva != null)
            {
                MessageBox.Show($"Editing Reserva: {reserva.Id}");
            }
        }

        private void ViewDetails(model.Reserva reserva)
        {
            if (reserva != null)
            {
                MessageBox.Show($"Viewing Details for Reserva: {reserva.Id}");
            }
        }
        private void DeleteReserva(model.Reserva reserva)
        {
            if (reserva != null)
            {
                Reservas.Remove(reserva);
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        protected void OnPropertyChanged(string propertyName) =>
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
    }

    public class RelayCommand : ICommand
    {
        private readonly Action<object> _execute;
        private readonly Func<object, bool> _canExecute;

        public RelayCommand(Action<object> execute, Func<object, bool> canExecute = null)
        {
            _execute = execute;
            _canExecute = canExecute;
        }

        public bool CanExecute(object parameter) => _canExecute?.Invoke(parameter) ?? true;
        public void Execute(object parameter) => _execute(parameter);
        public event EventHandler CanExecuteChanged;
        public void RaiseCanExecuteChanged() => CanExecuteChanged?.Invoke(this, EventArgs.Empty);
    }

    public class RelayCommand<T> : ICommand
    {
        private readonly Action<T> _execute;
        private readonly Func<T, bool> _canExecute;

        public RelayCommand(Action<T> execute, Func<T, bool> canExecute = null)
        {
            _execute = execute;
            _canExecute = canExecute;
        }

        public bool CanExecute(object parameter) => _canExecute == null || _canExecute((T)parameter);
        public void Execute(object parameter) => _execute((T)parameter);
        public event EventHandler CanExecuteChanged;
        public void RaiseCanExecuteChanged() => CanExecuteChanged?.Invoke(this, EventArgs.Empty);
    }
}