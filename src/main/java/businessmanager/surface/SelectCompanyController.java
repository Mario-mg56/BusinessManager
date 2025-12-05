package businessmanager.surface;

import java.io.IOException;
import java.util.ArrayList;

import businessmanager.database.ConnectionDAO;
import businessmanager.database.DataStore;
import businessmanager.management.BusinessManager;
import businessmanager.management.Company;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class SelectCompanyController {

    @FXML
    public Button buttonEditEnterprice;

    @FXML
    public Button buttonDeleteCompany;

    @FXML
    public Button buttonCheckMore;

    @FXML
    private Button buttonAddEnterprice;
    
    @FXML
    private TableView<Company> companyTable;

    @FXML
    private TableColumn<Company, String> colNif;

    @FXML
    private TableColumn<Company, String> colName;

    @FXML
    private TableColumn<Company, String> colCity;

    @FXML
    private TextField searchField;

    @FXML
    private Label lblTotalEmpresas;

    @FXML
    private ObservableList<Company> companyList;

    @FXML
    public void initialize() {
        setupTable();
        loadCompanies();
    }

    private void setupTable() {
        colNif.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNif()));

        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        colCity.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCity()));

        companyList = FXCollections.observableArrayList();
        companyTable.setItems(companyList);
    }

    private void loadCompanies() {
        try {
            ArrayList<Company> companies = ConnectionDAO.getEmpresas();
            companyList.clear();
            companyList.addAll(companies);
            lblTotalEmpresas.setText("Business Manager v1.0 | Total empresas: " + companies.size());
        } catch (Exception e) {
            showAlert("Error", "No se pudieron cargar las empresas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void addEnterprice() throws IOException {
        DataStore.selectedCompany = null;
        BusinessManager.getInstance().editing = false;
        System.out.println("Going to addEditCompanyView");
        App.setRoot("addEditCompanyView");
    }

    @FXML
    private void editEnterprice() throws IOException {
        Company selected = companyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selección requerida", "Por favor, selecciona una empresa de la tabla para editar.");
            return;
        }

        DataStore.selectedCompany = selected;
        BusinessManager.getInstance().editing = true;
        System.out.println("Going to addEditCompanyView to edit: " + selected.getName());
        App.setRoot("addEditCompanyView");
    }

    @FXML
    private void deleteCompany() {
        Company selected = companyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selección requerida", "Por favor, selecciona una empresa de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar empresa?");
        confirm.setContentText("¿Estás seguro de que quieres eliminar la empresa: " + selected.getName() + "?\nEsta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    boolean success = ConnectionDAO.deleteEmpresa(selected.getNif());
                    if (success) {
                        showAlert("Éxito", "Empresa eliminada correctamente.");
                        loadCompanies();
                    } else {
                        showAlert("Error", "No se pudo eliminar la empresa. Verifica que no tenga datos asociados.");
                    }
                } catch (Exception e) {
                    showAlert("Error", "Error al eliminar empresa: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void checkMore() throws IOException {
        Company selectedCompany = companyTable.getSelectionModel().getSelectedItem();
        if (selectedCompany == null) {
            showAlert("Selección requerida", "Por favor, selecciona una empresa de la tabla.");
            return;
        }

        BusinessManager.getInstance().setCurrentCompany(selectedCompany);
        System.out.println("Going to companyCSPView for: " + selectedCompany.getName());
        App.setRoot("companyCSPView");
    }

    @FXML
    private void searchCompanies() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            loadCompanies();
            return;
        }

        try {
            ArrayList<Company> allCompanies = ConnectionDAO.getEmpresas();
            ArrayList<Company> filtered = new ArrayList<>();

            for (Company company : allCompanies) {
                if (company.getName().toLowerCase().contains(searchText) ||
                        company.getNif().toLowerCase().contains(searchText) ||
                        company.getCity().toLowerCase().contains(searchText)) {
                    filtered.add(company);
                }
            }

            companyList.clear();
            companyList.addAll(filtered);
            lblTotalEmpresas.setText("Business Manager v1.0 | Empresas encontradas: " + filtered.size());
        } catch (Exception e) {
            showAlert("Error", "Error en la búsqueda: " + e.getMessage());
        }
    }

    @FXML
    private void refreshList() {
        loadCompanies();
        searchField.clear();
        showAlert("Actualizado", "Lista de empresas actualizada.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}